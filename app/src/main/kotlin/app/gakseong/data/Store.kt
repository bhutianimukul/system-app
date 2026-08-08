package app.gakseong.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate

private const val FILE_NAME = "system.json"

private val json = Json {
    // A state written by a newer build must not crash an older one, and vice versa.
    ignoreUnknownKeys = true
    encodeDefaults = true
}

object SystemSerializer : Serializer<SystemState> {

    /**
     * A getter rather than a constant, so the seed adopts the current date at the moment it is used. With a
     * fixed empty date, `rolledTo` would clear the seed's quests the instant they loaded and every screen would
     * render empty for the whole of phase 03.
     */
    override val defaultValue: SystemState
        get() = SEED.copy(today = SEED.today.copy(date = LocalDate.now().toString()))

    override suspend fun readFrom(input: InputStream): SystemState =
        try {
            json.decodeFromString(SystemState.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            // A crash loop on cold start is unrecoverable for the user, so unreadable state degrades to the
            // seed. Throwing here is what hands control to the ReplaceFileCorruptionHandler below.
            throw CorruptionException("system.json is unreadable", e)
        }

    override suspend fun writeTo(t: SystemState, output: OutputStream) {
        output.write(json.encodeToString(SystemState.serializer(), t).encodeToByteArray())
    }
}

private var instance: DataStore<SystemState>? = null

/**
 * One process-wide store. Widgets and workers run in the same process as the Activity, so they share this
 * instance; DataStore throws if the same file is opened twice.
 */
@Synchronized
fun systemStore(context: Context): DataStore<SystemState> = instance ?: DataStoreFactory.create(
    serializer = SystemSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { SystemSerializer.defaultValue },
    produceFile = { File(context.applicationContext.filesDir, FILE_NAME) },
).also { instance = it }
