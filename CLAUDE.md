# Gakseong — feature spec and build rules

**Gakseong** (각성, Korean for *awakening*) is the product name and store identity. **The System** is the
in-app voice and never changes — it is what the app calls itself when it speaks to the user. Keep the two
separate: every screen says "The System", the Play listing says Gakseong. That separation means the store
identity can change without touching a single screen.

Android app. Kotlin, Jetpack Compose, single module to start. A discipline game where **every quest is
proven by the phone itself**. Audience is 15–30 and anime-adjacent.

**Design is locked (2026-07-28).** `DECISIONS.md` holds the reasoning for everything below and is the
authority when this file is ambiguous. `SESSION-LOG.md` has every instruction that produced it, in order.
Treat both as decisions of record. Changing one is a new decision that needs its own reasoning.

---

## Non-negotiables

Read these before writing code. Several look like style choices and are not.

1. **Android only.** `UsageStatsManager` gives real package names and durations. iOS Screen Time returns
   opaque tokens, so the core mechanic cannot exist there. Do not add an iOS target.
2. **Retroactive settle, never a live watcher.** Query `UsageStats` history on app open, widget refresh,
   and a 15-minute `WorkManager` job. No persistent foreground service for tracking. OEM battery managers
   (Xiaomi, Oppo, Vivo, Samsung) kill long-lived services, and a design that depends on one is dead on
   most Indian devices.
3. **The daily aura cap is a safety property, not balance.** It puts a hard ceiling on how much the app can
   reward in a single day. This matters because there are fifteen-year-olds in a fitness-adjacent app. Never
   raise it to make progression feel better.
4. **Penalties sequence, never stack.** Streak, then aura, then warning, then demotion and containment,
   spread across a week. Five penalties at once are illegible; the player learns nothing and uninstalls.
   Every penalty screen shows the road back.
5. **No penalty ever demands physical effort.** No "do 50 push-ups to recover". Exercise as punishment is
   both a safety problem and a liability one.
6. **The System assigns aura. The player never does.** The moment users weight their own tasks the ladder
   is meaningless.
7. **AI never touches the aura number.** The model emits a verifier from a closed set plus numeric
   parameters. Clamps are enforced in code after generation, never in the prompt. Free-text quests cannot
   be tiered, and an untierable quest kills the leaderboard.
8. **No baked text in any art asset,** and no borrowed character names or the Solo Leveling mark. Genre
   vocabulary (gate, dungeon, hunter, awakening, raid, guild, rank E–S, shadow) is free. Character names
   and the specific cosmology are not.
9. **Private-track data never leaves the device and never reaches another human.** Biometric gate on every
   open. No guild notification, no leaderboard entry, no "went dark" flag. **It emits no analytics event of
   any kind** — not a screen view, not a count, not "a check-in happened". The exemption is unconditional and
   is not tied to the analytics toggle.
10. **Three things leave the device, and nothing else:** guild identity when you join one, anonymous feature
   counts, and crash stack traces. Never a package name from the user's app list, never a duration, never
   anything from the private track. Analytics is Firebase (GA4 for apps), user-switchable in Settings, and
   because it exists the app needs a **privacy policy URL and a Play Data Safety declaration** — both are
   Play requirements the moment anything leaves the device. **A share card is bound by this rule too.** It may
   never render a package name, a duration or any screen-time number, and there is no "but the user chose to"
   exemption. Build the card from an allowlist, not a blocklist.

---

## Branding in the app

**The name appears in two places and nowhere else:** the splash, and a small wordmark in the Home header
(`각성 GAKSEONG`, hangul first, latin as a spaced-out sub-label). Everywhere else the app still speaks as
**The System**. The wordmark is a mark, not a voice — it never says "Gakseong" in a sentence to the user.

### Splash — use the platform API, do not build an Activity

**Android 12+ shows a system splash whether you want one or not.** Build it with
`androidx.core.splashscreen` (`SplashScreen` compat) and customise it. **A custom splash Activity on top of
that produces a visible double-splash**, which is the single most common way this gets shipped wrong.

- Icon animation goes in an **`AnimatedVectorDrawable`** set as `windowSplashScreenAnimatedIcon`. The system
  gives it roughly **1000 ms**; anything longer is cut off, so design to that budget.
- Use **`setOnExitAnimationListener`** to take over the `SplashScreenView` and run the handoff yourself. That
  is where the brand moment lives: the hangul resolving out of a blur, the aura ring expanding, then the gate
  splitting to reveal Home. Budget ~600 ms for the handoff.
- **`setKeepOnScreenCondition` only until the first frame is ready.** Never hold the splash to finish an
  animation — a splash that delays first paint is a retention cost, and cold start is already the worst
  moment in the app.
- **Respect reduced motion.** Check `Settings.Global.ANIMATOR_DURATION_SCALE`; when it is 0, skip straight to
  Home with a cross-fade. The design file mirrors this with `prefers-reduced-motion`.
- The gate-split reveal is decoration over an already-rendered Home. If anything in the sequence fails, the
  user still lands on a working screen.

## The economy

| concept | rule |
|---|---|
| **Aura** | one currency. Every task is worth aura; the ceiling depends on how provable it is |
| **Daily band** | a threshold below and a cap above. Land inside it |
| **Below threshold** | penalty territory |
| **Inside the band** | Rank counts |
| **Above the cap** | Level only. Overflow never buys standing |
| **Level** | private, only rises. The reward |
| **Rank** | public E→S with three tiers each, rises and falls. The stakes |
| **Bonus** | one per day, spawned at random. Raises today's ceiling by 120–450 |

**Provability tiers.** Sensor-proven pays full and reaches S-rank. App-initiated pays medium.
Declared (self-reported) pays low. An honest player using only declared quests still climbs, but nobody
reaches S-rank without doing things that can be checked.

**Non-confirmable tasks pay roughly a third** (120–180 against 400–450) and are settled by a single
yes/no question when the clock runs out. This covers meditation, music sessions, sitting with no screen,
thirty minutes with your parents, talking to a friend, a meal with people. These matter most and the
phone can see none of them. Equal pay would make self-reporting the rational way to farm.

**Rank is the adaptive difficulty engine, in arithmetic.** Thresholds scale with rank, so difficulty
converges on real capacity without an opaque model deciding it.

---

## Verifiers — the closed set

Every quest maps to one of these. Build the verifiers once and the quest bank writes itself; the same code
that issues "don't touch your phone for 90 minutes" issues a 72-hour Gate.

| verifier | API | notes |
|---|---|---|
| `SCREEN_OFF_BLOCK` | `UsageStatsManager` + screen-interactive events | longest unbroken phone-free block |
| `APP_ABSENT` | `UsageStatsManager` | named packages unopened for a window |
| `TOTAL_SCREEN_TIME` | `UsageStatsManager` | per-day or per-window budget |
| `APP_BUDGET` | `UsageStatsManager` | per-package time cap |
| `STEPS` / `DISTANCE` | Health Connect | `dataOrigin` checked against real providers |
| `SLEEP` | Health Connect | one query at 06:00, zero battery cost |
| `LOCATION_CHECKIN` | foreground location, one tap | passive geofencing needs a restricted permission |
| `CALL_DURATION` | `READ_PHONE_STATE` | duration only, never who. Avoids `READ_CALL_LOG` |
| `DECLARED` | user confirmation | low ceiling, one yes/no at expiry |
| `READ_SESSION` | in-app reader foregrounded, scroll advancing | app-initiated tier. The passage ends and never autoloads |

**Night gate** is `SCREEN_OFF_BLOCK` across a configurable 00:30–06:00 window, checked once after it
closes. **Focus session** is a foreground service bounded by session length, dialer whitelisted, with a
~10s return grace so a mis-tap does not cost the session.

### Do Not Disturb during sessions

A session turns DND on and takes it off again when the session ends. Applies to focus sessions, the night
gate and raids.

- **Use `AutomaticZenRule`**, not `setInterruptionFilter`. Register a named rule via
  `NotificationManager.addAutomaticZenRule` and drive it with a condition. It appears in system Settings under
  the app's own name so the user can edit or kill it, and — the reason that matters — **a condition-driven
  rule expires on its own if the app dies mid-session.** Setting the filter by hand can strand a user in
  permanent silence after a crash, which is the worst bug this feature could ship.
- Needs **`ACCESS_NOTIFICATION_POLICY`**, which is a special access granted through
  `Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS` rather than a runtime dialog. **Ask at the first
  session, never at onboarding**, same rule as §3. It is not a Play-restricted permission and needs no
  declaration form.
- **Calls always ring.** Configure the rule to allow calls and repeat callers. The audience includes
  fifteen-year-olds, and an app that silences a parent's call for forty-five minutes is indefensible. This
  also matches the existing dialer whitelist during focus sessions.
- **Restore, do not assume.** Read the prior interruption filter before starting and put it back after, so a
  user who already had DND on for their own reasons keeps it.
- Skip it entirely during the night gate if system Bedtime mode already has DND active. Do not fight the
  platform for control of the same setting.
- Fully optional and off by default until the user grants access. A refused grant degrades to a normal
  session rather than blocking it.

**Speed bump** uses `SYSTEM_ALERT_WINDOW` (a single user toggle) and only while a session's foreground
service is already alive. **Containment** (real blocking) needs `AccessibilityService`, so it stays
opt-in, earned by repeated failure, disclosed at onboarding with the permission requested at the trigger.
An accessibility dialog warning that the app "can view your screen" will kill installs if shown on day one.

`DevicePolicyManager.setPackagesSuspended` is not viable — Device Owner needs factory-reset provisioning.

---

## Onboarding — four asks, then it starts

Setup asks four questions and nothing more, about ninety seconds:

1. **Access** — usage, Health Connect, notifications, location
2. **Intent** — free text plus fifteen quoted statements, pick at least five
3. **Apps** — five already selected from the user's own 30 days. A confirm, not a choose
4. **Terms** — the single accept gate. It issues the first quest

Then Reality (206 hours from the last 30 days, which is all Android retains), Awakening (class derived
from intent), and Baseline with the first three quests. **These carry no step counter** because they cost
the user nothing. Only screens that ask something are numbered.

Staged later, never in setup: self-rating on day 2, private track on day 3, raising the band after seven
clean days, weighting in Settings.

**New-app detection runs at every app start for the app's whole life.** A stored package list of social,
short-video, browser, VPN, delivery and betting apps, matched on device. No package list leaves the phone.

---

## Social

- **Guilds before global leaderboards.** Weekly leagues of about thirty. A global ladder looks abandoned
  at low population.
- **Thin divisions are filled with shadow pacers, never fake people.** A division below thirty real hunters
  is padded with System-run shadows that hold a fixed pace. They carry a `◇` and the label `pacer`, are
  stated on the screen to not be people, and are never counted as members. Each real hunter who joins
  replaces one. **Never generate plausible human usernames to pad a ladder** — the ladder is the one thing
  in this app that has to be trustworthy, and a fabricated member who never posts in the guild feed gets
  noticed.
- **A raid partner can be a shadow.** Shadow raids exist for when nobody is free: the shadow never breaks,
  so only the user can fail. It pays **+180 against +450** for a human partner, because the bonus is paying
  for accountability rather than for the timer.
- **Referral: one rewarded summon per week, +600 aura**, paid when the invitee **clears day 3**, not on
  install. Paying on install rewards installing the app twice. Referral aura is ordinary aura and sits under
  the same daily cap, so anything above the ceiling rolls into Level.

### Referral tracking — no email, no phone, no account

The whole chain runs on identifiers the platform already gives you. **Do not add email or phone collection
for this**; it buys almost no fraud resistance and costs the entire no-account posture.

1. **Link** carries an opaque code derived from the inviter's Firebase Anonymous Auth UID:
   `gakseong.app/s/<code>`. Not a name, not an address.
2. **Attribution** via the **Play Install Referrer API** (`InstallReferrerClient`). Play hands the app the
   referrer string once, on first launch, for installs that went through the Store. This is why the design
   already specifies App Links plus Install Referrer — a plain deep link cannot survive the Play round-trip,
   and Firebase Dynamic Links is shut down. If the invitee already has the app, the App Link carries the code
   directly instead.
3. **Identity** is the invitee's own Anonymous Auth UID. No sign-up.
4. **Pairing** is a Firestore doc `referrals/{inviteeUid} = {inviterUid, installedAt, state:pending}`, with a
   security rule allowing **create only, never update**. One credit per invitee UID, ever.
5. **Release condition:** the invitee clears the daily threshold on **three days inside a fourteen-day
   window, not necessarily consecutive.** Consecutive would punish the inviter for the invitee's bad Tuesday.

**Where this is forgeable, stated plainly.** Anonymous Auth UIDs rotate on reinstall, so a determined person
with a spare device can farm it. The three-day gate is the actual defence rather than a retention nicety: the
fake account has to clear three real thresholds, which means defeating the same `UsageStats` and Health
Connect `dataOrigin` checks as normal play. That work costs more than 600 capped aura is worth, and the
weekly limit caps the upside at roughly one per week regardless.

**Cheap hardening, no PII:** add **Firebase App Check** so only genuine app instances can write, and rate-limit
`referrals` creates per inviter in security rules.

**If you want real verification, that needs one Cloud Function** — a Firestore trigger evaluating the day
count server-side, because a client-reported "I cleared three days" is written by the very account you are
trying to verify. That breaks the "no server code" line in §8, so it is a deliberate trade to make later if
abuse actually shows up, not upfront.
- **Raids** are live co-op sessions with a shared objective drawn at random from eighteen. Base aura is
  never at risk; only the raid bonus. Whoever broke it loses base as well. That rule is the difference
  between accountability and resentment.
- **The invite is the install loop.** OS share sheet, App Links plus the Play Install Referrer API.
  Firebase Dynamic Links is shut down. No friend list, no pairing step, no code to type.
- **The feed ends** at twenty posts a day. Guild-scoped only. Global UGC would need moderation, reporting,
  blocking and a published policy, all four being Play requirements.

### Running raids — a shared window, never a shared place

One objective in the eighteen, on the existing `DISTANCE` verifier. Both hunters commit to a window such as
18:00–19:00, each runs wherever they actually run, and neither learns where the other was. No map, no route, no
GPS trace, no new permission. Sensor-proven tier, so it pays full, because Health Connect carries `dataOrigin`.
Aura rules are the ordinary raid rules: base safe, bonus at stake, +450 human and +180 shadow pacer. The
pacer's pace is target distance over the window, rendered `6:00/km`. Full reasoning in `DECISIONS.md` §26.

- **Settle retroactively, never at window close.** Strava, Nike and Samsung Health write to Health Connect on
  sync, minutes after the run ends, so a query at the whistle reads zero. Close the window, then let the
  15-minute `WorkManager` job re-check for up to 30 minutes while the screen holds `settling`. Never declare a
  failure the app may have to reverse.
- **The session starts in the runner's own app.** `Start in Strava` is
  `PackageManager.getLaunchIntentForPackage` on the origin package from their last distance record. Not the
  Strava API, which forbids showing one user's data to another and so cannot feed a raid or a ladder at all.
  See §27; Health Connect already delivers Strava, Nike, Garmin, Fitbit and Samsung Health for free.
- **A running raid is not a focus session.** No foreground service, no speed bump, no return grace, no penalty
  for leaving the app. Leaving is the point.
- Filter on the committed window, not on gait. `ExerciseSessionRecord` type checking is the upgrade path if
  pace gaming appears.
- No Health Connect grant means the objective is never drawn, same as steps.

### Sharing an ascension

Rank ascension only, for now. `Share this ascension` opens a preview screen showing the card at 9:16, then the
system share sheet. Capture with `rememberGraphicsLayer()` → `record {}` → `toImageBitmap()`, PNG into
`cacheDir`, out through `FileProvider` with `FLAG_GRANT_READ_URI_PERMISSION`. No storage permission, no new
dependency, and the card is the real ceremony composable so the two cannot drift.

**Allowlist for the card:** class portrait, rank before and after, level, streak days, one line of fixed System
script, the `각성 GAKSEONG` wordmark, `gakseong.app/s/<code>`. Nothing else, per §10. **No share affordance
exists anywhere in the private track.** Bake the referral link into the image *and* repeat it in `EXTRA_TEXT`,
because Instagram Stories drops the text when an image is attached. If capture fails, share text only.

---

## Stack

- Kotlin, Jetpack Compose, `WorkManager` (15-minute floor), Glance for widgets
- Firebase Anonymous Auth, Firestore, FCM. No server code
- Health Connect (Google Fit is deprecated)
- AGSL `RuntimeShader` for effects on Android 13+, `RenderEffect.createBlurEffect` on 12+, degrade to
  static art below
- `VibrationEffect` composition primitives. Haptics fire even on silent
- AI is bring-your-own-key (Gemini). The app must be fully complete with zero AI configured

### The AI gate — lock the feature, never the app

Five things need a key: generated quests, the weekly post-mortem, chat, reader passage selection, and title
generation. Each shows a **locked state in place** rather than a modal that interrupts anything.

- **The locked state previews the value.** A blurred sample of the real output plus one `Awaken it` action.
  Never a nag, never a popup over the core loop, never a countdown or a repeated prompt.
- **The daily quest never locks.** It falls back to the static bank, which is why §8 says the bank gets built
  regardless. A user with no key gets quests, thresholds, streaks, gates, raids, leagues, shadows, the private
  track and the whole ladder. **If any of those ever require a key, the build has gone wrong.**
- **Framed as awakening the System, never as setup.** Pasting a key reads as unlocking the real version; a
  form labelled "configure API access" reads as homework. Copy: *"The System reads. It does not yet speak."*
- **Setup is three steps and no card:** aistudio.google.com → create key → paste. State plainly that it is
  free and needs no billing, because most users assume otherwise and stop.
- **The free-tier warning is shown before the paste, not after.** Google may train on free-tier traffic;
  a paid key is excluded. The private track never touches AI on either tier.
- **Store the key in the Android keystore** (`EncryptedSharedPreferences` or equivalent). Never log it, never
  put it in analytics, never send it anywhere but Google's endpoint.
- **`Not now` must be a real answer.** Dismissing it costs nothing and it does not re-ask on a schedule.

### The voice and speech stack

| job | engine | when it runs | why |
|---|---|---|---|
| The System's spoken lines | **ElevenLabs** | **build time**, shipped as audio in the APK | Zero inference cost, zero latency, works offline, no key on the device. The System's lines are a fixed script, so there is nothing to generate live |
| Reading a live AI reply aloud | Android **TextToSpeech** | on device | Free and offline. A dynamic reply cannot be pre-rendered, and paying ElevenLabs per chat message is the fastest way to make the unit economics fail |
| Speech to text, default | Android **`SpeechRecognizer`** | on device, `EXTRA_PREFER_OFFLINE` | Free, no upload, no liability |
| Speech to text, opt-in | **Groq Whisper** | uploads audio | Materially better on Hinglish and Indian English, which the on-device recogniser handles badly. This is a real quality gap for this audience, not a nicety |

**Rules that are not negotiable:**

1. **Private-track voice never leaves the device**, whatever the accuracy setting says. Route it to the
   on-device recogniser unconditionally. Someone speaking a relapse out loud at 1am must never have that audio
   uploaded to a third party.
2. **The Whisper path is opt-in with an explicit disclosure**, off by default, and reversible. The copy names
   what is uploaded.
3. **ElevenLabs stays at build time.** If a line needs to be generated at runtime, that is a signal the line
   should have been in the script.
4. TTS and STT both degrade to silence and typing rather than blocking anything. Voice is a mode, not a
   dependency.

### `READ_SESSION` — the one quest that keeps you on the phone

An in-app reader. N minutes with the reader in the foreground and scroll progress advancing, so it is
**app-initiated tier**, never sensor-proven.

- **Content must be public domain (Project Gutenberg and similar) or originally generated.** Never serve
  copyrighted excerpts. This is the same rule as the art in §16 and it is not optional.
- The AI picks a passage against the user's own record — a focus problem gets something on attention, a sleep
  problem gets something on rest — and it is pre-generated at build time like every other AI-authored string.
- **The passage ends and nothing loads beneath it.** That is the whole justification for a screen-time quest
  inside an anti-screen-time app: twenty minutes of something finite replacing twenty minutes of something
  that never stops. An infinite reader would defeat the app exactly as an infinite feed would.
- Time in the reader counts as reading, not as screen time, in every threshold that measures screen time. The
  same carve-out covers the recording app during a running raid window, for a harder reason: Strava holds the
  foreground for the whole run, so without it a 10 km evening eats the day's screen budget and the app
  penalises somebody for running.

---

## Build order

**Phase 01 is the aura and rank engine as pure functions, plus one test file.** No UI, no persistence, no
Android dependencies. Thresholds, caps, provability tiers, rank transitions, shields, penalty sequencing.
Everything else depends on this being right, and it is the only part that is cheap to test exhaustively.

**Phase 01 is done.** `engine/Aura.kt` and `engine/AuraSelfCheck.kt`, 51 assert-based checks, no framework and
no Gradle yet because pure functions need neither:

```
kotlinc engine/*.kt -include-runtime -d /tmp/engine.jar
java -cp /tmp/engine.jar gakseong.engine.AuraSelfCheckKt
```

Every tunable number is in the `Balance` object. `DECISIONS.md` §29 records the rank arithmetic, which the
design had left ambiguous across three screens. Level is not in the engine: it only rises and gates nothing, so
it arrives when a screen needs it.

**Phase 02 is the Android project and the UI kit.** Single module, `app.gakseong`, compileSdk 35, minSdk 26,
Compose. The engine stays in `engine/` and is pulled in with one `srcDir` line so `kotlinc engine/*.kt` keeps
working.

```
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew assembleDebug
```

**How the design page becomes Compose.** That page draws the phone at `width:min(330px,92vw)` against a 16px
root, so every size in it is proportional to its own width. `Metrics` in `ui/theme/Theme.kt` carries one scale
factor, `screenWidthDp / 330`, and `d()` and `s()` convert design pixels to dp and sp. Text stays in sp so the
reader's font-size setting still works; pixel-exactness is not worth breaking that for.

**Layer order is the stylesheet's z-index, not its markup order.** `bg 0, bg2 0, aura 1, art 2, shade 3,
grain 4, topfade 5, body 8, nav 9`. Compose draws in declaration order, so every screen calls them in that
sequence: `Bg`, `Bg2`, `Aura`, `Art`, `Shade`, `Grain`, `TopFade`, `Body`, `BottomNav`. Reading the HTML top to
bottom puts `topfade` under the art and washes out every header on a bright screen. Do not "tidy" this into a
wrapper that hides the order — the order is the design.

- `ui/theme/Tokens.kt` holds every colour from the stylesheet, including all seven class accents. Nothing there
  was invented; if a value is missing, it is in the CSS.
- `ui/Kit.kt` is the component kit: `Card`, `Cta`, `SystemWindow`, `QuestCard`, `Eye`, `Tag`, `Pill`, `Plate`,
  `Meter`, `Sig`, `XlNumber`, `Wordmark`, `BottomNav`, plus the layer composables. Build a screen out of these
  rather than styling from scratch, the same way the quest bank is built out of the closed verifier set.
- Art is imported into `res/drawable-nodpi/` from `web/`, hyphens turned into underscores. Portraits composite
  with `BlendMode.Lighten`, which is why they are generated on pure black.

Two known gaps, both marked `ponytail:` in the source: `.art.feather`'s radial mask needs a pre-masked bitmap
because Compose cannot blend an offscreen layer, and the grain is random grey rather than fractal noise.

---

## Writing standard for docs in this repo

An audit of the design docs found the prose, not the design, reading as machine-generated. Two habits
caused it and both are capped:

- **Em dash as an all-purpose connector**, standing in for a colon, a full stop and "because" at once. It
  ran at 17–18 per 1,000 words. Keep it under 5.
- **Balanced negation as the default sentence shape** ("craft is the gate, not the idea"), 50+ instances.
  Use it only where the contrast is the actual claim.
