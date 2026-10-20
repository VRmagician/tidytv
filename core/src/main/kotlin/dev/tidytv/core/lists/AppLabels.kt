package dev.tidytv.core.lists

/**
 * Human names and brand icons for packages. Icons are Simple Icons slugs (CC0 artwork); the app
 * draws them from a bundled JSON and falls back to initials when a package has no slug.
 */
object AppLabels {
    private data class Known(val label: String, val icon: String?)

    private val EXACT: Map<String, Known> = mapOf(
        "com.netflix.ninja" to Known("Netflix", "netflix"),
        "com.google.android.youtube.tv" to Known("YouTube", "youtube"),
        "com.google.android.youtube.tvmusic" to Known("YouTube Music", "youtubemusic"),
        "com.google.android.youtube.tvkids" to Known("YouTube Kids", "youtubekids"),
        "com.amazon.amazonvideo.livingroom" to Known("Prime Video", "primevideo"),
        "com.disney.disneyplus" to Known("Disney+", null),
        "com.hulu.livingroomplus" to Known("Hulu", "hulu"),
        "com.wbd.stream" to Known("Max", "hbomax"),
        "in.startv.hotstar" to Known("JioHotstar", "hotstar"),
        "com.spotify.tv.android" to Known("Spotify", "spotify"),
        "tv.twitch.android.app" to Known("Twitch", "twitch"),
        "tv.twitch.android.viewer" to Known("Twitch", "twitch"),
        "com.plexapp.android" to Known("Plex", "plex"),
        "com.peacocktv.peacockandroid" to Known("Peacock", null),
        "com.cbs.ott" to Known("Paramount+", "paramountplus"),
        "com.tubitv" to Known("Tubi", null),
        "tv.pluto.android" to Known("Pluto TV", null),
        "com.crunchyroll.crunchyroid" to Known("Crunchyroll", "crunchyroll"),
        "com.dazn" to Known("DAZN", "dazn"),
        "com.apple.atve.androidtv.appletv" to Known("Apple TV", "appletv"),
        "com.google.android.videos" to Known("Google TV store", "googletv"),
        "com.google.android.play.games" to Known("Play Games", "googleplay"),
        "com.google.android.katniss" to Known("Google Assistant", "googleassistant"),
        "com.google.android.apps.mediashell" to Known("Chromecast built-in", "googlecast"),
        "com.google.android.feedback" to Known("Google Feedback", "google"),
        "com.google.android.marvin.talkback" to Known("TalkBack", "google"),
        "com.google.android.apps.tv.dreamx" to Known("Ambient mode", "google"),
        "com.google.android.tvrecommendations" to Known("Google recommendations", "google"),
        "com.google.android.backdrop" to Known("Backdrop screensaver", "google"),
        "com.amazon.alexa.tv" to Known("Alexa", "amazonalexa"),
        "com.facebook.katana" to Known("Facebook", "facebook"),
        "com.facebook.appmanager" to Known("Facebook App Manager", "facebook"),
        "com.facebook.system" to Known("Facebook services", "facebook"),
        "com.samba.tv" to Known("Samba TV", null),
        "com.google.android.apps.tv.launcherx" to Known("Google TV home screen", "googletv"),
        "com.google.android.tvlauncher" to Known("Android TV home screen", "android"),
        "com.google.android.leanbacklauncher" to Known("Android TV home screen", "android"),
        "com.google.android.tungsten.setupwraith" to Known("Google TV setup", "google"),
    )

    /** Brand by package prefix, for the many vendor services with no friendly name. */
    private val PREFIX_ICON: List<Pair<String, String>> = listOf(
        "com.google." to "google",
        "com.android." to "android",
        "com.tcl." to "tcl",
        "com.sony." to "sony",
        "com.xiaomi." to "xiaomi",
        "com.mitv." to "xiaomi",
        "com.miui." to "xiaomi",
        "com.amazon." to "amazon",
        "com.nvidia." to "nvidia",
        "com.philips." to "philips",
        "org.droidtv." to "philips",
        "com.hisense." to "hisense",
        "com.mediatek." to "mediatek",
        "com.realtek." to "realtek",
        "com.oneplus." to "oneplus",
        "com.netflix." to "netflix",
        "com.spotify." to "spotify",
        "com.samsung." to "samsung",
        "com.huawei." to "huawei",
    )

    /** Every Simple Icons slug the app may ask for; tools/gen-icons.py bundles exactly these. */
    val ALL_SLUGS: Set<String> =
        (EXACT.values.mapNotNull { it.icon } + PREFIX_ICON.map { it.second }).toSet()

    fun iconSlug(pkg: String): String? =
        EXACT[pkg]?.icon ?: PREFIX_ICON.firstOrNull { pkg.startsWith(it.first) }?.second

    /** A short readable name: known name, else the list's note, else the package's last words. */
    fun label(pkg: String, note: String = ""): String {
        EXACT[pkg]?.let { return it.label }
        val fromNote = note.split(Regex("[,;(]| - | -- ")).first().trim()
        if (fromNote.length in 3..40 && !fromNote.contains("re-enable", ignoreCase = true)) {
            return fromNote.replaceFirstChar { it.uppercase() }
        }
        val parts = pkg.split('.').filterNot { it in GENERIC }
        val tail = parts.takeLast(2).joinToString(" ") { it.replace('_', ' ') }
        return tail.replaceFirstChar { it.uppercase() }.ifBlank { pkg }
    }

    /** Two letters for the fallback chip. */
    fun initials(label: String): String =
        label.split(Regex("[^\\p{L}\\p{N}+]+")).filter { it.isNotEmpty() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

    private val GENERIC = setOf("com", "org", "net", "tv", "android", "app", "google", "apps", "service", "services")
}
