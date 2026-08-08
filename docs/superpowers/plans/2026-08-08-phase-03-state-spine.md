# Phase 03 — The State Spine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the forty-eight built screens a real state layer, real persistence and real navigation, so every number on screen comes from disk instead of a literal.

**Architecture:** One `@Serializable SystemState` persisted as JSON through `DataStore<SystemState>`. A single `Repo` object exposes it as a `StateFlow` and is read by screens through a `LocalSystem` CompositionLocal, matching the ambient-state convention the codebase already uses for `LocalPalette` / `LocalMetrics` / `LocalType` / `LocalHunterClass`. Navigation moves from an intent extra to `navigation-compose`, keeping the intent extra as a debug start-destination override so the screenshot tooling survives.

**Tech Stack:** Kotlin 2.1.0, Compose BOM 2025.01.00, `kotlinx.serialization` 1.7.3, `androidx.datastore:datastore` 1.1.1, `androidx.navigation:navigation-compose` 2.8.5, JUnit 4.13.2 for JVM unit tests.

## Global Constraints

Every task's requirements implicitly include this section. Values are copied verbatim from `CLAUDE.md` and the spec.

- `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`, `JavaVersion.VERSION_17`, `jvmToolchain(17)`.
- **The engine stays pure.** `kotlinc engine/*.kt -include-runtime -d /tmp/engine.jar` must keep working. No `@Serializable`, no Android import, and no new dependency may enter `engine/`.
- **Layer order is z-index, not markup order:** `Bg`, `Bg2`, `Aura`, `Art`, `Shade`, `Grain`, `TopFade`, `Body`, `BottomNav`. Never reorder these when editing a screen.
- **The app calls itself "The System"** in every sentence. `Gakseong` appears only as the wordmark on the splash and the Home header.
- **Nothing from the private track leaves the device**, and it emits no analytics event of any kind.
- **Never render a package name or a duration** into anything shareable.
- **The daily aura cap is a safety property.** No code in this phase may raise it.
- Mark deliberate simplifications with a `ponytail:` comment naming the ceiling and the upgrade path.
- Docs and comments in this repo: keep em dashes under 5 per 1,000 words, and use balanced negation ("X, not Y") only where the contrast is the actual claim.

## File Structure

| file | responsibility |
|---|---|
| `app/src/main/kotlin/app/gakseong/data/Model.kt` | every `@Serializable` type, plus pure day-rollover logic |
| `app/src/main/kotlin/app/gakseong/data/Store.kt` | `DataStore<SystemState>`, the JSON serializer, corruption fallback |
| `app/src/main/kotlin/app/gakseong/data/Repo.kt` | the `StateFlow` singleton and mutation functions |
| `app/src/main/kotlin/app/gakseong/data/Seed.kt` | the demo `SystemState` a fresh install starts from |
| `app/src/main/kotlin/app/gakseong/App.kt` | `Application` subclass that initialises `Repo` |
| `app/src/main/kotlin/app/gakseong/ui/Local.kt` | `LocalSystem` CompositionLocal |
| `app/src/main/kotlin/app/gakseong/ui/Nav.kt` | the nav graph, the route table, `Dest` |
| `app/src/test/kotlin/app/gakseong/data/ModelTest.kt` | rollover and serialization round-trip |
| `critic/check.sh` | build, engine asserts, route smoke test, placeholder audit |
| `critic/allowlist.txt` | strings that are legitimately fixed System script |
| `CRITIC.md` | generated inventory: promised / exists / wired |

Modified: `app/build.gradle.kts`, `build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `MainActivity.kt`, `ui/Kit.kt` (BottomNav gains a click handler), and all 25 screen files.

---

### Task 1: Dependencies and the JVM test source set

**Files:**
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`
- Create: `app/src/test/kotlin/app/gakseong/BuildSanityTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: the `kotlinx.serialization` plugin, `androidx.datastore:datastore`, `androidx.navigation:navigation-compose` and a working `./gradlew testDebugUnitTest` for every later task.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/kotlin/app/gakseong/BuildSanityTest.kt`:

```kotlin
package app.gakseong

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/** Proves the serialization plugin is applied and the JVM test source set runs. Nothing more. */
class BuildSanityTest {

    @Serializable
    private data class Probe(val n: Int, val s: String)

    @Test
    fun `serialization plugin is applied`() {
        val json = Json.encodeToString(Probe(7, "system"))
        assertEquals("""{"n":7,"s":"system"}""", json)
        assertEquals(Probe(7, "system"), Json.decodeFromString<Probe>(json))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew testDebugUnitTest
```

Expected: FAIL. `Unresolved reference: kotlinx` and `Unresolved reference: org.junit`.

- [ ] **Step 3: Add the plugin and dependencies**

In `build.gradle.kts`, add to the `plugins` block:

```kotlin
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
```

In `app/build.gradle.kts`, add to the `plugins` block:

```kotlin
    id("org.jetbrains.kotlin.plugin.serialization")
```

Add inside `android { }`, after the `buildFeatures` block:

```kotlin
    sourceSets["test"].kotlin.srcDir("src/test/kotlin")

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
```

Add to `dependencies { }`:

```kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("androidx.datastore:datastore:1.1.1")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    testImplementation("junit:junit:4.13.2")
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew testDebugUnitTest
```

Expected: PASS, `BuildSanityTest > serialization plugin is applied PASSED`.

- [ ] **Step 5: Confirm the engine invariant still holds**

```bash
kotlinc engine/*.kt -include-runtime -d /tmp/engine.jar && java -cp /tmp/engine.jar gakseong.engine.AuraSelfCheckKt
```

Expected: the existing 51 checks pass. If this fails, a dependency has leaked into `engine/` and the change must be reverted.

- [ ] **Step 6: Commit**

```bash
git add build.gradle.kts app/build.gradle.kts app/src/test
git commit -m "Serialization, DataStore, navigation, and a JVM test source set

The engine keeps its own assert-based check runnable by kotlinc, so the new
JUnit source set covers only the Android-side pure logic."
```

---

### Task 2: The domain model

**Files:**
- Create: `app/src/main/kotlin/app/gakseong/data/Model.kt`
- Create: `app/src/test/kotlin/app/gakseong/data/ModelTest.kt`

**Interfaces:**
- Consumes: `gakseong.engine.HunterState`, `gakseong.engine.Rank` from Task 1's unchanged engine.
- Produces:
  - `SystemState(hunter, level, today, profile, settings, history, onboarded)`
  - `HunterSnapshot(rankOrdinal, daysHeldAtTier, streak, shields, consecutiveMisses)` with `fun toEngine(): HunterState` and `companion object { fun of(state: HunterState): HunterSnapshot }`
  - `Today(date, quests, auraEarned, bonus)`, `QuestInstance`, `Bonus`, `Profile`, `Settings`, `DaySettlement`
  - `fun SystemState.rolledTo(date: String): SystemState`

**Why a snapshot instead of annotating the engine:** `@Serializable` needs the compiler plugin on whatever module declares the class. Annotating `HunterState` would mean `kotlinc engine/*.kt` no longer compiles, which is an invariant `CLAUDE.md` states explicitly. A five-field mirror with two conversion functions costs twelve lines and keeps the engine pure.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/kotlin/app/gakseong/data/ModelTest.kt`:

```kotlin
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
        val after = json.decodeFromString<SystemState>(json.encodeToString(SystemState.serializer(), before))
        assertEquals(before, after)
    }

    @Test
    fun `unknown fields from a future version do not throw`() {
        val fromFuture = """{"level":9,"somethingNew":true}"""
        assertEquals(9, json.decodeFromString<SystemState>(fromFuture).level)
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

    private fun quest(id: String) = QuestInstance(
        id = id, icon = "◈", title = "Focus session\n45 min", sub = "Not started",
        baseAura = 300, provability = "APP_INITIATED", state = "PENDING",
    )
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew testDebugUnitTest --tests '*ModelTest*'
```

Expected: FAIL, `Unresolved reference: SystemState`.

- [ ] **Step 3: Write the model**

Create `app/src/main/kotlin/app/gakseong/data/Model.kt`:

```kotlin
package app.gakseong.data

import gakseong.engine.HunterState
import gakseong.engine.Rank
import kotlinx.serialization.Serializable

// Everything the app persists, in one file. The engine stays pure, so its HunterState is mirrored here rather
// than annotated: @Serializable needs the compiler plugin on the declaring module, and CLAUDE.md requires
// `kotlinc engine/*.kt` to keep working.

/**
 * The whole of what the app knows, persisted as one JSON document.
 *
 * ponytail: one document rewritten per settle rather than a relational store. A year of play is ~365 small
 * history rows, so this is microseconds. Move to Room if the report screen or the private journal measures slow.
 */
@Serializable
data class SystemState(
    val hunter: HunterSnapshot = HunterSnapshot(),
    /** §Economy: private, only rises. Absent from the engine because it gates nothing. */
    val level: Int = 1,
    val today: Today = Today(),
    val profile: Profile = Profile(),
    val settings: Settings = Settings(),
    val history: List<DaySettlement> = emptyList(),
    val onboarded: Boolean = false,
)

/** The engine's [HunterState] as five integers. [Rank] serializes as its ordinal, so its `require` still guards. */
@Serializable
data class HunterSnapshot(
    val rankOrdinal: Int = 0,
    val daysHeldAtTier: Int = 0,
    val streak: Int = 0,
    val shields: Int = 0,
    val consecutiveMisses: Int = 0,
) {
    fun toEngine(): HunterState = HunterState(
        rank = Rank(rankOrdinal.coerceIn(0, Rank.MAX)),
        daysHeldAtTier = daysHeldAtTier,
        streak = streak,
        shields = shields,
        consecutiveMisses = consecutiveMisses,
    )

    companion object {
        fun of(state: HunterState) = HunterSnapshot(
            rankOrdinal = state.rank.ordinal,
            daysHeldAtTier = state.daysHeldAtTier,
            streak = state.streak,
            shields = state.shields,
            consecutiveMisses = state.consecutiveMisses,
        )
    }
}

/** [date] is an ISO local date. A mismatch against the current date is how a day boundary is detected. */
@Serializable
data class Today(
    val date: String = "",
    val quests: List<QuestInstance> = emptyList(),
    val auraEarned: Int = 0,
    val bonus: Bonus? = null,
)

/**
 * One issued quest. [provability] and [state] are stored as enum names rather than as the enums themselves,
 * because `Provability` lives in the unannotated engine.
 */
@Serializable
data class QuestInstance(
    val id: String,
    val icon: String,
    val title: String,
    val sub: String,
    val baseAura: Int,
    val provability: String,
    val state: String,
    val wide: Boolean = false,
)

/** §Economy: one per day, spawned at random, raises today's ceiling by 120 to 450. */
@Serializable
data class Bonus(
    val title: String,
    val detail: String,
    val aura: Int,
    val expiresAtEpochMs: Long,
)

@Serializable
data class Profile(
    val hunterClass: String = "ASSASSIN",
    val intent: List<String> = emptyList(),
    /** Never leaves the device. §10. */
    val watchedPackages: List<String> = emptyList(),
)

@Serializable
data class Settings(
    val analytics: Boolean = true,
    val dnd: Boolean = false,
    val speedBump: Boolean = false,
    val privateTrack: Boolean = false,
    val nightGateStart: String = "00:30",
    val nightGateEnd: String = "06:00",
    val whisper: Boolean = false,
)

@Serializable
data class DaySettlement(
    val date: String,
    val aura: Int,
    val rankCredit: Int = 0,
    val overflow: Int = 0,
    val outcome: String = "IN_BAND",
    val penalty: String? = null,
)

/**
 * Adopt [date] as today. Settling the day that just ended is phase 05's job; this only detects the boundary and
 * clears the slate, so a rollover can never silently award or penalise.
 */
fun SystemState.rolledTo(date: String): SystemState =
    if (today.date == date) this else copy(today = Today(date = date))
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew testDebugUnitTest --tests '*ModelTest*'
```

Expected: PASS, six tests.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/data/Model.kt app/src/test/kotlin/app/gakseong/data/ModelTest.kt
git commit -m "The domain model, and why the engine keeps no annotation

@Serializable needs the compiler plugin on the declaring module. Annotating
HunterState would break \`kotlinc engine/*.kt\`, which CLAUDE.md states as an
invariant, so a five-field mirror carries it across instead.

Rollover clears the slate and nothing else. Settling the day that ended is
phase 05, so a date change can never silently award or penalise."
```

---

### Task 3: The store

**Files:**
- Create: `app/src/main/kotlin/app/gakseong/data/Store.kt`
- Create: `app/src/main/kotlin/app/gakseong/data/Seed.kt`

**Interfaces:**
- Consumes: `SystemState` and every type from Task 2.
- Produces:
  - `fun systemStore(context: Context): DataStore<SystemState>`
  - `object SystemSerializer : Serializer<SystemState>`
  - `val SEED: SystemState`

There is no unit test here. This is framework I/O whose only logic is the corruption fallback, and that path is covered by the route smoke test in Task 14. The serializer's actual encoding is already tested in Task 2.

- [ ] **Step 1: Write the seed**

Create `app/src/main/kotlin/app/gakseong/data/Seed.kt`:

```kotlin
package app.gakseong.data

// What a fresh install starts from, and what every screen renders against until onboarding writes over it.
//
// ponytail: the five quests here are hand-written placeholders standing in for the phase-05 quest bank. They
// exist so the screens have shaped data to read during phase 03. CRITIC.md tracks them as `exists, not wired`
// until quest/Bank.kt replaces them.

/** The Home screen's own design-page day, so the first launch matches the reference art exactly. */
val SEED = SystemState(
    hunter = HunterSnapshot(rankOrdinal = 4, daysHeldAtTier = 3, streak = 14, shields = 1, consecutiveMisses = 0),
    level = 34,
    today = Today(
        date = "",
        auraEarned = 640,
        quests = listOf(
            QuestInstance(
                id = "screen-off", icon = "🌙", title = "Screen off\n45 min", sub = "Verified",
                baseAura = 180, provability = "SENSOR", state = "DONE",
            ),
            QuestInstance(
                id = "scroll-cap", icon = "◉", title = "Scroll under\n90 min", sub = "41 min used",
                baseAura = 220, provability = "SENSOR", state = "DONE",
            ),
            QuestInstance(
                id = "steps", icon = "⚡", title = "6,000\nsteps", sub = "Health Connect",
                baseAura = 240, provability = "SENSOR", state = "DONE",
            ),
            QuestInstance(
                id = "focus", icon = "◈", title = "Focus session\n45 min", sub = "Not started",
                baseAura = 300, provability = "APP_INITIATED", state = "PENDING",
            ),
            QuestInstance(
                id = "night-gate", icon = "☾", title = "Night gate · 00:30 to 06:00", sub = "Pending · tonight",
                baseAura = 260, provability = "SENSOR", state = "PENDING", wide = true,
            ),
        ),
        bonus = Bonus(
            title = "Phone down · 2 hours",
            detail = "Start within the hour or it is gone",
            aura = 400,
            expiresAtEpochMs = 0L,
        ),
    ),
    profile = Profile(hunterClass = "ASSASSIN"),
    onboarded = false,
)
```

- [ ] **Step 2: Write the store**

Create `app/src/main/kotlin/app/gakseong/data/Store.kt`:

```kotlin
package app.gakseong.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

private const val FILE_NAME = "system.json"

private val json = Json {
    // A state written by a newer build must not crash an older one, and vice versa.
    ignoreUnknownKeys = true
    encodeDefaults = true
}

object SystemSerializer : Serializer<SystemState> {

    override val defaultValue: SystemState = SEED

    override suspend fun readFrom(input: InputStream): SystemState =
        try {
            json.decodeFromString(SystemState.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            // A crash loop on cold start is unrecoverable for the user, so unreadable state degrades to the
            // seed rather than throwing. DataStore turns this into a replaceFileCorruptionHandler call.
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
    corruptionHandler = androidx.datastore.core.handlers.ReplaceFileCorruptionHandler { SEED },
    produceFile = { File(context.applicationContext.filesDir, FILE_NAME) },
).also { instance = it }
```

- [ ] **Step 3: Verify it compiles**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/data/Store.kt app/src/main/kotlin/app/gakseong/data/Seed.kt
git commit -m "One JSON document on disk, and a seed to render against

Unreadable state falls back to the seed rather than throwing. A crash loop on
cold start is the one failure a user cannot get out of.

The seed's five quests are placeholders for the phase-05 bank. CRITIC.md tracks
them as exists-not-wired so they cannot be mistaken for the real thing."
```

---

### Task 4: The repository and the Application

**Files:**
- Create: `app/src/main/kotlin/app/gakseong/data/Repo.kt`
- Create: `app/src/main/kotlin/app/gakseong/App.kt`
- Create: `app/src/main/kotlin/app/gakseong/ui/Local.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: `systemStore(context)`, `SEED`, `SystemState`, `rolledTo(date)`.
- Produces:
  - `object Repo` with `val state: StateFlow<SystemState>`, `fun init(context: Context)`, `suspend fun update(block: (SystemState) -> SystemState)`, `fun today(): String`
  - `val LocalSystem: ProvidableCompositionLocal<SystemState>`

- [ ] **Step 1: Write the repository**

Create `app/src/main/kotlin/app/gakseong/data/Repo.kt`:

```kotlin
package app.gakseong.data

import android.content.Context
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
    private val _state = MutableStateFlow(SEED)
    val state: StateFlow<SystemState> = _state.asStateFlow()

    private lateinit var store: androidx.datastore.core.DataStore<SystemState>
    private var started = false

    @Synchronized
    fun init(context: Context) {
        if (started) return
        started = true
        store = systemStore(context)
        scope.launch {
            store.data.collect { loaded ->
                // Reading state whose date is not today is how the boundary is detected. No alarm, no receiver.
                val rolled = loaded.rolledTo(today())
                if (rolled != loaded) store.updateData { rolled } else _state.value = loaded
            }
        }
    }

    suspend fun update(block: (SystemState) -> SystemState) {
        store.updateData { block(it) }
    }

    /** Local date as an ISO string. The day boundary is the device's, which is what the user experiences. */
    fun today(): String = LocalDate.now().toString()
}
```

- [ ] **Step 2: Write the Application**

Create `app/src/main/kotlin/app/gakseong/App.kt`:

```kotlin
package app.gakseong

import android.app.Application
import app.gakseong.data.Repo

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repo.init(this)
    }
}
```

- [ ] **Step 3: Write the CompositionLocal**

Create `app/src/main/kotlin/app/gakseong/ui/Local.kt`:

```kotlin
package app.gakseong.ui

import androidx.compose.runtime.compositionLocalOf
import app.gakseong.data.SEED
import app.gakseong.data.SystemState

/**
 * Ambient state, following the convention `LocalPalette`, `LocalMetrics`, `LocalType` and `LocalHunterClass`
 * already set in this codebase. A screen reads `LocalSystem.current` the same way it reads its palette.
 *
 * Defaults to [SEED] so a `@Preview` renders without a running Application.
 */
val LocalSystem = compositionLocalOf { SEED }
```

- [ ] **Step 4: Register the Application**

In `app/src/main/AndroidManifest.xml`, add `android:name=".App"` as the first attribute of `<application>`:

```xml
    <application
        android:name=".App"
        android:label="Gakseong"
        android:supportsRtl="true"
        android:theme="@style/Theme.Gakseong">
```

- [ ] **Step 5: Verify it builds and runs**

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n app.gakseong/.MainActivity
adb logcat -d -t 200 | grep -i "FATAL\|AndroidRuntime" || echo "no crash"
```

Expected: `BUILD SUCCESSFUL`, the app launches to Home, `no crash`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/data/Repo.kt app/src/main/kotlin/app/gakseong/App.kt \
        app/src/main/kotlin/app/gakseong/ui/Local.kt app/src/main/AndroidManifest.xml
git commit -m "One StateFlow, read the way every other ambient value is read

LocalSystem sits beside LocalPalette, LocalMetrics, LocalType and
LocalHunterClass rather than introducing a second convention next to them.

No ViewModels: forty-eight screens would be forty-eight near-empty files, and
there is exactly one of this state for the process lifetime."
```

---

### Task 5: Navigation

**Files:**
- Create: `app/src/main/kotlin/app/gakseong/ui/Nav.kt`
- Modify: `app/src/main/kotlin/app/gakseong/MainActivity.kt`
- Modify: `app/src/main/kotlin/app/gakseong/ui/Kit.kt:708` (`BottomNav`)

**Interfaces:**
- Consumes: every `*Screen()` composable, `LocalSystem`, `Repo.state`.
- Produces:
  - `@Composable fun GakseongNav(start: String)`
  - `val LocalNav: ProvidableCompositionLocal<(String) -> Unit>` so any screen can navigate without threading a controller
  - `BottomNav(active: Int, onSelect: (Int) -> Unit = {})`

- [ ] **Step 1: Give BottomNav a click handler**

In `app/src/main/kotlin/app/gakseong/ui/Kit.kt`, change the signature at line 708 and make each item clickable. Replace:

```kotlin
fun BoxScope.BottomNav(active: Int) {
```

with:

```kotlin
fun BoxScope.BottomNav(active: Int, onSelect: (Int) -> Unit = {}) {
```

Then inside `items.forEachIndexed { i, (glyph, label) ->`, add the click modifier to the `Column`. Change:

```kotlin
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(m.d(3.2)),
                ) {
```

to:

```kotlin
                Column(
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(i) },
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(m.d(3.2)),
                ) {
```

Add these imports to `Kit.kt` if absent:

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
```

The ripple is suppressed with `indication = null` because the bar draws its own active glow, and a default ripple over that gradient reads as a smudge.

- [ ] **Step 2: Write the nav graph**

Create `app/src/main/kotlin/app/gakseong/ui/Nav.kt`:

```kotlin
package app.gakseong.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.gakseong.data.Repo
import app.gakseong.ui.screens.*

/** Navigate by route name from anywhere, without threading a NavController through forty-eight signatures. */
val LocalNav = compositionLocalOf<(String) -> Unit> { {} }

/** The five bottom-nav destinations, in bar order. Index matches `BottomNav(active = ...)`. */
val NAV_TABS = listOf("home", "gates", "raidhub", "guild", "profile")

/**
 * Every route the app has. The intent extra in [app.gakseong.MainActivity] picks the start destination from
 * this same table, so `adb shell am start ... --es screen <name>` keeps working for the screenshot sheet.
 */
@Composable
fun GakseongNav(start: String) {
    val nav = rememberNavController()
    val state by Repo.state.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalSystem provides state,
        LocalNav provides { route -> nav.navigate(route) { launchSingleTop = true } },
    ) {
        NavHost(navController = nav, startDestination = start) {
            composable("home") { HomeScreen() }
            composable("focus") { FocusScreen() }
            composable("ceremony") { CeremonyScreen() }
            composable("raid") { RaidScreen() }
            composable("raidhub") { RaidHubScreen() }
            composable("runraid") { RunRaidScreen() }
            composable("runsettle") { RunSettleScreen() }
            composable("share") { ShareScreen() }
            composable("shareraid") { ShareScreen(ShareMoment.RAID) }
            composable("arise") { AriseScreen() }
            composable("league") { LeagueScreen() }
            composable("gate") { GateScreen() }
            composable("break") { BreakScreen() }
            composable("invite") { InviteScreen() }
            composable("guild") { GuildScreen() }
            composable("feed") { FeedScreen() }
            composable("refer") { ReferScreen() }
            composable("soon") { SoonScreen() }
            composable("profile") { ProfileScreen() }
            composable("private") { PrivateScreen() }
            composable("report") { ReportScreen() }
            composable("splash") { SplashScreen() }
            composable("welcome") { WelcomeScreen() }
            composable("perms") { PermsScreen() }
            composable("diag") { RealityScreen() }
            composable("apps") { AppsScreen() }
            composable("contract") { ContractScreen() }
            composable("intent") { IntentScreen() }
            composable("class") { AwakeningScreen() }
            composable("stage") { StagedScreen() }
            composable("privset") { PrivateSetupScreen() }
            composable("thresh") { ThresholdScreen() }
            composable("weights") { WeightsScreen() }
            composable("newapp") { NewAppScreen() }
            composable("read") { ReaderScreen() }
            composable("shadows") { ShadowsScreen() }
            composable("complete") { CompleteScreen() }
            composable("monarch") { MonarchScreen() }
            composable("gates") { GatesScreen() }
            composable("aikey") { AiGateScreen() }
            composable("bonus") { BonusScreen() }
            composable("widget") { WidgetScreen() }
            composable("pact") { PactScreen() }
            composable("contain") { ContainScreen() }
            composable("chat") { ChatScreen() }
            composable("store") { StoreScreen() }
            composable("type") { TypeSpecimenScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
```

Add to `app/build.gradle.kts` dependencies (needed by `collectAsStateWithLifecycle`):

```kotlin
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
```

- [ ] **Step 3: Rewrite MainActivity**

Replace the whole body of `app/src/main/kotlin/app/gakseong/MainActivity.kt` with:

```kotlin
package app.gakseong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.gakseong.data.Repo
import app.gakseong.ui.GakseongNav
import app.gakseong.ui.theme.GakseongTheme
import app.gakseong.ui.theme.HunterClass

/**
 * Navigation lives in `ui/Nav.kt`. The intent extra survives as a debug start-destination override, because
 * `screenshots/index.html` drives one route per launch:
 *
 * `adb shell am start -n app.gakseong/.MainActivity --es screen ceremony --es class ranger`
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The platform splash, taken over rather than replaced. A custom splash Activity on top of this is the
        // single most common way this ships wrong, and it produces a visible double-splash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Art bleeds under the status bar exactly as the design page shows it, so the app draws edge to edge.
        enableEdgeToEdge()

        val override = intent?.getStringExtra("screen")
        val start = override ?: if (Repo.state.value.onboarded) "home" else "welcome"

        val hunter = intent?.getStringExtra("class")
            ?.let { name -> HunterClass.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
            ?: HunterClass.entries.firstOrNull { it.name == Repo.state.value.profile.hunterClass }
            ?: HunterClass.ASSASSIN

        setContent {
            // Dark is the design's default. The light theme is real rather than an inversion; it follows the
            // system once onboarding owns this choice.
            GakseongTheme(hunterClass = hunter, dark = true) {
                GakseongNav(start)
            }
        }
    }
}
```

`ScreenTransition` is no longer called here. `navigation-compose` owns the transition now, and Task 6 restores the design's `.4s cubic-bezier(.16,1,.3,1)` curve on the NavHost.

- [ ] **Step 4: Verify every route still launches**

```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
for s in home focus ceremony raid raidhub runraid runsettle share shareraid arise league gate break \
         invite guild feed refer soon profile private report splash welcome perms diag apps contract \
         intent class stage privset thresh weights newapp read shadows complete monarch gates aikey \
         bonus widget pact contain chat store type settings; do
  adb shell am start -n app.gakseong/.MainActivity --es screen $s > /dev/null
  sleep 1
done
adb logcat -d | grep -c "FATAL EXCEPTION"
```

Expected: `BUILD SUCCESSFUL`, and the grep count is `0`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/Nav.kt app/src/main/kotlin/app/gakseong/MainActivity.kt \
        app/src/main/kotlin/app/gakseong/ui/Kit.kt app/build.gradle.kts
git commit -m "A real back stack, and a bottom bar that goes somewhere

The intent extra stays as a debug start-destination override, because the
screenshot contact sheet drives one route per launch and that is the tool this
project verifies screens with.

BottomNav had no click handling at all. It has one now, with the ripple
suppressed: the bar draws its own active glow and a default ripple over that
gradient reads as a smudge."
```

---

### Task 6: The transition curve on the NavHost

**Files:**
- Modify: `app/src/main/kotlin/app/gakseong/ui/Nav.kt`
- Modify: `app/src/main/kotlin/app/gakseong/ui/Motion.kt`

**Interfaces:**
- Consumes: `animationsEnabled(context)`, `rememberHaptics()` from `Motion.kt`.
- Produces: `NavHost` transitions matching the design page's `pgIn` keyframe, and a tick on every route change.

- [ ] **Step 1: Export the curve from Motion.kt**

In `app/src/main/kotlin/app/gakseong/ui/Motion.kt`, change the two private declarations to internal so `Nav.kt` can use the same numbers rather than copying them:

```kotlin
internal val PgIn = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
internal const val PG_IN_MS = 400
```

- [ ] **Step 2: Apply them to the NavHost**

In `Nav.kt`, add these imports:

```kotlin
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
```

Replace the `NavHost(navController = nav, startDestination = start) {` line with:

```kotlin
        val context = LocalContext.current
        val animate = remember { animationsEnabled(context) }
        val haptics = rememberHaptics()
        val entry by nav.currentBackStackEntryAsState()
        LaunchedEffect(entry?.destination?.route) { haptics.tick() }

        NavHost(
            navController = nav,
            startDestination = start,
            // The design page's own transition: `.4s cubic-bezier(.16,1,.3,1)` with an 8px rise.
            enterTransition = {
                if (!animate) fadeIn(tween(0))
                else fadeIn(tween(PG_IN_MS, easing = PgIn)) + slideInVertically(tween(PG_IN_MS, easing = PgIn)) { it / 40 }
            },
            exitTransition = { fadeOut(tween(if (animate) PG_IN_MS / 2 else 0)) },
            popEnterTransition = { fadeIn(tween(if (animate) PG_IN_MS else 0)) },
            popExitTransition = { fadeOut(tween(if (animate) PG_IN_MS / 2 else 0)) },
        ) {
```

Add the import `androidx.navigation.compose.currentBackStackEntryAsState`.

Reduced motion is honoured by collapsing every duration to zero rather than by skipping the animation, which keeps one code path.

- [ ] **Step 3: Verify**

```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell settings put global animator_duration_scale 0
adb shell am start -n app.gakseong/.MainActivity
adb logcat -d -t 100 | grep -c "FATAL EXCEPTION"
adb shell settings put global animator_duration_scale 1
```

Expected: `0` crashes with animations off.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/Nav.kt app/src/main/kotlin/app/gakseong/ui/Motion.kt
git commit -m "The design's own curve, now on the NavHost

Reduced motion collapses every duration to zero rather than branching to a
second code path, so there is one transition to keep correct instead of two."
```

---

## Screen rewiring, Tasks 7 to 13

Every screen task follows the same shape, so the shape is stated once here and each task lists only its own mappings.

**The rule.** A literal that represents *state* becomes a read from `LocalSystem.current`. A literal that is *fixed System script* or a *fixed label* stays exactly as it is. Concretely: `Tag("D · III")` is state; `Eye("Daily Quest")` is a label; `Text("Start within the hour or it is gone")` is script.

**The import added to every rewired screen:**

```kotlin
import app.gakseong.ui.LocalSystem
```

**The line added at the top of every rewired composable, beside the existing `val p = LocalPalette.current`:**

```kotlin
    val sys = LocalSystem.current
```

**Per-task verification, identical every time:**

```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
# then, for each route this task touched:
adb shell am start -n app.gakseong/.MainActivity --es screen <route>
adb logcat -d -t 200 | grep -c "FATAL EXCEPTION"   # must be 0
```

**Never reorder the layer calls.** `Bg`, `Bg2`, `Aura`, `Art`, `Shade`, `Grain`, `TopFade`, `Body`, `BottomNav`, in that sequence, always.

---

### Task 7: Home

**Files:**
- Modify: `app/src/main/kotlin/app/gakseong/ui/screens/Home.kt`

**Interfaces:**
- Consumes: `LocalSystem`, `LocalNav`, `NAV_TABS`, `SystemState`, `QuestInstance`, `HunterSnapshot.toEngine()`, and `gakseong.engine.bandFor`.
- Produces: the worked example every later screen task follows.

- [ ] **Step 1: Map the literals**

| current line | becomes |
|---|---|
| `Tag("D · III", ...)` | `Tag(rank.label, ...)` |
| `Tag("LV 34 · 14d", ...)` | `Tag("LV ${sys.level} · ${sys.hunter.streak}d", ...)` |
| `XlNumber("640")` | `XlNumber(sys.today.auraEarned.toString())` |
| `Tag("560 to D · II", ...)` | `Tag("$toNext to ${next.label}", ...)` |
| `Meter(fill = 0.53f, marker = 0.33f)` | `Meter(fill = fill, marker = marker)` |
| `Tag("Threshold cleared", t.key)` | `Tag(if (cleared) "Threshold cleared" else "Below threshold", t.key)` |
| `Tag("Shadow in 3 days", t.key)` | `Tag(shieldLine, t.key)` |
| the five hard-coded `QuestCard(...)` calls | a loop over `sys.today.quests` |
| the `SystemWindow` bonus block | driven by `sys.today.bonus`, and omitted entirely when it is null |
| `BottomNav(active = 0)` | `BottomNav(active = 0, onSelect = { nav(NAV_TABS[it]) })` |

- [ ] **Step 2: Write the derived values**

Insert directly after `val hunter = LocalHunterClass.current`:

```kotlin
    val sys = LocalSystem.current
    val nav = LocalNav.current

    val engine = sys.hunter.toEngine()
    val rank = engine.rank
    val band = bandFor(rank)
    val next = if (rank.ordinal < Rank.MAX) Rank(rank.ordinal + 1) else rank
    val toNext = (bandFor(next).threshold - sys.today.auraEarned).coerceAtLeast(0)
    val cleared = sys.today.auraEarned >= band.threshold
    val fill = (sys.today.auraEarned.toFloat() / band.cap).coerceIn(0f, 1f)
    val marker = (band.threshold.toFloat() / band.cap).coerceIn(0f, 1f)
    val toShield = Balance.SHIELD_EVERY_DAYS - (sys.hunter.streak % Balance.SHIELD_EVERY_DAYS)
    val shieldLine = if (sys.hunter.shields >= Balance.MAX_SHIELDS) "Shields full" else "Shadow in $toShield days"
```

Imports to add:

```kotlin
import app.gakseong.ui.LocalNav
import app.gakseong.ui.LocalSystem
import app.gakseong.ui.NAV_TABS
import gakseong.engine.Balance
import gakseong.engine.Rank
import gakseong.engine.bandFor
```

- [ ] **Step 3: Replace the quest cards with a loop**

Replace everything from `Row(horizontalArrangement = Arrangement.spacedBy(m.d(8))) {` through the closing brace of the `QuestCard(... wide = true ...)` call with:

```kotlin
                val wide = sys.today.quests.filter { it.wide }
                val narrow = sys.today.quests.filterNot { it.wide }
                narrow.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
                        pair.forEach { q ->
                            QuestCard(
                                icon = q.icon,
                                title = q.title,
                                sub = q.sub,
                                value = "+${award(q.baseAura, Provability.valueOf(q.provability))}",
                                state = QuestState.valueOf(q.state),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // An odd count would otherwise stretch the last card to full width.
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                wide.forEach { q ->
                    QuestCard(
                        icon = q.icon,
                        title = q.title,
                        sub = q.sub,
                        value = "+${award(q.baseAura, Provability.valueOf(q.provability))}",
                        state = QuestState.valueOf(q.state),
                        wide = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
```

Imports to add:

```kotlin
import androidx.compose.foundation.layout.Spacer
import gakseong.engine.Provability
import gakseong.engine.award
```

The displayed `+180` is now `award(baseAura, provability)` rather than a literal. That is the point: the provability tier is applied by the engine, and a declared quest can never display a sensor rate.

- [ ] **Step 4: Make the bonus conditional**

Wrap the `SystemWindow { ... }` block:

```kotlin
                sys.today.bonus?.let { bonus ->
                    Gap(3.2)
                    SystemWindow {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Tag("⚡ Bonus spawned", t.tag.copy(color = p.hot))
                            Filler()
                            Tag(expiryLabel(bonus.expiresAtEpochMs), t.tag.copy(color = p.soft))
                        }
                        Gap(7.2)
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(m.d(9.6)),
                        ) {
                            Sig()
                            Column(Modifier.weight(1f)) {
                                Text(bonus.title, style = t.listItem)
                                Gap(2.2)
                                Tag(bonus.detail, t.tag.copy(letterSpacing = t.key.letterSpacing))
                            }
                            Text("+${bonus.aura}", style = t.monoSmall)
                        }
                    }
                }
```

Add this private helper at the bottom of `Home.kt`:

```kotlin
/** `expires 41:08`, or nothing at all once the clock has run out. */
private fun expiryLabel(expiresAtEpochMs: Long): String {
    val left = expiresAtEpochMs - System.currentTimeMillis()
    if (left <= 0L) return "expired"
    val minutes = left / 60_000
    return "expires %02d:%02d".format(minutes / 60, minutes % 60)
}
```

- [ ] **Step 5: Verify**

```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n app.gakseong/.MainActivity --es screen home
adb logcat -d -t 200 | grep -c "FATAL EXCEPTION"
adb exec-out screencap -p > /tmp/home.png
```

Expected: `0`, and the screenshot shows `C · III`, `LV 34 · 14d` and `640` from `SEED` rather than the old literals. The rank differs from the old hard-coded `D · III` because `SEED` sets `rankOrdinal = 4`, which is `C · III`. Change `SEED` to `rankOrdinal = 4` only if the reference screenshot must match; otherwise accept the difference and note it.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/screens/Home.kt
git commit -m "Home reads state, and the aura on a card is now the engine's answer

The +180 on a quest card was a literal. It is award(baseAura, provability) now,
so a declared quest cannot display a sensor rate no matter what the bank says.

The bonus window disappears when there is no bonus, which the static screen
could not express."
```

---

### Task 8: Onboarding

**Files:**
- Modify: `app/src/main/kotlin/app/gakseong/ui/screens/Onboarding.kt` (`SplashScreen`, `WelcomeScreen`, `PermsScreen`, `RealityScreen`, `AppsScreen`, `ContractScreen`)
- Modify: `app/src/main/kotlin/app/gakseong/ui/screens/Onboarding2.kt` (`IntentScreen`, `AwakeningScreen`, `StagedScreen`)

**Interfaces:**
- Consumes: `LocalSystem`, `LocalNav`, `Repo.update`.
- Produces: onboarding that advances through routes and writes `onboarded = true`, `profile.hunterClass` and `profile.intent`.

- [ ] **Step 1: Read the current files in full before editing**

```bash
sed -n '1,460p' app/src/main/kotlin/app/gakseong/ui/screens/Onboarding.kt
sed -n '1,290p' app/src/main/kotlin/app/gakseong/ui/screens/Onboarding2.kt
```

- [ ] **Step 2: Wire every `Cta` to a route**

The four asks run in this order, per `CLAUDE.md`: Access, Intent, Apps, Terms. The unnumbered screens follow. Wire each screen's primary `Cta` with `Modifier.clickable { nav("<next>") }`:

| screen | route | next |
|---|---|---|
| `WelcomeScreen` | `welcome` | `perms` |
| `PermsScreen` | `perms` | `intent` |
| `IntentScreen` | `intent` | `apps` |
| `AppsScreen` | `apps` | `contract` |
| `ContractScreen` | `contract` | `diag` |
| `RealityScreen` | `diag` | `class` |
| `AwakeningScreen` | `class` | `stage` |
| `StagedScreen` | `stage` | `home` |

`Cta` currently takes no `onClick`. Add one in `Kit.kt:508`:

```kotlin
fun Cta(
    text: String,
    ghost: Boolean = false,
    bad: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
```

and add `.let { if (onClick != null) it.clickable(onClick = onClick) else it }` to the outermost `Modifier` chain inside `Cta`.

- [ ] **Step 3: Persist what onboarding collects**

`AwakeningScreen` writes the derived class. `IntentScreen` writes the picked statements. `ContractScreen` is the single accept gate, so it writes `onboarded = true`.

Each write follows this shape, using `rememberCoroutineScope` since `Repo.update` is suspending:

```kotlin
    val scope = rememberCoroutineScope()
    // ...
    Cta("Accept and begin", onClick = {
        scope.launch { Repo.update { it.copy(onboarded = true) } }
        nav("diag")
    })
```

Imports: `androidx.compose.runtime.rememberCoroutineScope`, `kotlinx.coroutines.launch`, `app.gakseong.data.Repo`.

- [ ] **Step 4: Verify all nine routes**

```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
for s in splash welcome perms diag apps contract intent class stage; do
  adb shell am start -n app.gakseong/.MainActivity --es screen $s > /dev/null; sleep 1; done
adb logcat -d | grep -c "FATAL EXCEPTION"
```

Expected: `0`.

- [ ] **Step 5: Verify the gate actually gates**

```bash
adb shell pm clear app.gakseong
adb shell am start -n app.gakseong/.MainActivity
```

Expected: the app opens on `welcome`, not `home`, because `SEED.onboarded` is `false`. Walking through to `contract` and accepting, then killing and relaunching, opens on `home`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/screens/Onboarding.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Onboarding2.kt \
        app/src/main/kotlin/app/gakseong/ui/Kit.kt
git commit -m "Onboarding advances, and the accept gate writes the flag it promises

Four asks in the order CLAUDE.md sets: Access, Intent, Apps, Terms. The
unnumbered screens carry no step counter because they cost the user nothing."
```

---

### Task 9: Self — profile, report, settings, private

**Files:**
- Modify: `app/src/main/kotlin/app/gakseong/ui/screens/Profile.kt`
- Modify: `app/src/main/kotlin/app/gakseong/ui/screens/Report.kt`
- Modify: `app/src/main/kotlin/app/gakseong/ui/screens/Settings.kt`
- Modify: `app/src/main/kotlin/app/gakseong/ui/screens/Private.kt`

**Interfaces:**
- Consumes: `LocalSystem`, `Repo.update`, `ui/Charts.kt` (`AuraByDay`, `HoursHeatmap`, `DayCalendar`, `Legend`).
- Produces: settings toggles that persist, and a report driven by `sys.history`.

- [ ] **Step 1: Profile**

Replace rank, level, streak, shield and class literals with reads: `sys.hunter.toEngine().rank.label`, `sys.level`, `sys.hunter.streak`, `sys.hunter.shields`, `sys.profile.hunterClass`. The rank *title* comes from `rank.title`, never from a second hard-coded list.

- [ ] **Step 2: Report**

Feed `AuraByDay` from `sys.history.takeLast(7).map { it.aura }` rather than a literal list. When `sys.history` is empty the chart shows an empty state rather than a flat zero line, because a zero line reads as a bad week rather than as no data yet.

- [ ] **Step 3: Settings — every switch reads and writes**

Each toggle binds to a `Settings` field. The pattern, repeated per switch:

```kotlin
    val scope = rememberCoroutineScope()
    // ...
    Pill(
        text = "Analytics",
        on = sys.settings.analytics,
        modifier = Modifier.clickable {
            scope.launch { Repo.update { s -> s.copy(settings = s.settings.copy(analytics = !s.settings.analytics)) } }
        },
    )
```

`Pill` at `Kit.kt:537` takes no modifier. Add one:

```kotlin
fun Pill(text: String, on: Boolean = false, modifier: Modifier = Modifier) {
```

and thread it into the outermost `Box`/`Row`.

Field bindings: `analytics`, `dnd`, `speedBump`, `privateTrack`, `whisper`, plus `nightGateStart` and `nightGateEnd` as text.

Every term §13 promised was reversible must be reversible here. That is what makes the single accept gate at onboarding honest rather than a trap.

- [ ] **Step 4: Private**

Read `sys.settings.privateTrack` for the enabled state. **Add no analytics call of any kind to this file**, and add no share affordance. The exemption is unconditional and is not tied to the analytics toggle.

- [ ] **Step 5: Verify**

```bash
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk
for s in profile report settings private; do
  adb shell am start -n app.gakseong/.MainActivity --es screen $s > /dev/null; sleep 1; done
adb logcat -d | grep -c "FATAL EXCEPTION"
```

Expected: `0`. Then toggle a switch on `settings`, kill the app, reopen: the toggle holds.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/screens/Profile.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Report.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Settings.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Private.kt \
        app/src/main/kotlin/app/gakseong/ui/Kit.kt
git commit -m "The switches now switch, and they survive a kill

Every term §13 promised was reversible is reversible here, which is what makes a
single accept gate at onboarding an honest ask.

The private track gained no analytics call and no share affordance. It never
gets either."
```

---

### Task 10: The loop screens — focus, ceremony, arise, gate, break

**Files:**
- Modify: `Focus.kt`, `Ceremony.kt`, `Arise.kt`, `Gate.kt`, `Break.kt`

**Interfaces:**
- Consumes: `LocalSystem`, `LocalNav`, `gakseong.engine.Penalty`, `gakseong.engine.penaltyFor`, `bandFor`.
- Produces: ceremony and break screens driven by the engine's own transition types.

- [ ] **Step 1: Ceremony and Arise read the rank move**

The before and after ranks come from `sys.history` and `sys.hunter`, never from two hard-coded strings. Rank titles come from `Rank.title`.

- [ ] **Step 2: Break reads the penalty sequence**

`BreakScreen` shows exactly one penalty, chosen by `penaltyFor(sys.hunter.consecutiveMisses)`. Penalties sequence, never stack: if that function returns `null`, the screen shows the quiet-day state rather than inventing a penalty. **Every penalty state shows the road back**, and no penalty may demand physical effort.

- [ ] **Step 3: Focus reads the session length from the quest**

`FocusScreen` takes its duration from the matching `QuestInstance` in `sys.today.quests` rather than from a literal. The service itself is phase 06; this task wires the numbers only, and leaves a `ponytail:` comment saying so.

- [ ] **Step 4: Gate reads the night-gate window**

`sys.settings.nightGateStart` and `nightGateEnd` replace the hard-coded `00:30` and `06:00`.

- [ ] **Step 5: Verify**

```bash
for s in focus ceremony arise gate break; do
  adb shell am start -n app.gakseong/.MainActivity --es screen $s > /dev/null; sleep 1; done
adb logcat -d | grep -c "FATAL EXCEPTION"
```

Expected: `0`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/screens/Focus.kt app/src/main/kotlin/app/gakseong/ui/screens/Ceremony.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Arise.kt app/src/main/kotlin/app/gakseong/ui/screens/Gate.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Break.kt
git commit -m "The break screen shows one penalty, chosen by the engine

penaltyFor() decides which one lands. Five at once are illegible, the player
learns nothing, and the app gets uninstalled. Every state still shows the road
back."
```

---

### Task 11: Raids and sharing

**Files:**
- Modify: `Raid.kt`, `RaidHub.kt`, `RunRaid.kt` (`RunRaidScreen`, `RunSettleScreen`), `Share.kt`

**Interfaces:**
- Consumes: `LocalSystem`, `LocalNav`.
- Produces: share cards built from an allowlist.

- [ ] **Step 1: The share card allowlist, enforced in code**

`ShareScreen` may render only: class portrait, rank before and after, level, streak days, one line of fixed System script, the `각성 GAKSEONG` wordmark, and `gakseong.app/s/<code>`. Nothing else.

Build it from an explicit list rather than by omission:

```kotlin
// §10: an allowlist, never a blocklist. A card may never carry a package name, a duration, or any screen-time
// number, and there is no "but the user chose to" exemption.
private data class CardFields(
    val portrait: Int,
    val rankBefore: String,
    val rankAfter: String,
    val level: Int,
    val streakDays: Int,
    val script: String,
    val referral: String,
)
```

`ShareScreen` constructs a `CardFields` and renders only from it. Any field a future edit wants on the card has to be added to this type first, which is the point.

- [ ] **Step 2: No share affordance in the private track**

Confirm by grep that nothing in `Private.kt` reaches `ShareScreen`:

```bash
grep -n "share\|Share" app/src/main/kotlin/app/gakseong/ui/screens/Private.kt || echo "clean"
```

Expected: `clean`.

- [ ] **Step 3: Raid screens read state**

Partner name, objective and window come from state. A shadow partner carries `◇` and the label `pacer`, and the screen states plainly that it is not a person. **Never generate a plausible human username.**

`RunSettleScreen` shows the `settling` state rather than a failure, because Strava and Samsung Health write to Health Connect minutes after a run ends and a query at the whistle reads zero.

- [ ] **Step 4: Verify**

```bash
for s in raid raidhub runraid runsettle share shareraid; do
  adb shell am start -n app.gakseong/.MainActivity --es screen $s > /dev/null; sleep 1; done
adb logcat -d | grep -c "FATAL EXCEPTION"
```

Expected: `0`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/screens/Raid.kt app/src/main/kotlin/app/gakseong/ui/screens/RaidHub.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/RunRaid.kt app/src/main/kotlin/app/gakseong/ui/screens/Share.kt
git commit -m "The share card is built from a type that lists what may appear

An allowlist, not a blocklist. Adding anything to the card means adding a field
to CardFields first, so a package name or a duration cannot arrive by accident."
```

---

### Task 12: Social — league, guild, feed, invite, refer

**Files:**
- Modify: `League.kt`, `Guild.kt`, `Feed.kt`, `Invite.kt`, `Refer.kt`

**Interfaces:**
- Consumes: `LocalSystem`.
- Produces: social screens rendering a local not-connected state until phase 07.

- [ ] **Step 1: These screens have no local source of truth yet**

Firestore is phase 07. Until then each screen renders the user's own row from `sys` and shows an explicit not-connected state for everything else, rather than fabricated members.

- [ ] **Step 2: Shadow pacers are labelled**

Where `League.kt` pads a thin division, every padded row carries `◇` and the label `pacer`, the screen states they are not people, and they are excluded from the member count. **Never generate plausible human usernames to pad a ladder.**

- [ ] **Step 3: The feed cap**

`FeedScreen` renders at most twenty posts a day, guild-scoped. Add the cap as a constant with a comment naming why: global UGC needs moderation, reporting, blocking and a published policy, and all four are Play requirements.

- [ ] **Step 4: Verify**

```bash
for s in league guild feed invite refer; do
  adb shell am start -n app.gakseong/.MainActivity --es screen $s > /dev/null; sleep 1; done
adb logcat -d | grep -c "FATAL EXCEPTION"
```

Expected: `0`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/screens/League.kt app/src/main/kotlin/app/gakseong/ui/screens/Guild.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Feed.kt app/src/main/kotlin/app/gakseong/ui/screens/Invite.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Refer.kt
git commit -m "Social screens say they are not connected rather than inventing members

The ladder is the one thing in this app that has to be trustworthy. Pacers carry
the diamond and the label; nothing here generates a plausible human username."
```

---

### Task 13: The remainder — More.kt, Last.kt, Staged.kt, Soon.kt

**Files:**
- Modify: `More.kt` (`ReaderScreen`, `ShadowsScreen`, `CompleteScreen`, `MonarchScreen`, `GatesScreen`)
- Modify: `Last.kt` (`AiGateScreen`, `BonusScreen`, `WidgetScreen`, `PactScreen`, `ContainScreen`, `ChatScreen`, `StoreScreen`)
- Modify: `Staged.kt` (`PrivateSetupScreen`, `ThresholdScreen`, `WeightsScreen`, `NewAppScreen`)
- Leave untouched: `Typography.kt` (`TypeSpecimenScreen`), `Soon.kt` (`SoonScreen`) — both are stateless by design

**Interfaces:**
- Consumes: `LocalSystem`, `Repo.update`.
- Produces: the last state reads, and the exempt list for `CRITIC.md`.

- [ ] **Step 1: ThresholdScreen reads the real six**

`data-thresh`, already recovered, is the source: Movement 2,000 steps AGI · Walk or run 1.5 km AGI · Screen off 45 min-block INT · Scroll cap 3 h-max INT · Sleep 6h 30m VIT · Focus session 20 min INT. The last column is whether the user may raise it, and only rows marked raisable get a control.

Raising the band is staged to after seven clean days, so gate the control on `sys.hunter.streak >= 7`.

- [ ] **Step 2: AiGateScreen shows the locked state, never a nag**

No key configured yet in phase 03, so every AI surface renders its locked state: a blurred sample of the real output plus one `Awaken it` action. Never a modal over the core loop, never a countdown, never a repeated prompt. `Not now` is a real answer.

The copy is fixed: *"The System reads. It does not yet speak."*

- [ ] **Step 3: The rest read what they display**

`ShadowsScreen` reads `sys.hunter.shields`. `MonarchScreen` and `CompleteScreen` read rank and level. `BonusScreen` reads `sys.today.bonus`. `WidgetScreen` keeps its `requestPinAppWidget` call. `PrivateSetupScreen` reads and writes `sys.settings.privateTrack`. `NewAppScreen` reads `sys.profile.watchedPackages`, which never leaves the device.

- [ ] **Step 4: Verify all sixteen routes**

```bash
for s in read shadows complete monarch gates aikey bonus widget pact contain chat store privset thresh weights newapp; do
  adb shell am start -n app.gakseong/.MainActivity --es screen $s > /dev/null; sleep 1; done
adb logcat -d | grep -c "FATAL EXCEPTION"
```

Expected: `0`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/app/gakseong/ui/screens/More.kt app/src/main/kotlin/app/gakseong/ui/screens/Last.kt \
        app/src/main/kotlin/app/gakseong/ui/screens/Staged.kt
git commit -m "The last sixteen screens read state

Raising the band stays gated on seven clean days, which is where §13 staged it.
The AI surfaces show a locked state in place: a blurred sample and one action,
never a modal over the core loop."
```

---

### Task 14: The critic

**Files:**
- Create: `critic/check.sh`
- Create: `critic/allowlist.txt`
- Create: `critic/inventory.txt`
- Create: `CRITIC.md` (generated)

**Interfaces:**
- Consumes: the whole tree.
- Produces: one command that passes or fails honestly, and a generated inventory with `exists` and `wired` as separate columns.

- [ ] **Step 1: Write the allowlist**

Create `critic/allowlist.txt`. These are strings that are legitimately fixed and must not be reported as unwired state:

```
# Fixed System script and fixed labels. One regex per line, matched against the string literal's contents.
# Anything NOT matched here that looks like state (a rank, a number, a duration) is a finding.
Daily Quest
Threshold cleared
Below threshold
Shields full
Start within the hour or it is gone
The System reads. It does not yet speak.
Awaken it
Not now
각성 GAKSEONG
pacer
settling
Accept and begin
```

- [ ] **Step 2: Write the inventory**

Create `critic/inventory.txt`, one line per promised capability as `phase|name|grep-probe`:

```
03|SystemState persisted to disk|app/src/main/kotlin/app/gakseong/data/Store.kt:DataStoreFactory
03|One observable state|app/src/main/kotlin/app/gakseong/data/Repo.kt:StateFlow
03|Ambient state for screens|app/src/main/kotlin/app/gakseong/ui/Local.kt:LocalSystem
03|Real back stack|app/src/main/kotlin/app/gakseong/ui/Nav.kt:NavHost
03|Bottom nav navigates|app/src/main/kotlin/app/gakseong/ui/Kit.kt:onSelect
03|Day rollover|app/src/main/kotlin/app/gakseong/data/Model.kt:rolledTo
03|Share card allowlist|app/src/main/kotlin/app/gakseong/ui/screens/Share.kt:CardFields
04|Usage event fold|app/src/main/kotlin/app/gakseong/sense/Usage.kt:queryEvents
04|Health Connect|app/src/main/kotlin/app/gakseong/sense/Health.kt:dataOrigin
05|Closed verifier set|app/src/main/kotlin/app/gakseong/quest/Verifier.kt:sealed
05|Static quest bank|app/src/main/kotlin/app/gakseong/quest/Bank.kt:QuestTemplate
05|Settle pipeline|app/src/main/kotlin/app/gakseong/quest/Settle.kt:settleDay
05|Fifteen-minute job|app/src/main/kotlin/app/gakseong/work/SettleWorker.kt:PeriodicWorkRequest
05|Widgets on real state|app/src/main/kotlin/app/gakseong/widget/Widgets.kt:Repo
06|Focus foreground service|app/src/main/kotlin/app/gakseong/session/FocusService.kt:startForeground
06|DND self-expires|app/src/main/kotlin/app/gakseong/session/Dnd.kt:AutomaticZenRule
07|Anonymous auth|app/src/main/kotlin/app/gakseong/cloud/Auth.kt:signInAnonymously
07|Referral create-only|firestore.rules:allow create
08|Key in the keystore|app/src/main/kotlin/app/gakseong/ai/Key.kt:EncryptedSharedPreferences
08|Daily quest never locks|app/src/main/kotlin/app/gakseong/quest/Bank.kt:QuestTemplate
```

- [ ] **Step 3: Write the check script**

Create `critic/check.sh`:

```bash
#!/usr/bin/env bash
# The critic. One command, honest pass or fail.
#
# 1. the engine's own asserts     2. the build     3. every route launched
# 4. the placeholder audit        5. CRITIC.md regenerated
#
# Usage: critic/check.sh [--no-device]

set -uo pipefail
cd "$(dirname "$0")/.."

export JAVA_HOME="${JAVA_HOME:-$HOME/.sdkman/candidates/java/current}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

FAIL=0
step() { printf '\n\033[1m== %s\033[0m\n' "$1"; }
bad()  { printf '\033[31mFAIL\033[0m %s\n' "$1"; FAIL=1; }
ok()   { printf '\033[32mok\033[0m   %s\n' "$1"; }

ROUTES="home focus ceremony raid raidhub runraid runsettle share shareraid arise league gate break
        invite guild feed refer soon profile private report splash welcome perms diag apps contract
        intent class stage privset thresh weights newapp read shadows complete monarch gates aikey
        bonus widget pact contain chat store type settings"

step "1. engine self-check"
if kotlinc engine/*.kt -include-runtime -d /tmp/engine.jar 2>/dev/null \
   && java -cp /tmp/engine.jar gakseong.engine.AuraSelfCheckKt; then
  ok "engine asserts pass and kotlinc still compiles engine/ alone"
else
  bad "engine self-check — a dependency may have leaked into engine/"
fi

step "2. unit tests and build"
./gradlew testDebugUnitTest assembleDebug -q && ok "build and JVM tests" || bad "build or JVM tests"

step "3. route smoke test"
if [ "${1:-}" = "--no-device" ] || ! adb get-state >/dev/null 2>&1; then
  printf 'skipped: no device. Routes are unverified, which is not the same as passing.\n'
else
  adb install -r app/build/outputs/apk/debug/app-debug.apk >/dev/null 2>&1
  adb logcat -c
  for s in $ROUTES; do
    adb shell am start -n app.gakseong/.MainActivity --es screen "$s" >/dev/null 2>&1
    sleep 0.8
  done
  CRASHES=$(adb logcat -d | grep -c "FATAL EXCEPTION")
  if [ "$CRASHES" -eq 0 ]; then ok "all $(echo $ROUTES | wc -w | tr -d ' ') routes launched clean"
  else bad "$CRASHES fatal exceptions across the routes"; adb logcat -d | grep -A6 "FATAL EXCEPTION" | head -40; fi
fi

step "4. placeholder audit"
# A screen that compiles, renders, and still shows 640 is the failure this catches. Any string literal in a
# screen that looks like state (a rank, a bare number, a duration) and is not in the allowlist is a finding.
SUSPECT=$(grep -rnoE '"[^"]*([0-9]{2,}|[EDCBAS] · (I|II|III))[^"]*"' \
            app/src/main/kotlin/app/gakseong/ui/screens/ \
          | grep -vFf critic/allowlist.txt \
          | grep -v 'Typography.kt' || true)
if [ -z "$SUSPECT" ]; then
  ok "no unwired state literals in screens"
else
  bad "$(echo "$SUSPECT" | wc -l | tr -d ' ') suspected unwired literals"
  echo "$SUSPECT" | head -30
fi

step "5. CRITIC.md"
{
  echo "# Critic"
  echo
  echo "Generated by \`critic/check.sh\`. Do not edit by hand."
  echo
  echo "\`exists\` means the file and symbol are present. \`wired\` means something outside its own file calls it."
  echo "The gap between those two columns is where this kind of work rots, which is why they are separate."
  echo
  echo "| phase | capability | exists | wired |"
  echo "|---|---|---|---|"
  while IFS='|' read -r phase name probe; do
    [ -z "${phase:-}" ] && continue
    file="${probe%%:*}"; symbol="${probe##*:}"
    if [ -f "$file" ] && grep -q "$symbol" "$file" 2>/dev/null; then exists="yes"; else exists="no"; fi
    base=$(basename "$file")
    if [ "$exists" = "yes" ] && grep -rq "$symbol" --include="*.kt" --exclude="$base" app/src/main 2>/dev/null; then
      wired="yes"
    else
      wired="no"
    fi
    echo "| $phase | $name | $exists | $wired |"
  done < critic/inventory.txt
} > CRITIC.md
ok "CRITIC.md regenerated"
grep -c '| no |' CRITIC.md | xargs printf '%s capabilities still missing or unwired\n'

step "verdict"
[ "$FAIL" -eq 0 ] && printf '\033[32mPASS\033[0m\n' || printf '\033[31mFAIL\033[0m\n'
exit "$FAIL"
```

- [ ] **Step 4: Make it executable and run it**

```bash
chmod +x critic/check.sh
critic/check.sh
```

Expected: steps 1, 2 and 3 pass. Step 4 reports whatever literals remain, which is the honest state. Step 5 writes `CRITIC.md` showing phase 03 rows as `yes / yes` and phases 04 to 08 as `no / no`.

- [ ] **Step 5: Fix whatever step 4 reports**

Every finding is either a real unwired literal, which gets wired, or a legitimately fixed string, which goes in `critic/allowlist.txt` with a one-line reason. Do not silence a finding by loosening the regex.

- [ ] **Step 6: Commit**

```bash
git add critic CRITIC.md
git commit -m "The critic, and why exists and wired are two columns

A file that declares a function and a screen that actually calls it are
different states, and the gap between them is where this kind of work rots.

The placeholder audit is the part that earns its keep: it catches a screen that
compiles, renders, and still shows 640."
```

---

## Self-Review

**Spec coverage.** Phase 03 of the spec has four parts. Storage is Tasks 1 to 3. State is Task 4. Navigation is Tasks 5 and 6. Screen rewiring is Tasks 7 to 13. The critic is Task 14, which the spec places alongside phase 03. The spec's verification criteria for phase 03 — launches from disk, survives a kill, all forty-eight routes clean, zero unwired literals — map to Task 4 Step 5, Task 8 Step 5, Task 14 Step 3 and Task 14 Step 4.

**Types.** `SystemState`, `HunterSnapshot`, `Today`, `QuestInstance`, `Bonus`, `Profile`, `Settings`, `DaySettlement` and `rolledTo` are declared once in Task 2 and used with those exact names in Tasks 3, 4, 7, 8, 9, 10, 11, 12 and 13. `Repo.state` / `Repo.update` / `Repo.today` are declared in Task 4 and used from Task 8 onward. `LocalSystem` is Task 4, `LocalNav` and `NAV_TABS` are Task 5. `BottomNav(active, onSelect)`, `Cta(..., onClick)` and `Pill(..., modifier)` are the three `Kit.kt` signature changes, made in Tasks 5, 8 and 9 respectively.

**Known gap, deliberate.** `SEED` carries five hand-written quests standing in for the phase-05 bank. This is recorded in `Seed.kt` with a `ponytail:` comment and tracked in `CRITIC.md`, so it cannot be mistaken for real quest generation.

**Note for the executor.** Tasks 8 through 13 edit files this plan has not reproduced in full. Read each file completely before editing it. The mapping tables say what to change; they do not say the file contains nothing else.
