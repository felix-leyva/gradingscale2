#!/usr/bin/env kotlin

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:5.0.3")

import java.time.LocalDateTime

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.stream.consumeAsFlow
import java.io.File

private val homeDir = System.getenv("HOME") ?: error("HOME environment variable is required")
private val serverHost = System.getenv("SSH_SERVER_HOST") ?: error("SSH_SERVER_HOST environment variable is required")
private val serverUser =
    System.getenv("SSH_SERVER_USERNAME") ?: error("SSH_SERVER_USERNAME environment variable is required")
private val serverPath = "/srv/docker/gradingscale"
// Script assumes, you have setup a paswordless ssh access to the server
private val sshKeyName = System.getenv("SSH_KEY_NAME") ?: "id_ed25519"
private val sshKey = File("$homeDir/.ssh/$sshKeyName").path
private val currentPath = (__FILE__).parentFile
private val distDirPath = System.getenv("DIST_DIR_PATH") ?: "composeApp/build/dist/wasmJs/productionExecutable"
private val distDir = File(currentPath, distDirPath).also { it.mkdirs() }
private val deployDirPath = System.getenv("DEPLOY_DIR_PATH") ?: "docker/wasmjs-app"
private val deployDir = File(currentPath, deployDirPath).also { it.mkdirs() }
private val resetColor = "\u001b[0m"


class WasmBuilder : SuspendingCliktCommand() {

    init {
        processLog("Verifying prerequisites...")
        checkPrerequisites()
    }

    private fun Process.consumeAsFlow() =
        inputStream.bufferedReader().lines().consumeAsFlow().flowOn(Dispatchers.Default)

    private suspend fun Process.printOutput() = apply { consumeAsFlow().collect(::println) }

    private inline fun Process.onError(operation: String, onError: () -> Unit) = apply {
        val exitCode = waitFor()
        if (exitCode != 0) {
            errorLog("Operation '$operation' failed with exit code: $exitCode")
            onError()
        }
    }

    private fun timestamp(): String = "\u001b[90m[${LocalDateTime.now()}]$resetColor"
    private fun errorLog(message: String) = println("${timestamp()} \u001b[31mERROR: $message$resetColor")
    private fun successLog(message: String) = println("${timestamp()} \u001b[32mSUCCESS: $message$resetColor")
    private fun processLog(message: String) =
        println("${timestamp()} \u001b[94mPROCESSING: $message$resetColor")

    private val gradleCommand = ProcessBuilder(
        "./gradlew", ":composeApp:wasmJsBrowserDistribution", "--no-daemon"
    ).redirectErrorStream(true)

    private val sshCopyCommand = ProcessBuilder(
        "ssh",
        "-i", sshKey,
        "$serverUser@$serverHost",
        "cd $serverPath && docker compose down && docker compose up -d --build"
    ).redirectErrorStream(true)

    private val rsyncCommand = ProcessBuilder(
        "rsync", "-avz", "--delete", "docker/", "$serverUser@$serverHost:$serverPath/"
    ).redirectErrorStream(true)

    private fun checkPrerequisites() {
        // Check if required commands are available
        val requiredCommands = listOf("rsync", "ssh", "./gradlew")
        requiredCommands.forEach { command ->
            try {
                val process = ProcessBuilder("which", command).start()
                if (process.waitFor() != 0) {
                    errorLog("Required command '$command' is not available in PATH")
                    kotlin.system.exitProcess(1)
                }
            } catch (e: Exception) {
                errorLog("Error checking command '$command': ${e.message}")
                kotlin.system.exitProcess(1)
            }
        }

        // Check if SSH key exists
        if (!File(sshKey).exists()) {
            errorLog("SSH key file '$sshKey' does not exist")
            kotlin.system.exitProcess(1)
        }

        // Check if SSH key has proper permissions
        if (File(sshKey).readText().contains("ENCRYPTED")) {
            errorLog("SSH key '$sshKey' appears to be encrypted. Please use an unencrypted key or setup passwordless ssh access.")
            kotlin.system.exitProcess(1)
        }
    }

    override suspend fun run() = coroutineScope {
        processLog("Building WasmJS Distribution...")
        gradleCommand.start()
            .printOutput()
            .onError("Build WasmJs") {
                cancel()
                return@coroutineScope
            }

        processLog("Start of copy files to deployment dir")
        distDir.copyRecursively(deployDir, overwrite = true) { file, exception ->
            errorLog("File: $file failed copy with ${exception.message}")
            OnErrorAction.TERMINATE
        }.let { completed ->
            when {
                completed -> successLog("Finished copy of ${distDir.walk().count()} files")
                else -> {
                    errorLog("Failed to copy files to deployment directory")
                    cancel()
                    return@coroutineScope
                }
            }
        }

        processLog("Transferring files to server")
        rsyncCommand.start().printOutput().onError("Transfer files to server") {
            cancel()
            return@coroutineScope
        }

        processLog("Deploying application on server")
        sshCopyCommand.also { processLog("${it.command()}") }.start()
            .printOutput()
            .onError("Deploy application on server") {
                cancel()
                return@coroutineScope
            }

        successLog("Deployment completed!")
        echo()
    }
}

runBlocking { WasmBuilder().main(args) }
