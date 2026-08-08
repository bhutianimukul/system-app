package app.gakseong

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/** Proves the serialization plugin is applied and the JVM test source set runs. Nothing more. */
class BuildSanityTest {

    @Serializable
    private data class Probe(val n: Int, val s: String)

    @Test
    fun `serialization plugin is applied`() {
        val json = Json.encodeToString(Probe.serializer(), Probe(7, "system"))
        assertEquals("""{"n":7,"s":"system"}""", json)
        assertEquals(Probe(7, "system"), Json.decodeFromString(Probe.serializer(), json))
    }
}
