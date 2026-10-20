package dev.tidytv.core.lists

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Runs every bundled upstream list through the parser and the safety rules (port of test_examples.py). */
class UpstreamListsTest {
    companion object {
        val upstream = File(System.getProperty("tidytv.upstream") ?: "../upstream/cleanyourtv")
        val sources: Map<String, String> by lazy {
            File(upstream, "examples").listFiles()!!.filter { it.isDirectory }
                .associate { it.name to File(it, "debloat.txt").readText() }
        }
        val catalog by lazy { ListCatalog(sources) }
    }

    @Test fun everyFolderHasAList() {
        assertTrue("expected the upstream submodule checked out", sources.size >= 50)
    }

    @Test fun everyListParses() {
        assertEquals(catalog.rejected.joinToString("\n"), 0, catalog.rejected.size)
        assertEquals(sources.size, catalog.lists.size)
    }

    @Test fun everyListHasAPackageAndATitle() {
        for (l in catalog.lists) {
            assertTrue(l.id, l.entries.isNotEmpty())
            assertTrue(l.id, l.title.isNotBlank())
        }
    }

    @Test fun stockLauncherIsReadWhereStated() {
        val stated = catalog.lists.filter { it.header.any { h -> h.startsWith("Stock launcher: com.") } }
        assertTrue(stated.size >= 40)
        for (l in stated) assertTrue(l.id, l.stockLauncher != null && PackageName.isValid(l.stockLauncher!!))
    }

    @Test fun candidatesNeverIncludeProtectedOrHomeApps() {
        for (l in catalog.lists) {
            val all = (l.entries + l.optional).map { it.pkg }.toSet()
            for (c in Grouping.candidates(l, installed = all)) {
                assertFalse("${l.id}: ${c.pkg}", Protected.isProtected(c.pkg, listKeep = l.keep))
                assertFalse("${l.id}: ${c.pkg}", Launchers.isHomeApp(c.pkg))
                assertFalse("${l.id}: ${c.pkg}", c.pkg in Protected.EXACT)
            }
        }
    }

    @Test fun playServicesAndStoreAreNeverCandidates() {
        for (l in catalog.lists) {
            val all = (l.entries + l.optional).map { it.pkg }.toSet() + setOf("com.google.android.gms", "com.android.vending")
            val c = Grouping.candidates(l, installed = all).map { it.pkg }
            assertFalse(l.id, "com.google.android.gms" in c || "com.android.vending" in c)
        }
    }

    @Test fun mostListedPackagesSurviveTheSafetyFilter() {
        // Guard against over-protection quietly emptying the clean-up.
        var listed = 0
        var offered = 0
        for (l in catalog.lists) {
            val installed = l.entries.map { it.pkg }.toSet()
            listed += installed.size
            offered += Grouping.candidates(l, installed).size
        }
        assertTrue("offered $offered of $listed", offered >= listed * 0.75)
    }

    @Test fun listMatchesItsOwnTitleCodename() {
        val g05 = catalog.byId("tcl-g05-google-tv")!!
        val tv = TvInfo("Smart TV Pro", "G05", "G05_4K_US", "TCL", "TCL", 30, "11", "x")
        val m = ModelMatcher.match(tv, catalog.lists)
        assertTrue(m.first().exact)
        assertTrue(m.any { it.list == g05 })
    }

    @Test fun everyPackageHasALabel() {
        for (l in catalog.lists) for (e in l.entries) assertTrue(e.pkg, AppLabels.label(e.pkg, e.note).isNotBlank())
    }

    @Test fun report() {
        // Not an assertion: prints what the safety filter removes, for review when upstream changes.
        val out = StringBuilder()
        for (l in catalog.lists) {
            val removed = l.entries.map { it.pkg }.filter { Launchers.isHomeApp(it) || Protected.isProtected(it, listKeep = l.keep) }
            if (removed.isNotEmpty()) out.append("${l.id}: ${removed.joinToString { "$it (${Protected.reason(it, listKeep = l.keep) ?: "home app"})" }}\n")
        }
        File("build/list-report.txt").apply { parentFile.mkdirs() }.writeText(out.toString())
    }
}
