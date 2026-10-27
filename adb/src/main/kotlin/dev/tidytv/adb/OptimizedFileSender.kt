package dev.tidytv.core.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileInputStream

class OptimizedFileSender(private val adbSyncClient: AdbSyncClient) {

    companion object {
        // 512KB delivers optimal throughput without out-of-memory on low-end TV
        private const val OPTIMAL_BUFFER_SIZE = 512 * 1024
    }

    fun pushFile(
        localFile: File,
        remotePath: String
    ): Flow<TransferProgress> = flow {
        val totalBytes = localFile.length()
        var bytesTransferred = 0L

        FileInputStream(localFile).use { fis ->
            val buffer = ByteArray(OPTIMAL_BUFFER_SIZE)
            adbSyncClient.startSyncSession(remotePath, mode = 0b110100100) // 0644

            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                adbSyncClient.writeSyncChunk(buffer, 0, read)
                bytesTransferred += read

                val percent = (bytesTransferred.toDouble() / totalBytes * 100).toInt()
                emit(TransferProgress(bytesTransferred, totalBytes, percent))
            }

            adbSyncClient.finishSyncSession(localFile.lastModified())
        }
    }.flowOn(Dispatchers.IO)
}
