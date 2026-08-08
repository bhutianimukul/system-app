package app.gakseong.data

import android.content.Context
import androidx.datastore.core.DataStore
import app.gakseong.quest.Readings
import app.gakseong.quest.tick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * The single source of truth. Screens read [state]; anything that changes it goes through [update].
 *
 * ponytail: an object rather than a DI graph. There is one of these for the process lifetime, and forty-eight
 * screens would otherwise mean forty-eight near-empty ViewModels. Introduce Hilt only if a second implementation
 * ever needs to exist.
 */
object Repo {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(SystemSerializer.defaultValue)

    /** Never suspends and never blocks the first frame. Reads the seed until disk arrives, then the real state. */
    val state: StateFlow<SystemState> = _state.asStateFlow()

    private var store: DataStore<SystemState>? = null

    @Synchronized
    fun init(context: Context) {
        if (store != null) return
        val opened = systemStore(context)
        store = opened
        scope.launch {
            opened.data.collect { loaded ->
                // Reading state whose date is not today is how the day boundary is detected. No alarm, no
                // receiver, no scheduled work: the next read after midnight does it.
                val rolled = loaded.rolledTo(today())
                if (rolled != loaded) opened.updateData { rolled } else _state.value = loaded
            }
        }
    }

    suspend fun update(block: (SystemState) -> SystemState) {
        val opened = store ?: return
        opened.updateData { block(it) }
    }

    /**
     * The last sensor readings, kept so an answer can be re-scored without two fresh queries.
     *
     * Answering a declared quest changes nothing a sensor can see, so re-reading UsageStats and Health Connect
     * on a button tap would be two IPCs to learn what did not change.
     */
    @Volatile
    var lastReadings: Readings = Readings()
        private set

    /** Called by whoever just gathered. Keeps [lastReadings] honest without making it a second source of truth. */
    fun remember(readings: Readings) {
        lastReadings = readings
    }

    /**
     * Answer a declared quest and re-score the day immediately.
     *
     * Without the re-score the answer lands on disk and the screen keeps saying "Answer when it expires" until
     * something else happens to tick. Verified: the aura moved only after a kill and relaunch.
     */
    suspend fun answerDeclared(questId: String, yes: Boolean) {
        update { state ->
            val answered = state.copy(today = state.today.copy(declared = state.today.declared + (questId to yes)))
            tick(answered, lastReadings, today(), endOfDayMs())
        }
    }

    /** Milliseconds at the end of the current local day. What a bonus expires at. */
    fun endOfDayMs(): Long =
        LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    /** Local date as an ISO string. The day boundary is the device's, which is the one the user experiences. */
    fun today(): String = LocalDate.now().toString()
}
