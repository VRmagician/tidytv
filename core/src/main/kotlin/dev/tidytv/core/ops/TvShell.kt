package dev.tidytv.core.ops

import java.io.IOException

/**
 * The only way tidytv talks to a TV. The real one is the adb module's Kadb connection; tests use
 * FakeTv. Output is stdout and stderr together: legacy adb shells have no exit codes, so every
 * check in core parses output, as upstream's scripts do.
 */
interface TvShell {
    /** Runs one shell command. Throws [TvConnectionLost] when the TV stops answering. */
    suspend fun run(command: String): String

    /** Writes a file on the TV (ADB sync). Throws [TvConnectionLost] when the TV stops answering. */
    suspend fun push(bytes: ByteArray, remotePath: String)
}

class TvConnectionLost(message: String, cause: Throwable? = null) : IOException(message, cause)
