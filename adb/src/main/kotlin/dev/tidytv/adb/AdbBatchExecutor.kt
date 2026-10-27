package dev.tidytv.core.adb

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-performance ADB Batch Executor for TidyTV
 * Replaces loop-based adb.shell calls with a single pipeline transaction.
 */
class AdbBatchExecutor(private val adbConnection: AdbClient) {

    suspend fun disablePackagesBatch(
        packageNames: List<String>,
        userId: Int = 0
    ): Map<String, Boolean> = withContext(Dispatchers.IO) {
        if (packageNames.isEmpty()) return@withContext emptyMap()

        // 1. Construct single chained pipeline script with structured delimiter
        val scriptBuilder = StringBuilder()
        scriptBuilder.append("echo '===TIDYTV_BATCH_START==='
")
        for (pkg in packageNames) {
            // pm disable-user runs in subshell; echo exact package and return status
            scriptBuilder.append(
                "pm disable-user --user $userId $pkg > /dev/null 2>&1 && " +
                "echo "$pkg:OK" || echo "$pkg:FAIL"
"
            )
        }
        scriptBuilder.append("echo '===TIDYTV_BATCH_END==='
")

        // 2. Execute entire batch in a single ADB stream roundtrip
        val rawOutput = adbConnection.executeShell(scriptBuilder.toString())

        // 3. Parse delimited output without spawning multiple processes
        parseBatchResponse(rawOutput)
    }

    private fun parseBatchResponse(output: String): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        val lines = output.lines()
        var recording = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed == "===TIDYTV_BATCH_START===") {
                recording = true
                continue
            }
            if (trimmed == "===TIDYTV_BATCH_END===") break

            if (recording && trimmed.contains(":")) {
                val parts = trimmed.split(":")
                if (parts.size >= 2) {
                    val pkg = parts[0].trim()
                    val success = parts[1].trim() == "OK"
                    results[pkg] = success
                }
            }
        }
        return results
    }
}
