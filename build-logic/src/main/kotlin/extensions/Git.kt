package extensions

fun gitBranch(): String {
    return try {
        val command = "git rev-parse --abbrev-ref HEAD".split(" ")
        val process = ProcessBuilder(command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        
        val exitCode = process.waitFor()
        if (exitCode == 0) {
            process.inputStream.bufferedReader().readText().trim()
        } else {
            println("Git command failed with exit code: $exitCode")
            "unknown"
        }
    } catch (e: Exception) {
        println("Failed to get git branch: ${e.message}")
        "unknown"
    }
}
