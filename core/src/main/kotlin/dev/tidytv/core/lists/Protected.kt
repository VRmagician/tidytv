package dev.tidytv.core.lists

/**
 * Packages tidytv never disables, whatever a list says. Upstream PROMPT.md: never disable the TV and
 * input services (HDMI, tuner), the input menu, remote and Bluetooth pairing, the keyboard, Play
 * Store or Play Services. Over-matching is fine: a protected package is simply left on.
 *
 * Home-screen apps are not here: they are never part of the clean-up and only the launcher
 * handover may turn them off (see [Launchers]).
 */
object Protected {
    val EXACT: Set<String> = setOf(
        "android",
        "com.android.vending",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.google.android.gsf.login",
        "com.android.systemui",
        "com.android.settings",
        "com.android.tv.settings",
        "com.android.providers.settings",
        "com.android.providers.tv",
        "com.android.providers.media",
        "com.android.providers.downloads",
        "com.android.externalstorage",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.shell",
        "com.android.bluetooth",
        "com.android.bluetoothmidiservice",
        "com.android.inputdevices",
        "com.android.webview",
        "com.google.android.webview",
        "com.android.location.fused",
        "com.google.android.ext.services",
        "com.android.networkstack",
        "com.google.android.networkstack",
        "com.android.wifi.resources",
        "com.android.provision",
        "com.android.keychain",
        "com.android.certinstaller",
        // tidytv's own home screen, once installed.
        Launchers.TIDY_HOME_PACKAGE,
    )

    /** Substrings that mark TV-input, tuner, keyboard and remote-pairing packages. */
    private val PATTERNS = listOf(
        "tvinput", "tv.input", "tvcenter", "hdmi", "tuner", "tvsystem", "tv.framework", "tvserver",
        "inputmethod", ".ime", "leanback.ime", "keyboard",
        "bluetooth", "bt_rcu", "btrcu", "rcuservice", "autopair", "remotepair",
        "networkstack", "permissioncontroller", "packageinstaller",
    )

    fun reason(pkg: String, live: Set<String> = emptySet(), listKeep: Set<String> = emptySet()): String? {
        val lower = pkg.lowercase()
        return when {
            pkg in EXACT -> "needed by Android"
            pkg in live -> "in use by your TV right now"
            pkg in listKeep -> "the list for this TV says to keep it"
            PATTERNS.any { lower.contains(it) } -> "needed for TV inputs, remote or keyboard"
            else -> null
        }
    }

    fun isProtected(pkg: String, live: Set<String> = emptySet(), listKeep: Set<String> = emptySet()) =
        reason(pkg, live, listKeep) != null
}

object Launchers {
    const val TIDY_HOME_PACKAGE = "com.ansub.tvhome"
    const val TIDY_HOME_COMPONENT = "com.ansub.tvhome/.MainActivity"
    const val SETUPWRAITH = "com.google.android.tungsten.setupwraith"
    const val LAUNCHERX = "com.google.android.apps.tv.launcherx"

    /** Stock launchers tidytv may turn off, and only after its own home screen owns Home. */
    val KNOWN_STOCK: Set<String> = setOf(
        "com.google.android.tvlauncher",
        LAUNCHERX,
        "com.google.android.leanbacklauncher",
        "com.mitv.tvhome",
    )

    /** Never part of the clean-up: Home owners and the Google TV setup app that can take Home over. */
    fun isHomeApp(pkg: String) = pkg in KNOWN_STOCK || pkg == SETUPWRAITH || pkg == TIDY_HOME_PACKAGE ||
        pkg == "com.amazon.tv.launcher"
}
