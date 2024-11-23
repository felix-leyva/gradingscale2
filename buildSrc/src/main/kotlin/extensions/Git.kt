package extensions

fun commitCount(): Int? {
    val command = "git rev-list --count HEAD".split(" ")
    val process =
        ProcessBuilder(command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
    return String(process.inputStream.readBytes()).filter { it.isDigit() }.toIntOrNull()
}

fun gitBranch(): String {
    val command = "git rev-parse --abbrev-ref HEAD".split(" ")
    val process =
        ProcessBuilder(command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
    return String(process.inputStream.readBytes())
}
