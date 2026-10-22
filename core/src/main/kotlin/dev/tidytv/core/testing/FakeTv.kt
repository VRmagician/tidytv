package dev.tidytv.core.testing

import dev.tidytv.core.lists.HomeApp
import dev.tidytv.core.lists.Launchers
import dev.tidytv.core.ops.Commands
import dev.tidytv.core.ops.TvConnectionLost
import dev.tidytv.core.ops.TvShell

/**
 * A stateful simulated Android TV that answers the commands TidyTV sends, with the output formats
 * real TVs print. Used by every unit test and by the app's demo mode. Failure knobs let tests
 * reproduce field problems: dropped connections, refused commands, changes that don't stick,
 * install errors and Home landing on Google TV's setup app.
 */
class FakeTv(
    val props: MutableMap<String, String> = defaultProps(),
    val installed: MutableSet<String> = mutableSetOf(),
    val disabled: MutableSet<String> = mutableSetOf(),
    val persistent: MutableSet<String> = mutableSetOf(),
    /** HOME activities by component, with their intent-filter priority. */
    val homeClaimants: MutableMap<String, Int> = mutableMapOf(),
    var preferredHome: String? = null,
    val settings: MutableMap<String, String> = mutableMapOf(),
    val files: MutableMap<String, String> = mutableMapOf(),
    var wifiOwner: String = "android.uid.system",
    val liveOwners: MutableList<String> = mutableListOf(),
) : TvShell {
    val commands = mutableListOf<String>()
    val pushed = mutableMapOf<String, ByteArray>()

    // Failure knobs
    /** Throw [TvConnectionLost] when command number N (1-based) arrives. */
    var dropAtCommand: Int? = null
    /** Throw on the first command matching this prefix. */
    var dropOnPrefix: String? = null
    var refuseDisable: MutableSet<String> = mutableSetOf()
    var disableDoesNotStick: MutableSet<String> = mutableSetOf()
    var enableDoesNotStick: MutableSet<String> = mutableSetOf()
    var installAnswer: String = "Success"
    /** Which of TidyTV's home screens the next pushed APK is. */
    var installAs: HomeApp = HomeApp.SIMPLE
    var startAnswer: String? = null
    var mirrorWritable = true
    /** Google TV 14 quirk: the setup app's RecoveryActivity wins Home over the preference. */
    var wraithBeatsPreference = false
    var getpropBroken = false
    var listPackagesGarbage = false

    override suspend fun push(bytes: ByteArray, remotePath: String) {
        tick("push $remotePath")
        pushed[remotePath] = bytes
        files[remotePath] = "<apk ${bytes.size} bytes>"
        // A real APK names its package in the manifest (UTF-16 in binary XML): install that home screen.
        if (remotePath == Commands.APK_TMP) HomeApp.entries.firstOrNull { app ->
            val utf16 = app.pkg.toByteArray(Charsets.UTF_16LE)
            bytes.indexOf(utf16) >= 0 || bytes.indexOf(app.pkg.toByteArray()) >= 0
        }?.let { installAs = it }
    }

    /** APK bytes by path, for icon tests. Paths come from [apkPaths]. */
    val apkFiles = mutableMapOf<String, ByteArray>()
    val apkPaths = mutableMapOf<String, String>()

    override suspend fun pull(remotePath: String): ByteArray? {
        tick("pull $remotePath")
        return apkFiles[remotePath]
    }

    override suspend fun run(command: String): String {
        tick(command)
        return answer(command)
    }

    private var attempts = 0

    /** A dropped command never reaches the TV, so it is not recorded in [commands]. */
    private fun tick(command: String) {
        attempts++
        val drop = dropAtCommand == attempts || dropOnPrefix?.let { command.startsWith(it) } == true
        if (drop) {
            dropAtCommand = null
            dropOnPrefix = null
            throw TvConnectionLost("connection reset (simulated) at: $command")
        }
        commands += command
    }

    fun pkgOf(component: String) = component.substringBefore('/')
    private fun enabled(pkg: String) = pkg in installed && pkg !in disabled
    private fun claimantsNow() = homeClaimants.keys.filter { enabled(pkgOf(it)) }.toSet()

    fun home(): String? {
        val candidates = claimantsNow()
        if (wraithBeatsPreference) candidates.firstOrNull { pkgOf(it) == Launchers.SETUPWRAITH }?.let { return it }
        preferredHome?.takeIf { it in candidates }?.let { return it }
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.single()
        val top = candidates.maxOf { homeClaimants.getValue(it) }
        val best = candidates.filter { homeClaimants.getValue(it) == top }
        return if (best.size == 1) best.single() else "android/com.android.internal.app.ResolverActivity"
    }

    /** Android forgets the Home preference whenever the set of HOME apps changes. */
    private inline fun changingHomeSet(block: () -> Unit) {
        val before = claimantsNow()
        block()
        if (claimantsNow() != before) preferredHome = null
    }

    private fun answer(cmd: String): String {
        val c = cmd.trim()
        val args = c.split(Regex("\\s+"))
        return when {
            c == Commands.GETPROP -> if (getpropBroken) "" else props.entries.joinToString("\n") { "[${it.key}]: [${it.value}]" } + "\n"
            c.startsWith("getprop ") -> (props[args[1]] ?: "") + "\n"

            c == Commands.LIST_PACKAGES -> if (listPackagesGarbage) "Error: Could not access the Package Manager.\n"
                else installed.sorted().joinToString("") { "package:$it\n" }
            c == Commands.LIST_DISABLED -> disabled.filter { it in installed }.sorted().joinToString("") { "package:$it\n" }

            c.startsWith("pm disable-user --user 0 ") -> {
                val p = args.last()
                when {
                    p !in installed -> "Error: java.lang.IllegalArgumentException: Unknown package: $p\n"
                    p in refuseDisable || p in persistent ->
                        "Error: java.lang.SecurityException: Shell cannot change component state for $p to 3\n"
                    else -> {
                        if (p !in disableDoesNotStick) changingHomeSet { disabled += p }
                        "Package $p new state: disabled-user\n"
                    }
                }
            }
            c.startsWith("pm enable ") -> {
                val p = args.last()
                if (p !in installed) "Error: java.lang.IllegalArgumentException: Unknown package: $p\n"
                else {
                    if (p !in enableDoesNotStick) changingHomeSet { disabled -= p }
                    "Package $p new state: enabled\n"
                }
            }
            c.startsWith("pm uninstall ") -> {
                val p = args.last()
                if (p !in installed) "Failure [DELETE_FAILED_INTERNAL_ERROR]\n" else {
                    changingHomeSet { installed -= p; disabled -= p }
                    "Success\n"
                }
            }
            c == Commands.INSTALL_TMP -> {
                if (Commands.APK_TMP !in files) "Error: Unable to open file: ${Commands.APK_TMP}\n"
                else {
                    if (installAnswer.startsWith("Success")) changingHomeSet {
                        installed += installAs.pkg
                        homeClaimants[installAs.component] = 0
                    }
                    "Performing Streamed Install\n$installAnswer\n"
                }
            }
            c == Commands.REMOVE_TMP -> { files.remove(Commands.APK_TMP); "" }

            c == Commands.RESOLVE_HOME -> home()?.let { "priority=0 preferredOrder=0 match=0x108000 specificIndex=-1 isDefault=true\n$it\n" }
                ?: "No activity found\n"
            c.startsWith(Commands.RESOLVE_HOME + " ") -> {
                val p = args.last()
                homeClaimants.keys.firstOrNull { pkgOf(it) == p && enabled(p) }
                    ?.let { "priority=0 preferredOrder=0 match=0x108000 specificIndex=-1 isDefault=false\n$it\n" }
                    ?: "No activity found\n"
            }
            c == Commands.QUERY_HOME -> claimantsNow().sorted().joinToString("") { "  $it\n" }
            c.startsWith("cmd package set-home-activity") -> {
                val comp = args.last()
                if (comp in claimantsNow()) { preferredHome = comp; "Success: Home activity set to $comp\n" }
                else "Error: Package $comp is not a valid home activity\n"
            }
            c.startsWith("cmd role add-role-holder") -> {
                val p = args.last()
                homeClaimants.keys.firstOrNull { pkgOf(it) == p && enabled(p) }?.let { preferredHome = it }
                ""
            }
            c.startsWith("am start -W -n ") -> {
                startAnswer ?: run {
                    val comp = args[4]
                    if (enabled(pkgOf(comp))) "Starting: Intent { cmp=$comp }\nStatus: ok\nLaunchState: COLD\nActivity: $comp\nTotalTime: 412\nWaitTime: 420\nComplete\n"
                    else "Starting: Intent { cmp=$comp }\nError type 3\nError: Activity class {$comp} does not exist.\n"
                }
            }
            c == Commands.KEY_HOME -> ""
            c.startsWith("mkdir -p /sdcard/Android/data/") -> "ok\n"
            c.startsWith("printf '%s' '") && c.contains("theme_revision") -> {
                files[c.substringAfter("> ").substringBefore(" &&")] = c.substringAfter("'%s' '").substringBefore("'"); "ok\n"
            }
            c.startsWith("pm grant ") -> "granted\n"
            c.startsWith("pm path ") -> apkPaths[args.last()]?.let { "package:$it\n" } ?: ""
            c.startsWith("stat -c %s ") -> apkFiles[args.last()]?.let { "${it.size}\n" } ?: "stat: cannot access '${args.last()}': No such file or directory\n"
            c.startsWith("echo ") && !c.contains(";") && !c.contains("&&") -> c.removePrefix("echo ").trim('"') + "\n"

            c.startsWith("for p in ") && c.contains("pkgFlags") -> {
                val pkgs = c.removePrefix("for p in ").substringBefore(";").trim().split(" ")
                pkgs.joinToString("") { p ->
                    val flags = when {
                        p !in installed -> ""
                        p in persistent -> "    pkgFlags=[ SYSTEM HAS_CODE PERSISTENT ALLOW_CLEAR_USER_DATA ]\n"
                        else -> "    pkgFlags=[ SYSTEM HAS_CODE ALLOW_CLEAR_USER_DATA ]\n"
                    }
                    "@$p\n$flags"
                }
            }
            c.startsWith("mkdir -p ${Commands.MIRROR_DIR} && printf") -> {
                if (!mirrorWritable) "printf: write error: Read-only file system\n" else {
                    val pkgs = c.substringAfter("'%s\\n' ").substringBefore(" >>").split(" ")
                    files[Commands.MIRROR_FILE] = files[Commands.MIRROR_FILE].orEmpty() + pkgs.joinToString("") { "$it\n" }
                    "ok\n"
                }
            }
            c == Commands.READ_MIRROR -> files[Commands.MIRROR_FILE].orEmpty() + "END\n"
            c.startsWith("if [ -e ${Commands.MIRROR_FILE} ]; then grep -vxF ") -> {
                val pkg = c.removePrefix("if [ -e ${Commands.MIRROR_FILE} ]; then grep -vxF ").substringBefore(' ')
                files[Commands.MIRROR_FILE]?.let { cur -> files[Commands.MIRROR_FILE] = cur.lines().filter { it.isNotEmpty() && it != pkg }.joinToString("") { "$it\n" } }
                "ok\n"
            }
            c == Commands.CLEAR_MIRROR -> { files.remove(Commands.MIRROR_FILE); "cleared\n" }

            c == Commands.MEMINFO -> "MemTotal:        2009240 kB\nMemFree:          112340 kB\nMemAvailable:     ${memAvailable()} kB\n"
            c == Commands.WIFI_OWNERS -> "cname=$wifiOwner\n"
            c == Commands.TV_INPUTS -> liveOwners.joinToString("") { "  inputId: $it/.HdmiService/HW5\n" }

            c.startsWith("settings get ") -> (settings["${args[2]}/${args[3]}"] ?: "null") + "\n"
            c.startsWith("settings put ") -> { settings["${args[2]}/${args[3]}"] = args[4]; "" }
            c.startsWith("settings delete ") -> { settings.remove("${args[2]}/${args[3]}"); "Deleted 1 rows\n" }
            c.startsWith("settings list ") -> settings.filterKeys { it.startsWith(args[2] + "/") }
                .entries.joinToString("") { "${it.key.substringAfter('/')}=${it.value}\n" }

            else -> "/system/bin/sh: ${args.first()}: not found\n"
        }
    }

    /** Each package turned off frees roughly 18 MB in this simulation, for the "Freed X MB" number. */
    private fun memAvailable(): Long = 620_000L + disabled.size * 18_000L

    companion object {
        fun defaultProps() = mutableMapOf(
            "ro.product.model" to "Smart TV Pro",
            "ro.product.device" to "G05",
            "ro.product.name" to "G05_4K_US",
            "ro.product.brand" to "TCL",
            "ro.product.manufacturer" to "TCL",
            "ro.build.version.sdk" to "30",
            "ro.build.version.release" to "11",
            "ro.serialno" to "TCLG05TEST01",
        )

        /** A TCL Google TV with the stock launcherx Home, setupwraith and the usual bloat installed. */
        fun googleTv(sdk: Int = 30): FakeTv = FakeTv().apply {
            props["ro.build.version.sdk"] = sdk.toString()
            props["ro.build.version.release"] = if (sdk >= 34) "14" else "11"
            installed += BASE + listOf(Launchers.LAUNCHERX, Launchers.SETUPWRAITH)
            homeClaimants[Launchers.LAUNCHERX + "/.MainActivity"] = 2
            homeClaimants[Launchers.SETUPWRAITH + "/.RecoveryActivity"] = 1
            homeClaimants["com.android.tv.settings/.FallbackHome"] = -1000
            installed += "com.android.tv.settings"
            preferredHome = Launchers.LAUNCHERX + "/.MainActivity"
            persistent += "com.android.systemui"
            liveOwners += "com.tcl.tvinput"
            settings["secure/enabled_input_methods"] = "com.google.android.leanback.ime/.LeanbackImeService"
            settings["secure/default_input_method"] = "com.google.android.leanback.ime/.LeanbackImeService"
            settings["global/device_provisioned"] = "1"
            settings["secure/user_setup_complete"] = "1"
            settings["global/window_animation_scale"] = "1.0"
        }

        /** Classic Android TV 9 (Mi Box S style) with com.google.android.tvlauncher. */
        fun androidTv9(): FakeTv = FakeTv().apply {
            props["ro.build.version.sdk"] = "28"
            props["ro.build.version.release"] = "9"
            props["ro.product.model"] = "MIBOX4"
            props["ro.product.device"] = "oneday"
            props["ro.product.brand"] = "Xiaomi"
            props["ro.product.manufacturer"] = "Xiaomi"
            installed += BASE + "com.google.android.tvlauncher"
            homeClaimants["com.google.android.tvlauncher/.MainActivity"] = 2
            homeClaimants["com.android.tv.settings/.FallbackHome"] = -1000
            installed += "com.android.tv.settings"
            settings["global/device_provisioned"] = "1"
            settings["secure/user_setup_complete"] = "1"
        }

        val BASE = listOf(
            "android", "com.android.systemui", "com.android.vending", "com.google.android.gms",
            "com.google.android.leanback.ime", "com.tcl.tvinput", "com.tcl.tv",
            "com.tcl.tv.plus", "com.tcl.channelplus", "com.tcl.tv.tclchannel", "com.tcl.waterfall.overseas",
            "com.tcl.partnercustomizer", "com.tcl.messagebox", "com.tcl.ttvs", "com.tcl.guard",
            "com.google.android.katniss", "com.google.android.apps.mediashell", "com.google.android.feedback",
            "com.google.android.youtube.tv", "com.netflix.ninja", "com.google.android.youtube.tvmusic",
            "com.google.android.play.games",
        )
    }
}

private fun ByteArray.indexOf(needle: ByteArray): Int {
    if (needle.isEmpty() || needle.size > size) return -1
    outer@ for (i in 0..size - needle.size) {
        for (j in needle.indices) if (this[i + j] != needle[j]) continue@outer
        return i
    }
    return -1
}
