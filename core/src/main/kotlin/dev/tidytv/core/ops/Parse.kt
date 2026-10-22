package dev.tidytv.core.ops

import dev.tidytv.core.lists.PackageName
import dev.tidytv.core.lists.TvInfo

/** Parsers for TV command output. All tolerate CRLF and stray blank lines. */
object Parse {
    /**
     * Free space in KB from `df /sdcard`. Toybox (Android 8+) prints a 1K-blocks table with an
     * "Available" column; old toolbox (Android 7) prints "Free" with units like 3.5G.
     */
    fun dfAvailableKb(out: String): Long? {
        val rows = out.lines().map { it.trim().split(Regex("\\s+")) }.filter { it.size >= 4 }
        val header = rows.firstOrNull { "Available" in it || "Free" in it } ?: return null
        val col = header.indexOf("Available").takeIf { it >= 0 } ?: header.indexOf("Free")
        val row = rows.firstOrNull { it !== header && it.size > col } ?: return null
        val v = row[col]
        v.toLongOrNull()?.let { return it }
        val n = v.dropLast(1).toDoubleOrNull() ?: return null
        val mult = when (v.last().uppercaseChar()) { 'K' -> 1.0; 'M' -> 1024.0; 'G' -> 1024.0 * 1024; 'T' -> 1024.0 * 1024 * 1024; else -> return null }
        return (n * mult).toLong()
    }

    private val PROP = Regex("^\\[([^]]+)]: \\[(.*)]$")
    private val COMPONENT = Regex("([A-Za-z_][A-Za-z0-9_.]*)/([A-Za-z0-9_.$]+)")

    fun lines(out: String) = out.replace("\r", "").split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    fun props(out: String): Map<String, String> =
        lines(out).mapNotNull { PROP.find(it)?.let { m -> m.groupValues[1] to m.groupValues[2] } }.toMap()

    fun tvInfo(props: Map<String, String>): TvInfo = TvInfo(
        model = props["ro.product.model"].orEmpty(),
        device = props["ro.product.device"].orEmpty(),
        product = props["ro.product.name"].orEmpty(),
        brand = props["ro.product.brand"].orEmpty(),
        manufacturer = props["ro.product.manufacturer"].orEmpty(),
        sdk = props["ro.build.version.sdk"]?.toIntOrNull() ?: 0,
        release = props["ro.build.version.release"].orEmpty(),
        serial = listOf("ro.serialno", "ro.boot.serialno").firstNotNullOfOrNull { k -> props[k]?.takeIf { it.isNotBlank() && it != "unknown" } }
            ?: "${props["ro.product.brand"]}-${props["ro.product.model"]}-${props["ro.product.device"]}",
    )

    /**
     * `pm list packages` output. Lines that are not `package:<name>` mean pm printed an error,
     * so the whole answer is refused rather than half-read (tv-detox undo.sh does the same).
     */
    fun packages(out: String): Set<String> {
        val ls = lines(out)
        val bad = ls.filterNot { it.startsWith("package:") }
        if (bad.isNotEmpty()) throw UnexpectedOutput("pm list packages", out)
        return ls.map { it.removePrefix("package:").substringAfterLast('=') }
            .filter { PackageName.isValid(it) }.toSet()
    }

    /** Last line of `resolve-activity --brief`: "pkg/.Activity", or null when nothing resolves. */
    fun component(out: String): String? {
        val last = lines(out).lastOrNull() ?: return null
        return COMPONENT.matchEntire(last)?.value
    }

    fun componentsIn(out: String): Set<String> =
        lines(out).mapNotNull { COMPONENT.matchEntire(it)?.groupValues?.get(1) }.toSet()

    /** Output of [Commands.flags]: package -> its pkgFlags line (empty when it could not be read). */
    fun flags(out: String): Map<String, String> {
        val result = linkedMapOf<String, String>()
        var current: String? = null
        for (l in lines(out)) {
            if (l.startsWith("@")) {
                current = l.removePrefix("@").also { result[it] = "" }
            } else if (current != null && l.contains("pkgFlags")) {
                result[current] = l
            }
        }
        return result
    }

    fun memAvailableKb(out: String): Long? =
        lines(out).firstOrNull { it.startsWith("MemAvailable:") }
            ?.split(Regex("\\s+"))?.getOrNull(1)?.toLongOrNull()

    /** Packages that own something running on the TV: IMEs, accessibility services, TV inputs. */
    fun ownerPackages(vararg outs: String): Set<String> =
        outs.flatMap { out ->
            Regex("([A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+)/").findAll(out).map { it.groupValues[1] }.toList()
        }.filter { PackageName.isValid(it) }.toSet()

    fun mirror(out: String): Set<String>? {
        val ls = lines(out)
        if (ls.lastOrNull() != "END") return null
        return ls.dropLast(1).filter { PackageName.isValid(it) }.toSet()
    }
}

class UnexpectedOutput(val command: String, val output: String) :
    RuntimeException("Unexpected answer to \"$command\": ${output.take(200)}")
