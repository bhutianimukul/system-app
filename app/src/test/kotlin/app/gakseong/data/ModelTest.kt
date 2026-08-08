package app.gakseong.data

import gakseong.engine.HunterState
import gakseong.engine.Rank
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `snapshot round trips through the engine type`() {
        val engine = HunterState(Rank(7), daysHeldAtTier = 3, streak = 12, shields = 2, consecutiveMisses = 0)
        assertEquals(engine, HunterSnapshot.of(engine).toEngine())
    }

    @Test
    fun `state survives a json round trip`() {
        val before = SystemState(
            hunter = HunterSnapshot(rankOrdinal = 4, streak = 9),
            level = 34,
            today = Today(date = "2026-08-08", auraEarned = 640),
            onboarded = true,
        )
        val after = json.decodeFromString(SystemState.serializer(), json.encodeToString(SystemState.serializer(), before))
        assertEquals(before, after)
    }

    @Test
    fun `unknown fields from a future version do not throw`() {
        val fromFuture = """{"level":9,"somethingNew":true}"""
        assertEquals(9, json.decodeFromString(SystemState.serializer(), fromFuture).level)
    }

    @Test
    fun `rolling to the same date changes nothing`() {
        val state = SystemState(today = Today(date = "2026-08-08", auraEarned = 400))
        assertEquals(state, state.rolledTo("2026-08-08"))
    }

    @Test
    fun `rolling to a new date clears today and keeps history`() {
        val state = SystemState(
            today = Today(date = "2026-08-08", auraEarned = 400, quests = listOf(quest("q1"))),
            history = listOf(DaySettlement(date = "2026-08-07", aura = 300)),
        )
        val rolled = state.rolledTo("2026-08-09")

        assertEquals("2026-08-09", rolled.today.date)
        assertEquals(0, rolled.today.auraEarned)
        assertTrue(rolled.today.quests.isEmpty())
        assertEquals(1, rolled.history.size)
        assertNotEquals(state.today, rolled.today)
    }

    @Test
    fun `rolling from an empty state adopts the date without inventing history`() {
        val rolled = SystemState().rolledTo("2026-08-09")
        assertEquals("2026-08-09", rolled.today.date)
        assertTrue(rolled.history.isEmpty())
    }

    @Test
    fun `an out of range rank ordinal is clamped rather than throwing`() {
        // Rank's constructor requires 0..17. A corrupt or hand-edited file must not crash the app on read.
        assertEquals(Rank.MAX, HunterSnapshot(rankOrdinal = 99).toEngine().rank.ordinal)
        assertEquals(0, HunterSnapshot(rankOrdinal = -4).toEngine().rank.ordinal)
    }

    private fun quest(id: String) = QuestInstance(
        id = id, icon = "◈", title = "Focus session\n45 min", sub = "Not started",
        baseAura = 300, provability = "APP_INITIATED", state = "PENDING",
    )
}
