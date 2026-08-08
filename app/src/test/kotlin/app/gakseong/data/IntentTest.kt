package app.gakseong.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IntentTest {

    private fun texts(vararg domains: String): List<String> =
        domains.flatMapIndexed { i, d ->
            // Take the i-th available statement of that domain, so repeats pick distinct statements.
            listOf(STATEMENTS.filter { it.domain == d }[minOf(i, STATEMENTS.count { s -> s.domain == d } - 1)].text)
        }.distinct()

    @Test
    fun `the design's own example reads as an assassin`() {
        // "Three of your five statements were about attention" — the sentence the Awakening screen shows.
        val picked = STATEMENTS.filter { it.domain == "Attention" }.take(3).map { it.text } +
            STATEMENTS.first { it.domain == "Rest" }.text +
            STATEMENTS.first { it.domain == "Body" }.text

        assertEquals("ASSASSIN", hunterClassFor(picked))
        assertEquals("Attention" to 3, dominantDomain(picked))
    }

    @Test
    fun `every domain maps to a class`() {
        STATEMENTS.map { it.domain }.distinct().forEach { domain ->
            val picked = STATEMENTS.filter { it.domain == domain }.map { it.text }
            assertEquals("$domain should resolve", true, hunterClassFor(picked) != "ASSASSIN" || domain == "Attention")
        }
    }

    @Test
    fun `rest reads as a healer and isolation as an envoy`() {
        assertEquals("HEALER", hunterClassFor(STATEMENTS.filter { it.domain == "Rest" }.map { it.text }))
        assertEquals("ENVOY", hunterClassFor(STATEMENTS.filter { it.domain == "Isolation" }.map { it.text }))
    }

    @Test
    fun `a tie breaks the same way every time regardless of pick order`() {
        val a = listOf(
            STATEMENTS.first { it.domain == "Attention" }.text,
            STATEMENTS.first { it.domain == "Rest" }.text,
        )
        assertEquals(hunterClassFor(a), hunterClassFor(a.reversed()))
        // Attention comes first in STATEMENTS, so it wins a one-all tie. A user cannot reroll by reordering.
        assertEquals("ASSASSIN", hunterClassFor(a))
    }

    @Test
    fun `no picks is not a crash`() {
        assertEquals("ASSASSIN", hunterClassFor(emptyList()))
        assertNull(dominantDomain(emptyList()))
    }

    @Test
    fun `a statement that is not in the bank is ignored rather than counted`() {
        assertEquals("ASSASSIN", hunterClassFor(listOf("something the user typed themselves")))
    }

    @Test
    fun `the bank is the fifteen the design page lists`() {
        assertEquals(15, STATEMENTS.size)
        assertEquals(15, STATEMENTS.map { it.text }.distinct().size)
    }
}
