package app.gakseong.sense

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Provenance is what lets the sensor tier reach S-rank. If self-written records ever counted, a quest could be
 * proven by the app that issued it.
 */
class ProvenanceTest {

    private val self = "app.gakseong"

    @Test
    fun `records from real providers are summed`() {
        val (total, origins) = trusted(
            listOf(OriginedValue("com.strava", 4000.0), OriginedValue("com.google.android.apps.fitness", 2000.0)),
            self,
        )
        assertEquals(6000.0, total, 0.0)
        assertEquals(setOf("com.strava", "com.google.android.apps.fitness"), origins)
    }

    @Test
    fun `a record this app wrote itself proves nothing`() {
        val (total, origins) = trusted(
            listOf(OriginedValue(self, 10_000.0), OriginedValue("com.strava", 1000.0)),
            self,
        )
        assertEquals(1000.0, total, 0.0)
        assertFalse(self in origins)
    }

    @Test
    fun `an all self written day sums to zero rather than to its claim`() {
        val (total, origins) = trusted(listOf(OriginedValue(self, 50_000.0)), self)
        assertEquals(0.0, total, 0.0)
        assertTrue(origins.isEmpty())
    }

    @Test
    fun `no records is zero and not a crash`() {
        val (total, origins) = trusted(emptyList(), self)
        assertEquals(0.0, total, 0.0)
        assertTrue(origins.isEmpty())
    }

    @Test
    fun `the same provider writing twice counts twice but is named once`() {
        val (total, origins) = trusted(
            listOf(OriginedValue("com.strava", 1500.0), OriginedValue("com.strava", 2500.0)),
            self,
        )
        assertEquals(4000.0, total, 0.0)
        assertEquals(setOf("com.strava"), origins)
    }

    @Test
    fun `unavailable is distinguishable from a zero day`() {
        // §Verifiers: no grant means the objective is never drawn. That is not the same as failing it.
        assertFalse(HealthReading.Unavailable.available)
        assertEquals(0L, HealthReading.Unavailable.steps)
        assertTrue(HealthReading().available)
    }
}
