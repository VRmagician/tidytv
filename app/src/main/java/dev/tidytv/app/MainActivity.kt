package dev.tidytv.app

import android.content.Intent
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
import dev.tidytv.app.data.Store
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
                    demo = if (BuildConfig.DEBUG) DemoBackend() else null,
                    lists = { Store.loadLists(app.assets) },
                    launcherApk = { runCatching { app.assets.open("launcher/tvhome.apk").use { it.readBytes() } }.getOrNull() },
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
                TidyApp(state, vm, ::shareLogs)
            }
        }
    }

    private fun shareLogs() {
        val file = vm.exportLogs(File(cacheDir, "share"), BuildConfig.VERSION_NAME)
        val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "tidytv log")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(send, "Share logs"))
    }
}
