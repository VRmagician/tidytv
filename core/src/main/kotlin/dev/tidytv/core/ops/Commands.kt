package dev.tidytv.core.ops

import dev.tidytv.core.lists.PackageName

/**
 * Every shell command TidyTV sends, in one place. Anything variable that goes into a command is a
 * validated package name or a component built from one; nothing else is ever interpolated.
 */
object Commands {
    const val MIRROR_DIR = "/data/local/tmp/tidytv"
    const val MIRROR_FILE = "$MIRROR_DIR/disabled.txt"
    const val APK_TMP = "/data/local/tmp/tidytv-home.apk"
    private const val HOME_ARGS = "-a android.intent.action.MAIN -c android.intent.category.HOME"

    const val GETPROP = "getprop"
    const val LIST_PACKAGES = "pm list packages"
    const val LIST_DISABLED = "pm list packages -d"
    const val RESOLVE_HOME = "cmd package resolve-activity --brief $HOME_ARGS"
    const val QUERY_HOME = "cmd package query-activities --brief $HOME_ARGS"
    const val MEMINFO = "cat /proc/meminfo"
    const val WIFI_OWNERS = "dumpsys wifi | grep -o 'cname=[^ ]*'"
    const val TV_INPUTS = "dumpsys tv_input"
    const val ENABLED_IMES = "settings get secure enabled_input_methods"
    const val DEFAULT_IME = "settings get secure default_input_method"
    const val A11Y_SERVICES = "settings get secure enabled_accessibility_services"
    const val PROVISIONED = "settings get global device_provisioned"
    const val SETUP_COMPLETE = "settings get secure user_setup_complete"
    const val READ_MIRROR = "if [ -e $MIRROR_FILE ]; then cat $MIRROR_FILE; fi; echo END"
    const val CLEAR_MIRROR = "rm -f $MIRROR_FILE; echo cleared"
    const val INSTALL_TMP = "pm install -r $APK_TMP"
    const val REMOVE_TMP = "rm -f $APK_TMP"

    fun settingsList(namespace: String): String {
        require(namespace in NAMESPACES)
        return "settings list $namespace"
    }

    fun disable(pkg: String) = "pm disable-user --user 0 ${PackageName.require(pkg)}"
    fun enable(pkg: String) = "pm enable ${PackageName.require(pkg)}"
    fun uninstall(pkg: String) = "pm uninstall ${PackageName.require(pkg)}"

    /** One round trip for many packages: prints "@pkg" then its pkgFlags line (or nothing). */
    fun flags(pkgs: Collection<String>): String {
        pkgs.forEach { PackageName.require(it) }
        return "for p in ${pkgs.joinToString(" ")}; do echo \"@${'$'}p\"; dumpsys package ${'$'}p | grep -m1 pkgFlags; done"
    }

    /** Appends to the on-TV undo list and prints "ok" only if the write worked. */
    fun mirror(pkgs: Collection<String>): String {
        pkgs.forEach { PackageName.require(it) }
        return "mkdir -p $MIRROR_DIR && printf '%s\\n' ${pkgs.joinToString(" ")} >> $MIRROR_FILE && echo ok"
    }

    fun resolveHomeOf(pkg: String) = "$RESOLVE_HOME ${PackageName.require(pkg)}"
    fun setHome(component: String) = "cmd package set-home-activity --user 0 ${component(component)}"
    fun addHomeRole(pkg: String) =
        "cmd role add-role-holder --user 0 android.app.role.HOME ${PackageName.require(pkg)}"

    /** Where the cleanyourtv home screen reads config.json and theme_revision (tools/theme.sh). */
    fun configDir(pkg: String) = "/sdcard/Android/data/${PackageName.require(pkg)}/files"
    fun makeConfigDir(pkg: String) = "mkdir -p ${configDir(pkg)} && echo ok"
    fun writeRevision(pkg: String, revision: Long) = "printf '%s' '$revision' > ${configDir(pkg)}/theme_revision && echo ok"
    fun grantLocation(pkg: String) = "pm grant ${PackageName.require(pkg)} android.permission.ACCESS_FINE_LOCATION 2>/dev/null; echo granted"

    /** config.json for the cleanyourtv home screen: the dock apps in order, everything else default. */
    fun dockConfig(dockApps: List<String>): String {
        dockApps.forEach { PackageName.require(it) }
        return "{\"dock_apps\": [" + dockApps.joinToString(", ") { "\"$it\"" } + "]}\n"
    }

    fun startLauncher(component: String, dockApps: List<String>, stamp: Long): String {
        val base = "am start -W -n ${component(component)}"
        if (dockApps.isEmpty()) return base
        dockApps.forEach { PackageName.require(it) }
        return "$base --es stamp $stamp --es apps ${dockApps.joinToString(",")}"
    }

    const val KEY_HOME = "input keyevent KEYCODE_HOME"

    private val NAMESPACES = setOf("global", "secure", "system")

    /** "pkg/.Cls" or "pkg/pkg.Cls": both halves made only of package-name characters. */
    fun component(c: String): String {
        val pkg = c.substringBefore('/')
        val cls = c.substringAfter('/', "")
        require(PackageName.isValid(pkg) && cls.isNotEmpty() && Regex("^\\.?[A-Za-z_][A-Za-z0-9_$.]*$").matches(cls)) {
            "not a component: ${c.take(80)}"
        }
        return c
    }
}
