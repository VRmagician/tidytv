package dev.tidytv.core.lists

/** What the TV says about itself (from getprop). */
data class TvInfo(
    val model: String,
    val device: String,
    val product: String,
    val brand: String,
    val manufacturer: String,
    val sdk: Int,
    val release: String,
    val serial: String,
) {
    val displayName: String
        get() = listOf(brand.replaceFirstChar { it.uppercase() }, model)
            .filter { it.isNotBlank() }.distinctBy { it.lowercase() }.joinToString(" ")
}

data class ListMatch(val list: DebloatList, val score: Int) {
    /** Tested on this model (codename or model number found in the list's title). */
    val exact: Boolean get() = score >= ModelMatcher.EXACT
}

/**
 * Picks the upstream list for a TV. A list's first line names the TV, its codenames and the
 * Android version, e.g. "TCL Smart TV Pro, US (device G05, product G05_4K_US, ...)". Same brand
 * with a similar Android version is offered as "similar model, not tested" and never as exact.
 */
object ModelMatcher {
    const val EXACT = 90
    private const val SIMILAR = 20

    fun match(tv: TvInfo, lists: List<DebloatList>, limit: Int = 3): List<ListMatch> =
        lists.map { ListMatch(it, score(tv, it)) }
            .filter { it.score >= SIMILAR }
            .sortedWith(compareByDescending<ListMatch> { it.score }.thenBy { it.list.id })
            .take(limit)

    internal fun score(tv: TvInfo, list: DebloatList): Int {
        val titleTokens = tokens(list.title) + tokens(list.id.replace('-', ' '))
        val compactTitle = compact(list.title + " " + list.id)
        var score = 0

        val codenames = listOf(tv.device, tv.product).map { it.lowercase() }.filter { it.length >= 3 }
        if (codenames.any { it in titleTokens }) score = maxOf(score, 100)

        val model = compact(tv.model)
        if (model.length >= 4 && (model in titleTokens || compactTitle.contains(model))) score = maxOf(score, 95)

        val brands = listOf(tv.brand, tv.manufacturer).map { it.lowercase() }.filter { it.length >= 2 }
        val brandHit = brands.any { b -> b in titleTokens || list.id.startsWith("$b-") }
        if (brandHit) {
            var similar = 40
            val major = tv.release.substringBefore('.')
            if (major.isNotEmpty() && Regex("\\b(android|google) tv $major\\b|\\bandroid $major\\b", RegexOption.IGNORE_CASE)
                    .containsMatchIn(list.title + " " + list.header.getOrNull(1).orEmpty())
            ) similar += 20
            score = maxOf(score, similar)
        }
        return score
    }

    private fun tokens(s: String): Set<String> =
        s.lowercase().split(Regex("[^a-z0-9_]+")).filter { it.length >= 3 }.toSet()

    private fun compact(s: String) = s.lowercase().replace(Regex("[^a-z0-9]"), "")
}
