package app.gakseong.ai

import app.gakseong.quest.Verifier
import gakseong.engine.Provability
import gakseong.engine.award
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * §7: the AI never touches the aura number, and clamps are enforced in code rather than in the prompt.
 *
 * A prompt is a request. These are the checks that a model which ignores the request cannot do any harm.
 */
class GenerateTest {

    @Test
    fun `a verifier outside the closed set is not a quest`() {
        listOf("MEDITATE_DEEPLY", "BE_HAPPY", "", "DROP TABLE quests", "steps; rm -rf").forEach {
            assertNull("$it should be refused", toTemplate(Generated(verifier = it, title = "x"), "gen"))
        }
    }

    @Test
    fun `every allowed verifier produces a real quest`() {
        ALLOWED_VERIFIERS.forEach { v ->
            val t = toTemplate(Generated(verifier = v, minutes = 30, steps = 5000, metres = 2000, title = "Go"), "gen")
            assertNotNull("$v should build", t)
        }
    }

    @Test
    fun `a lowercase or mixed case verifier still resolves`() {
        assertNotNull(toTemplate(Generated(verifier = "steps", steps = 5000, title = "Walk"), "gen"))
        assertNotNull(toTemplate(Generated(verifier = "Screen_Off_Block", minutes = 45, title = "Off"), "gen"))
    }

    // ── the clamps ───────────────────────────────────────────────────────────

    @Test
    fun `an absurd duration is clamped rather than issued`() {
        val t = toTemplate(Generated(verifier = "SCREEN_OFF_BLOCK", minutes = 100_000, title = "Off"), "gen")
        assertEquals(Clamp.MAX_MINUTES, (t!!.verifier as Verifier.ScreenOffBlock).minMinutes)
    }

    @Test
    fun `an absurd step target is clamped`() {
        // §5: no penalty may demand physical effort, and a quest asking for forty thousand steps is not a hard
        // quest, it is a broken one.
        val t = toTemplate(Generated(verifier = "STEPS", steps = 400_000, title = "Walk"), "gen")
        assertEquals(Clamp.MAX_STEPS, (t!!.verifier as Verifier.Steps).target)
    }

    @Test
    fun `a zero or negative number is raised to the floor rather than issued`() {
        val zero = toTemplate(Generated(verifier = "FOCUS_SESSION", minutes = 0, title = "Focus"), "gen")
        assertEquals(Clamp.MIN_MINUTES, (zero!!.verifier as Verifier.FocusSession).minMinutes)

        val negative = toTemplate(Generated(verifier = "STEPS", steps = -9000, title = "Walk"), "gen")
        assertEquals(Clamp.MIN_STEPS, (negative!!.verifier as Verifier.Steps).target)
    }

    @Test
    fun `a long title is cut rather than allowed to break the card`() {
        val t = toTemplate(Generated(verifier = "STEPS", steps = 5000, title = "x".repeat(500)), "gen")
        assertEquals(Clamp.MAX_TITLE, t!!.title.length)
    }

    @Test
    fun `an empty title is not a quest`() {
        assertNull(toTemplate(Generated(verifier = "STEPS", steps = 5000, title = "   "), "gen"))
    }

    // ── the aura, which the model never sees ─────────────────────────────────

    @Test
    fun `the model has no field for aura`() {
        // If this ever fails, somebody added one, and §7 went with it.
        val fields = Generated::class.java.declaredFields.map { it.name.lowercase() }
        assertFalse("Generated carries an aura field", fields.any { it.contains("aura") })
        assertFalse("Generated carries a points field", fields.any { it.contains("point") })
        assertFalse("Generated carries a reward field", fields.any { it.contains("reward") })
    }

    @Test
    fun `aura comes from the verifier kind and not from the generation`() {
        val a = toTemplate(Generated(verifier = "STEPS", steps = 5000, title = "Walk"), "gen")!!
        val b = toTemplate(Generated(verifier = "STEPS", steps = 15_000, title = "Walk further"), "gen")!!
        assertEquals("a harder ask must not pay itself more", a.baseAura, b.baseAura)
    }

    @Test
    fun `a generated declared quest still pays the declared rate`() {
        // The most valuable thing to forge would be a declared quest at a sensor rate.
        val t = toTemplate(Generated(verifier = "DECLARED", title = "Sit quietly"), "gen")!!
        assertEquals(Provability.DECLARED, t.verifier.provability)
        assertTrue(award(t.baseAura, t.verifier.provability) < award(t.baseAura, Provability.SENSOR))
    }

    @Test
    fun `no generated quest can out-pay the static bank's best`() {
        val best = app.gakseong.quest.BANK.maxOf { award(it.baseAura, it.verifier.provability) }
        ALLOWED_VERIFIERS.forEach { v ->
            val t = toTemplate(Generated(verifier = v, minutes = 999, steps = 99_999, metres = 99_999, title = "x"), "gen")
            t?.let {
                assertTrue(
                    "$v paid ${award(it.baseAura, it.verifier.provability)} against a bank best of $best",
                    award(it.baseAura, it.verifier.provability) <= best,
                )
            }
        }
    }
}

/** The key itself, and the rule that the app works without one. */
class KeyTest {

    @Test
    fun `a well formed key is recognised`() {
        assertTrue(looksLikeKey("AIza" + "a".repeat(35)))
        assertTrue(looksLikeKey("  AIza" + "B1_-".repeat(8) + "abc" + "  "))
    }

    @Test
    fun `anything else is not`() {
        listOf("", "hello", "AIza", "sk-proj-abc123", "AIza" + "a".repeat(10)).forEach {
            assertFalse("$it should not look like a key", looksLikeKey(it))
        }
    }

    @Test
    fun `a key is longer than any dimension the wire will carry`() {
        // §AI gate: never logged, never in analytics. cloud/Wire.kt refuses anything over 36 characters, so a
        // key cannot ride out as a dimension even if somebody tried.
        assertNotNull(app.gakseong.cloud.refusedReason("AIza" + "a".repeat(35)))
    }

    @Test
    fun `the daily quest is not one of the features that lock`() {
        // §AI gate: it never locks, and falls back to the static bank.
        assertTrue(DAILY_QUEST_NEVER_LOCKS)
        AiFeature.entries.forEach {
            assertFalse("${it.name} must not be the daily quest", it.name.contains("DAILY"))
        }
        assertEquals(5, AiFeature.entries.size)
    }

    @Test
    fun `the static bank still issues a full day with no key at all`() {
        val day = app.gakseong.quest.draw("2026-08-08", app.gakseong.quest.Readings())
        assertTrue("a user with no key must still get quests", day.isNotEmpty())
    }
}

/** The prompt and the request, checked for the things that must never be in them. */
class PromptTest {

    @Test
    fun `no key means locked, not failed`() = kotlinx.coroutines.runBlocking {
        // §AI gate: the feature is locked, the app is not. A blank key is not an error to show anybody.
        assertTrue(generateQuest("", "a quiet week") is AiResult.Locked)
    }

    @Test
    fun `the model is never offered a verifier that names an app`() {
        assertFalse(ALLOWED_VERIFIERS.any { it.contains("APP") })
    }

    @Test
    fun `every verifier the model is offered can actually be built`() {
        // The first draft offered two it could not build, and every generation naming one was silently dropped.
        ALLOWED_VERIFIERS.forEach {
            assertNotNull(
                "$it is offered but cannot be built",
                toTemplate(Generated(verifier = it, minutes = 30, steps = 5000, metres = 2000, title = "x"), "gen"),
            )
        }
    }
}
