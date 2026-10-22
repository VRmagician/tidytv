package dev.tidytv.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.runtime.getValue
import dev.tidytv.app.data.REPO
import dev.tidytv.app.data.ShareCard
import dev.tidytv.app.data.Store
import dev.tidytv.app.data.WinkleFeedback
import dev.tidytv.app.data.caption
import dev.tidytv.app.ui.Host
import dev.tidytv.app.data.Trace
import dev.tidytv.app.net.AdbBackend
import dev.tidytv.app.net.DemoBackend
import dev.tidytv.app.net.Discovery
import dev.tidytv.app.ui.TidyApp
import dev.tidytv.app.ui.TidyTheme
import java.io.File

class MainActivity : ComponentActivity() {
    private val vm: WizardViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = application
                val store = Store(app.filesDir)
                val trace = Trace(store.logsDir)
                val real by lazy { AdbBackend(Discovery(app, trace::line), store.keyFile, trace::line) }
                WizardViewModel(
                    store = store,
                    trace = trace,
                    real = { real },
                    demo = if (BuildConfig.DEBUG) DemoBackend(phoneApk = { pkg ->
                        runCatching { java.io.File(packageManager.getApplicationInfo(pkg, 0).sourceDir) }.getOrNull()
                    }) else null,
                    lists = { Store.loadLists(app.assets) },
                    launcherApk = { home -> runCatching { app.assets.open("launcher/${home.asset}").use { it.readBytes() } }.getOrNull() },
                    iconFromApk = dev.tidytv.app.data.ApkIcons(app, store)::iconFromApk,
                    updates = dev.tidytv.app.data.UpdateCheck(store.updateFile, BuildConfig.VERSION_NAME, ::httpGet),
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (!vm.back()) finish() }
        })
        setContent {
            TidyTheme {
                val state by vm.ui.collectAsStateWithLifecycle()
                TidyApp(state, vm, ::shareLogs, host)
            }
        }
    }

    private val feedback by lazy {
        WinkleFeedback(
            version = "${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}",
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            os = "Android ${Build.VERSION.RELEASE}",
            userId = installId(),
        )
    }

    private val host = Host(
        sendFeedback = { draft, includeLog ->
            val log = if (includeLog) runCatching { vm.exportLogs(File(cacheDir, "share"), BuildConfig.VERSION_NAME).readText() }.getOrNull() else null
            feedback.send(draft.copy(log = log))
        },
        renderCard = { ShareCard(this, vm.ui.value.icons).render(it) },
        shareCard = { stats ->
            val file = ShareCard(this, vm.ui.value.icons).save(stats, File(cacheDir, "share"))
            val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, stats.caption())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(send, "Share your TidyTV card"))
        },
        openStar = { runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REPO))) } },
        openUrl = { url -> runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } },
    )

    /** A random id for this install, so Joe can tell two messages from one person apart. No account, no device id. */
    private fun installId(): String {
        val f = File(filesDir, "install-id")
        return runCatching { f.readText().trim() }.getOrNull()?.takeIf { it.isNotEmpty() }
            ?: java.util.UUID.randomUUID().toString().also { runCatching { f.writeText(it) } }
    }

    /** A small GET for the update check; null on any error. */
    private fun httpGet(url: String): String? = runCatching {
        val c = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        try {
            c.connectTimeout = 8_000
            c.readTimeout = 8_000
            c.setRequestProperty("Accept", "application/vnd.github+json")
            c.setRequestProperty("User-Agent", "TidyTV/${BuildConfig.VERSION_NAME}")
            if (c.responseCode != 200) null else c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }.getOrNull()

    private fun shareLogs() {
        val file = vm.exportLogs(File(cacheDir, "share"), BuildConfig.VERSION_NAME)
        val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "TidyTV log")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(send, "Share logs"))
    }
}
