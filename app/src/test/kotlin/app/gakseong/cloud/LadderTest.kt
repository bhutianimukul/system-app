package app.gakseong.cloud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * §Social: thin divisions are padded with shadow pacers, never fake people, and pacers are never counted as
 * members. The ladder is the one thing in this app that has to be trustworthy.
 */
class LadderTest {

    @Test
    fun `a handle is opaque and could not be mistaken for a person`() {
        val h = handleFor("dahAAsSIiIZ9YvGQiI0dFZgEwlB3", "D")
        assertTrue("handles start with Hunter", h.startsWith("Hunter "))
        assertFalse("a handle must carry no lowercase name", Regex("""Hunter [A-Z]-\d+""").matches(h).not())
    }

    @Test
    fun `the same uid always gets the same handle`() {
        // A handle that changed between screens would read as two different people.
        assertEquals(handleFor("abc123", "D"), handleFor("abc123", "D"))
    }

    @Test
    fun `different uids get different handles, mostly`() {
        val handles = (1..50).map { handleFor("uid-$it", "D") }.toSet()
        assertTrue("50 uids collapsed to ${handles.size} handles", handles.size > 30)
    }

    @Test
    fun `every pacer says it is a pacer`() {
        // §Social wants the diamond and the label. The row draws the diamond from the flag, so the flag is
        // what has to be right here, and the word has to be in the handle either way.
        pacers("D", 12).forEach {
            assertTrue("${it.handle} must say pacer", it.handle.contains("pacer"))
            assertTrue("${it.handle} must be flagged so the row can mark it", it.pacer)
        }
    }

    @Test
    fun `no pacer carries anything resembling a human name`() {
        // The exact thing §Social forbids. A pacer called "Rahul" would be a fabricated member.
        pacers("D", 30).forEach {
            assertFalse(Regex("""^[A-Z][a-z]{2,}$""").containsMatchIn(it.handle))
        }
    }

    @Test
    fun `pacers are never counted as members`() {
        val ladder = listOf(
            Standing("Hunter D-1", "D", 900, you = true),
            Standing("Hunter D-2", "D", 800),
        ) + pacers("D", 28)
        assertEquals(30, ladder.size)
        assertEquals(2, memberCount(ladder))
    }

    @Test
    fun `a full division needs no pacers at all`() {
        assertTrue(pacers("D", 0).isEmpty())
        assertTrue(pacers("D", -5).isEmpty())
    }

    @Test
    fun `a pacer holds a fixed pace rather than drifting`() {
        // A pacer that varies is pretending to have a bad day, which is pretending to be a person.
        assertEquals(pacers("D", 10), pacers("D", 10))
    }

    @Test
    fun `pacers do not all sit at the top`() {
        // A division of thirty where every pacer out-scores you is not a ladder, it is a wall.
        val p = pacers("D", 10)
        assertTrue("pacers should span a range", p.maxOf { it.aura } - p.minOf { it.aura } > 100)
    }

    @Test
    fun `a league is one rank letter, so everyone in it is asked the same thing`() {
        assertEquals("D", leagueFor("D"))
        assertEquals(30, LEAGUE_SIZE)
    }
}
