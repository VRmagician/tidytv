package dev.tidytv.adb

import com.flyfishxu.kadb.Kadb
import com.flyfishxu.kadb.cert.KadbCert
import com.flyfishxu.kadb.cert.OkioFilePrivateKeyStore
import dev.tidytv.core.ops.TvConnectionLost
import dev.tidytv.core.ops.TvShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.Path.Companion.toOkioPath
import java.io.File
import java.io.IOException

/**
 * The phone's ADB identity. The key is created once and kept in app storage, so after the user
 * ticks "Always allow" the TV never asks again.
 */
object AdbIdentity {
    @Volatile private var configured: File? = null

    @Synchronized
    fun use(keyFile: File) {
        if (configured == keyFile) return
        keyFile.parentFile?.mkdirs()
        KadbCert.configure(OkioFilePrivateKeyStore(keyFile.toOkioPath()))
        KadbCert.ensureReady()
        configured = keyFile
    }
}

/** [TvShell] over a connected Kadb session. Every I/O error becomes [TvConnectionLost]. */
class KadbTvShell(private val kadb: Kadb, private val trace: (String) -> Unit = {}) : TvShell, AutoCloseable {
    override suspend fun run(command: String): String = withContext(Dispatchers.IO) {
        try {
            val started = System.nanoTime()
            val out = kadb.shell(command).allOutput
            trace("$ $command  (${(System.nanoTime() - started) / 1_000_000} ms)\n${out.trimEnd().take(2000)}")
            out
        } catch (e: IOException) {
            trace("$ $command  !! ${e.javaClass.simpleName}: ${e.message}")
            throw TvConnectionLost(e.message ?: "connection lost", e)
        } catch (e: IllegalStateException) {
            throw TvConnectionLost(e.message ?: "connection lost", e)
        }
    }

    override suspend fun push(bytes: ByteArray, remotePath: String) = withContext(Dispatchers.IO) {
        try {
            kadb.push(Buffer().write(bytes), remotePath, mode = "644".toInt(8), lastModifiedMs = System.currentTimeMillis())
            trace("push ${bytes.size} bytes -> $remotePath")
        } catch (e: IOException) {
            trace("push $remotePath !! ${e.message}")
            throw TvConnectionLost(e.message ?: "connection lost", e)
        }
    }

    val isAlive: Boolean get() = kadb.connectionCheck()

    override fun close() = runCatching { kadb.close() }.let { }
}
