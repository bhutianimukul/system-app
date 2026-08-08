# Full functionality — phases 03 to 08

**Status:** approved 2026-08-08. Supersedes nothing; `CLAUDE.md` and `DECISIONS.md` remain the authority on
product rules. This document covers only how the built screens become a working app.

## Where the code actually is

Forty-eight screens compile and render. Every one of them prints literals. `HomeScreen()` takes no parameters
and draws `XlNumber("640")`, `Tag("D · III")`, five `QuestCard`s with fixed titles and fixed states. There is
no seam to inject state through.

`engine/Aura.kt` is 200 lines of pure functions with 51 assert-based checks passing. It has no clock, no
storage, and no notion of what produced its `auraToday` argument. That missing layer, quest instances and
sensor readings collapsing into one integer, is larger than the engine.

`MainActivity` routes on an intent extra with no back stack.

Nothing else exists: no persistence, no permissions, no data sources, no scheduled work, no services, no
Firebase, no AI.

## Build order and why it is this order

Six phases. Each one is verifiable on its own, and each depends only on the ones before it.

| phase | name | depends on |
|---|---|---|
| 03 | The spine: persistence, state, navigation | nothing |
| 04 | Sensors: UsageStats, Health Connect, permissions | 03 |
| 05 | The loop: verifiers, quest bank, settle, WorkManager, widgets | 03, 04 |
| 06 | Sessions: focus service, DND, night gate, speed bump | 03, 05 |
| 07 | Cloud: Firebase, guilds, league, feed, referral | 03 |
| 08 | AI gate: Gemini BYOK, locked states | 03, 05 |

Phase 07 is independent of 04 through 06 and could run in parallel. It is placed late because it needs a real
Firebase project, which is the one input this plan cannot produce for itself.

---

## Phase 03 — The spine

### Storage: DataStore with kotlinx.serialization

`CLAUDE.md` names Room only in the inventory of what does not exist. It is absent from the locked Stack list,
so the choice is open.

What gets stored is one `HunterState` record, a settings blob, a profile, today's quest instances, and roughly
365 small settlement rows a year. A year of history is a few hundred kilobytes held comfortably in memory.
Room would buy date-range SQL for the chart screens at the cost of KSP codegen, entity and DAO files, and
migration discipline on every schema change.

Decision: `DataStore<SystemState>` with a JSON serializer. The ceiling is that every settle rewrites the whole
file. At this data volume that is microseconds. A `ponytail:` comment in `Store.kt` names the ceiling and
records Room as the upgrade path, to be taken if the report screen or the private journal ever measures slow.

### Files

**`data/Model.kt`** — `@Serializable` domain types.

```
SystemState(
  hunter: HunterState,        // reused from the engine, already a data class
  today:  Today,              // quest instances, bonus, aura accumulated, date stamp
  profile: Profile,           // hunter class, intent selections, watched packages
  settings: Settings,         // every toggle the Settings screen owns
  history: List<DaySettlement>,
  onboarded: Boolean,
)
```

`HunterState` is imported from `gakseong.engine` rather than duplicated. It gains a
`@Serializable` annotation, which is the only change phase 03 makes to engine code. `Rank` serializes as its
`ordinal` integer, so the `require` in its constructor still guards deserialization.

`Today` carries a date stamp. Reading a `SystemState` whose `today.date` is not the current local date rolls it
over, which is how a day boundary is detected without a scheduled alarm.

**`data/Store.kt`** — one `DataStore<SystemState>` built with `DataStoreFactory.create` and a `Serializer`
backed by `Json`. Corrupt or unreadable state falls back to a documented default rather than throwing, because
a crash loop on cold start is unrecoverable for the user.

**`data/Repo.kt`** — a singleton holding `StateFlow<SystemState>` and the mutation functions. Initialized from
`Application.onCreate`. Widgets and workers read the same DataStore instance.

No dependency injection framework. No ViewModels. Forty-eight screens would mean forty-eight near-empty
ViewModel files, and this codebase already reads ambient state through `LocalPalette`, `LocalMetrics`,
`LocalType` and `LocalHunterClass`. `LocalSystem` follows the convention already established rather than
introducing a second one beside it.

### Navigation

`androidx.navigation:navigation-compose`, wrapping the existing `ScreenTransition` from `ui/Motion.kt` so the
motion work survives. Three requirements drive the choice over a hand-rolled back stack:

1. App Links must resolve `gakseong.app/s/<code>` to a screen for the phase-07 referral chain.
2. Predictive back on Android 14+ needs a real back stack.
3. The screenshot contact sheet drives routes with `--es screen <name>`, which must keep working.

The intent extra survives as a debug start-destination override. It is read once in `onCreate` and sets the
initial route; after that the nav graph owns navigation.

Onboarding gates the start destination: `onboarded == false` routes to `welcome`, otherwise `home`.

### Rewiring the screens

The bulk of the phase. Every literal that represents state becomes a read from `LocalSystem`. Literals that are
fixed System script stay literal.

The distinction is worth stating precisely, because the critic checks it: `Tag("D · III")` is state and becomes
`Tag(sys.hunter.rank.label)`. `Eye("Daily Quest")` is a fixed label and stays. `Text("Start within the hour or
it is gone")` is System script and stays.

Screens with no state at all (`type`, `soon`) are exempt and listed as such in `CRITIC.md`.

### Verification

The app launches to Home showing state loaded from disk. Killing and reopening preserves it. A seeded
`SystemState` drives all forty-eight routes with no crash. The critic's placeholder audit reports zero
unwired state literals.

---

## Phase 04 — Sensors

### `sense/Usage.kt`

`PACKAGE_USAGE_STATS` is special access. It is requested through
`Settings.ACTION_USAGE_ACCESS_SETTINGS` and checked with `AppOpsManager.unsafeCheckOpNoThrow`, never through a
runtime permission dialog, because there is no runtime dialog for it.

One pass over `UsageStatsManager.queryEvents(start, end)` folds the event stream into every reading the
verifiers need:

- total foreground time in a window
- per-package foreground durations
- longest unbroken screen-off block, from `SCREEN_INTERACTIVE` and `SCREEN_NON_INTERACTIVE`
- whether named packages were opened in a window

Four separate queries over the same events would be four times the work for identical output, so this is a
single fold returning one `UsageReading`.

`queryEvents` returns nothing without the grant. The absent-grant path returns a `UsageReading.Unavailable`
that quest generation treats as "do not draw this verifier", the same rule Health Connect follows.

The carve-out from `DECISIONS.md` §24 lands here: time in the in-app reader, and time in the recording app
during a committed running-raid window, are subtracted from every screen-time reading. Without it the app
penalises somebody for running.

### `sense/Health.kt`

`androidx.health.connect:connect-client`. Three record types: `StepsRecord`, `DistanceRecord`,
`SleepSessionRecord`. Each result's `dataOrigin` is checked against a list of real providers, which is what
makes the sensor tier trustworthy enough to reach S-rank.

Health Connect availability is three-valued: installed and available, update required, or unavailable on this
device. All three are handled. Unavailable degrades to the same "never draw this verifier" path.

### Screens that become real

- `perms` — actual grant state per permission, actual intents to request them
- `apps` — five packages preselected from the user's own 30 days of usage, a confirm rather than a choose
- `diag` — the Reality screen reads real totals from the last 30 days, which is all Android retains

### Verification

On an emulator with granted access and seeded Health Connect data, the readings match what the system settings
report. With every grant refused, no screen crashes and no verifier is drawn.

---

## Phase 05 — The loop

### `quest/Verifier.kt`

The closed set of ten as a sealed class. Each variant carries its numeric parameters and knows how to evaluate
itself against a reading:

```
SCREEN_OFF_BLOCK(minMinutes)      APP_ABSENT(packages, windowMinutes)
TOTAL_SCREEN_TIME(maxMinutes)     APP_BUDGET(package, maxMinutes)
STEPS(target)                     DISTANCE(targetMetres)
SLEEP(minMinutes)                 LOCATION_CHECKIN(latitude, longitude, radiusMetres)
CALL_DURATION(minMinutes)         DECLARED
READ_SESSION(minMinutes)
```

Each variant declares its own `Provability`, which is what stops a declared quest from ever paying a sensor
rate. `LOCATION_CHECKIN` and `CALL_DURATION` are defined here but their data sources are stubbed to
unavailable; both need permissions that are not worth requesting until a quest actually uses them.

### `quest/Bank.kt`

Static quest templates as a Kotlin list. No JSON parser for data that ships inside the APK and never changes
without a rebuild. Each template is an icon, a title, a verifier with parameter ranges, a base aura value, and
eligibility rules.

This is the bank `DECISIONS.md` §8 requires to exist regardless of AI, and the fallback the AI gate degrades
to. The daily quest never locks.

Selection is seeded by date and hunter state, so the same day produces the same quests across a widget refresh
and an app open. Determinism here is what stops a user rerolling by pulling to refresh.

### `quest/Settle.kt`

The layer the engine was written to sit under.

1. Read each active quest's verifier against the current sensor readings.
2. For each cleared quest, `award(template.baseAura, verifier.provability)`.
3. Sum to `auraToday`.
4. Call `settleDay(state, auraToday, bonusCeiling)`.
5. Persist the returned `Settlement`, append a `DaySettlement` to history.

Non-confirmable quests settle by a single yes/no at expiry, per the economy rules. A `DECLARED` quest that is
never answered expires unclaimed rather than defaulting to cleared.

The daily aura cap is enforced by `settleDay` and is a safety property. Nothing in this layer may raise it.

### `work/SettleWorker.kt`

`PeriodicWorkRequest` at the 15-minute floor, plus explicit runs on app open and widget refresh. Retroactive
query of history, never a live watcher, because OEM battery managers kill long-lived services.

Settling is idempotent: running twice for the same day produces the same result. The date stamp on `Today` is
what makes that true.

### Widgets

`widget/Widgets.kt` swaps its placeholder data class for a read of the real DataStore. The four providers are
already registered and verified on a launcher.

### Verification

A seeded day with known usage produces the expected aura, the expected `DayOutcome`, and the expected rank
movement. Running the worker twice changes nothing the second time.

---

## Phase 06 — Sessions

**Focus foreground service.** Bounded by session length, dialer whitelisted, ~10 second return grace so a
mis-tap does not cost the session. `FOREGROUND_SERVICE_SPECIAL_USE` with a declared subtype, since targetSdk is
35.

**DND via `AutomaticZenRule`.** Registered through `NotificationManager.addAutomaticZenRule` and driven by a
condition, so the rule expires on its own if the app dies mid-session. `setInterruptionFilter` is not used: it
can strand a user in permanent silence after a crash. Needs `ACCESS_NOTIFICATION_POLICY`, requested at the
first session rather than at onboarding. Calls and repeat callers always ring. The prior interruption filter is
read before starting and restored after. Skipped entirely if system Bedtime mode already has DND active. A
refused grant degrades to a normal session.

**Night gate.** `SCREEN_OFF_BLOCK` across a configurable 00:30 to 06:00 window, checked once after it closes by
the existing settle worker. Zero battery cost.

**Speed bump.** `SYSTEM_ALERT_WINDOW` behind a single user toggle, drawn only while a session's foreground
service is alive.

**Containment** stays opt-in and earned by repeated failure. The `AccessibilityService` is declared and the
permission is requested at the trigger, never at onboarding.

---

## Phase 07 — Cloud

**Blocked on one input from the user: a Firebase project and its `google-services.json`.**

Everything in this phase is built so that a missing config file degrades to local-only rather than crashing, so
phases 03 through 06 stay verifiable without it. Guild, league and feed screens show a "not connected" state
instead of an error.

- Firebase Anonymous Auth. No sign-up, no email, no phone.
- Firestore for guilds, weekly leagues of about thirty, and the guild-scoped feed capped at twenty posts a day.
- Shadow pacers fill divisions below thirty. They carry `◇` and the label `pacer`, are stated on screen to not
  be people, and are never counted as members. No plausible human usernames are ever generated.
- FCM for raid invites and league results.
- Referral: opaque code from the inviter's UID, Play Install Referrer for attribution, App Links when the app is
  already installed, `referrals/{inviteeUid}` with a create-only security rule. Release condition is three
  cleared days inside a fourteen-day window, not necessarily consecutive. App Check on, creates rate-limited per
  inviter.
- Analytics is GA4 for apps, user-switchable in Settings. Three things leave the device and nothing else:
  guild identity, anonymous feature counts, crash stack traces.

The private track emits no analytics event of any kind. The exemption is unconditional and is not tied to the
analytics toggle. This is enforced by routing every analytics call through one function that hard-refuses when
the private track is the caller, so the rule lives in code rather than in review discipline.

---

## Phase 08 — AI gate

Gemini, bring your own key. The app is fully complete with zero AI configured.

- Key stored in `EncryptedSharedPreferences`. Never logged, never in analytics, never sent anywhere but
  Google's endpoint.
- One plain HTTPS POST. No SDK dependency for a single endpoint.
- Five features lock: generated quests, weekly post-mortem, chat, reader passage selection, title generation.
  Each shows a locked state in place with a blurred sample and one `Awaken it` action. Never a modal over the
  core loop, never a countdown, never a repeated prompt.
- The daily quest never locks. It falls back to the phase-05 static bank.
- Free-tier warning shown before the paste. `Not now` is a real answer that costs nothing and does not re-ask.
- The model emits a verifier from the closed set plus numeric parameters. Clamps are enforced in code after
  generation, never in the prompt. AI never touches the aura number.
- The private track never touches AI on either tier.

---

## The critic

Three parts, because they catch three different failures.

### `critic/check.sh`

One command, honest pass or fail:

1. Engine self-check. The 51 asserts that already exist.
2. `./gradlew assembleDebug`.
3. Route smoke test. Launch all forty-eight routes on an emulator, scan logcat for `FATAL EXCEPTION` and ANR.
4. **Placeholder audit.** Grep the screen sources for state literals that should be reads. This is the check
   that catches a screen which compiles, renders, and still shows `640`. The allowlist of legitimately fixed
   strings lives beside the script.
5. Regenerate `CRITIC.md`.

### `CRITIC.md`

A generated table: every function this spec promises, whether it exists, and whether it is wired to real data.
Three columns rather than two, because "the file exists" and "the screen actually reads it" are different
states and the gap between them is where this kind of work rots.

### Review pass per phase

A reviewing subagent audits each phase's diff against the non-negotiables, with standing attention on:

- §9 and §10: no package name, no duration, nothing from the private track ever leaving the device
- the share card built from an allowlist rather than a blocklist
- the daily aura cap never raised
- penalties sequencing rather than stacking
- no AI path that can reach the aura number

---

## Testing

The engine keeps its assert-based self-check with no framework, which is why it stays runnable by
`kotlinc engine/*.kt`.

New logic gets the same treatment: one runnable check per non-trivial unit, assert-based, no fixtures. The
units that earn a check are the usage event fold, the verifier evaluations, the settle pipeline, and the
referral release condition. Composables do not get unit tests; the route smoke test covers them.

## Risks

**Emulator sensor data.** `UsageStatsManager` on an emulator reports the emulator's own usage, which is thin.
Phase 04 verification seeds Health Connect directly and validates the usage fold against synthetic event
streams rather than against real emulator behaviour.

**Firebase project.** Phase 07 cannot complete without it. Flagged early so it is not discovered at the end.

**Forty-eight screen rewrites in one phase.** Wide and mechanical. The placeholder audit is what makes it
verifiable rather than a matter of trusting that every file was visited.
