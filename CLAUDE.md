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
   Play requirements the moment anything leaves the device.

---

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

**Night gate** is `SCREEN_OFF_BLOCK` across a configurable 00:30–06:00 window, checked once after it
closes. **Focus session** is a foreground service bounded by session length, dialer whitelisted, with a
~10s return grace so a mis-tap does not cost the session.

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

---

## Stack

- Kotlin, Jetpack Compose, `WorkManager` (15-minute floor), Glance for widgets
- Firebase Anonymous Auth, Firestore, FCM. No server code
- Health Connect (Google Fit is deprecated)
- AGSL `RuntimeShader` for effects on Android 13+, `RenderEffect.createBlurEffect` on 12+, degrade to
  static art below
- `VibrationEffect` composition primitives. Haptics fire even on silent
- AI is bring-your-own-key (Gemini). The app must be fully complete with zero AI configured. Build-time
  generation for the System's voice, shipped in the APK, so there is no inference cost or latency

---

## Build order

**Phase 01 is the aura and rank engine as pure functions, plus one test file.** No UI, no persistence, no
Android dependencies. Thresholds, caps, provability tiers, rank transitions, shields, penalty sequencing.
Everything else depends on this being right, and it is the only part that is cheap to test exhaustively.

Nothing has been written yet.

---

## Writing standard for docs in this repo

An audit of the design docs found the prose, not the design, reading as machine-generated. Two habits
caused it and both are capped:

- **Em dash as an all-purpose connector**, standing in for a colon, a full stop and "because" at once. It
  ran at 17–18 per 1,000 words. Keep it under 5.
- **Balanced negation as the default sentence shape** ("craft is the gate, not the idea"), 50+ instances.
  Use it only where the contrast is the actual claim.
