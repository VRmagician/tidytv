package dev.tidytv.core.ops

import dev.tidytv.core.lists.HomeApp
import dev.tidytv.core.lists.Launchers

sealed interface RestoreOutcome {
    /** Everything TidyTV changed is back. */
    data class Done(val enabled: List<String>, val homeRestored: Boolean) : RestoreOutcome
    /** Some packages stayed off; TidyTV's home screen was kept so the TV still has one. Run again. */
    data class Partial(val enabled: List<String>, val stillOff: List<String>) : RestoreOutcome
    data class Disconnected(val enabled: List<String>) : RestoreOutcome
}

/**
 * Puts back what TidyTV changed (upstream tools/restore.sh + tv-detox undo.sh). The targets are
 * the phone's action log joined with the undo list on the TV, so either one alone is enough.
 * Home only moves back once every package is on again; until then the current Home stays.
 */
class Restorer(
    private val tv: TvShell,
    private val log: ActionLog,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend fun run(onEnabled: (String) -> Unit = {}): RestoreOutcome {
        val enabled = mutableListOf<String>()
        try {
            val records = log.records()
            val mirror = Parse.mirror(tv.run(Commands.READ_MIRROR)) ?: emptySet()
            val targets = (records.disabledByUs() + mirror).toList()
            val disabledNow = Parse.packages(tv.run(Commands.LIST_DISABLED))

            for (pkg in targets) {
                if (pkg !in disabledNow) {
                    // Already on, or uninstalled since: nothing to do, but record it as back on.
                    log.append(ActionRecord(clock(), Op.ENABLE, pkg, Phase.DONE, detail = "was not off"))
                    continue
                }
                log.append(ActionRecord(clock(), Op.ENABLE, pkg, Phase.INTENT))
                tv.run(Commands.enable(pkg))
            }
            val after = Parse.packages(tv.run(Commands.LIST_DISABLED))
            val stillOff = mutableListOf<String>()
            for (pkg in targets.filter { it in disabledNow }) {
                if (pkg in after) {
                    log.append(ActionRecord(clock(), Op.ENABLE, pkg, Phase.FAILED, detail = "still off"))
                    stillOff += pkg
                } else {
                    log.append(ActionRecord(clock(), Op.ENABLE, pkg, Phase.DONE))
                    enabled += pkg
                    onEnabled(pkg)
                }
            }
            if (stillOff.isNotEmpty()) return RestoreOutcome.Partial(enabled, stillOff)

            for ((key, before) in records.changedSettings()) {
                val (ns, name) = key.split('/', limit = 2)
                val cmd = if (before == null || before == "null") Settings.delete(ns, name) else Settings.put(ns, name, before)
                tv.run(cmd)
                log.append(ActionRecord(clock(), Op.SETTING, key, Phase.DONE, detail = "restored"))
            }

            val homeRestored = restoreHome(records)
            tv.run(Commands.CLEAR_MIRROR)
            return RestoreOutcome.Done(enabled, homeRestored)
        } catch (e: TvConnectionLost) {
            return RestoreOutcome.Disconnected(enabled)
        }
    }

    /**
     * Turns just [pkgs] back on (GitHub #2: "I turned Netflix off by mistake"). Only apps TidyTV
     * turned off are touched, and never a home app: bringing one back alone could move Home, so
     * those stay with the full restore. Each app that comes back leaves the TV's undo list, so a
     * later full restore (or a restore from a new phone) doesn't count it twice.
     */
    suspend fun enableOnly(pkgs: Set<String>, onEnabled: (String) -> Unit = {}): RestoreOutcome {
        val enabled = mutableListOf<String>()
        try {
            val mirror = Parse.mirror(tv.run(Commands.READ_MIRROR)) ?: emptySet()
            val ours = log.records().disabledByUs() + mirror
            val targets = pkgs.filter { it in ours && !Launchers.isHomeApp(it) }
            if (targets.isEmpty()) return RestoreOutcome.Done(enabled, homeRestored = false)
            val disabledNow = Parse.packages(tv.run(Commands.LIST_DISABLED))
            for (pkg in targets) {
                if (pkg !in disabledNow) {
                    log.append(ActionRecord(clock(), Op.ENABLE, pkg, Phase.DONE, detail = "was not off"))
                    tv.run(Commands.unmirror(pkg))
                    continue
                }
                log.append(ActionRecord(clock(), Op.ENABLE, pkg, Phase.INTENT))
                tv.run(Commands.enable(pkg))
            }
            val after = Parse.packages(tv.run(Commands.LIST_DISABLED))
            val stillOff = mutableListOf<String>()
            for (pkg in targets.filter { it in disabledNow }) {
                if (pkg in after) {
                    log.append(ActionRecord(clock(), Op.ENABLE, pkg, Phase.FAILED, detail = "still off"))
                    stillOff += pkg
                } else {
                    log.append(ActionRecord(clock(), Op.ENABLE, pkg, Phase.DONE))
                    tv.run(Commands.unmirror(pkg))
                    enabled += pkg
                    onEnabled(pkg)
                }
            }
            return if (stillOff.isEmpty()) RestoreOutcome.Done(enabled, homeRestored = false) else RestoreOutcome.Partial(enabled, stillOff)
        } catch (e: TvConnectionLost) {
            return RestoreOutcome.Disconnected(enabled)
        }
    }

    private suspend fun restoreHome(records: List<ActionRecord>): Boolean {
        val original = records.originalHome() ?: return false
        val pkg = original.substringBefore('/')
        // Resolve the component on the TV: Android 9 wants pkg/activity, and the activity may differ.
        val component = Parse.component(tv.run(Commands.resolveHomeOf(pkg))) ?: original
        if (sdk() >= 29) tv.run(Commands.addHomeRole(pkg))
        tv.run(Commands.setHome(component))
        log.append(ActionRecord(clock(), Op.SET_HOME, component, Phase.DONE, detail = "restored"))
        val now = Parse.component(tv.run(Commands.RESOLVE_HOME))
        if (now?.substringBefore('/') != pkg) return false

        for (app in HomeApp.entries) if (records.launcherInstalledByUs(app.pkg)) {
            log.append(ActionRecord(clock(), Op.UNINSTALL, app.pkg, Phase.INTENT))
            val out = tv.run(Commands.uninstall(app.pkg))
            val phase = if (out.contains("Success")) Phase.DONE else Phase.FAILED
            log.append(ActionRecord(clock(), Op.UNINSTALL, app.pkg, phase, detail = out.take(200)))
        }
        tv.run(Commands.KEY_HOME)
        return true
    }

    private suspend fun sdk(): Int = tv.run("getprop ro.build.version.sdk").trim().toIntOrNull() ?: 0
}

/** Settings commands, with names checked the same way as package names. */
object Settings {
    private val NAME = Regex("^[A-Za-z0-9_.:-]+$")
    private val VALUE = Regex("^[A-Za-z0-9_.:,/-]*$")
    private fun ns(n: String) = n.also { require(it in setOf("global", "secure", "system")) }
    private fun name(n: String) = n.also { require(NAME.matches(it)) { "bad setting name" } }
    private fun value(v: String) = v.also { require(VALUE.matches(it)) { "bad setting value" } }

    fun get(ns: String, name: String) = "settings get ${ns(ns)} ${name(name)}"
    fun put(ns: String, name: String, value: String) = "settings put ${ns(ns)} ${name(name)} ${value(value)}"
    fun delete(ns: String, name: String) = "settings delete ${ns(ns)} ${name(name)}"
}
