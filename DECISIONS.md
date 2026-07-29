# DESIGN LOCKED — 2026-07-28

**The design phase is closed.** 49 screens, 7 classes, four published artifacts. Everything
below is a decision of record. Changing any of it is a new decision that needs its own reasoning, not a
preference to be applied silently.

**Name: Gakseong (각성) — final, confirmed 2026-07-28.** The in-app voice stays *The System*. Reasoning and
the competitor and availability evidence are in §14. "Solo Leveling" never appears in the name, package id,
or art.

**All four live on anyartifact, public, no login to read.** Base
`https://anyartifact-production.up.railway.app/`

| artifact | what it is | path |
|---|---|---|
| Full App Design | 49 screens, 7 classes, light and dark | `/fpy2EUijO2vo` |
| Feature Spec | every mechanic, its Android verifier, why it survives | `/0XY02nXT3S84` |
| Decision Record | this document plus the instruction log | `/-xE8brb4PuOA` |
| Rank Ceremony | one composition, five classes, one CSS variable | `/Dq0Y8K2gCdoc` |

Also on claude.ai, for anyone already signed in there:
`claude.ai/code/artifact/7a66e316-7246-464f-a701-06d58ad1346f`. That copy and the anyartifact page
`/oMhpqnEKxiSm` are both the 46-screen predecessor. No owner token was kept for `/oMhpqnEKxiSm`, so it cannot
be retired and stays public and stale; the token for the current page is in `PRIVATE-NOTES.md`.

**Repo:** `github.com/bhutianimukul/system-app`, **public**. `CLAUDE.md` there is the feature spec and
build rules, auto-loaded by Claude Code, so a session opened in that repo inherits the whole spec.

**Why the repo is public.** anyartifact sends no `Content-Security-Policy`, so a page hosted there can load
images from a URL, which claude.ai artifacts forbid. That takes the design page from 2.6 MB of inlined
base64 to 230 KB, which fits anyartifact's **500 KB cap** and keeps the art at full quality. The art has to
be publicly readable for that to work, and GitHub Pages cannot serve from a private repo on a free plan, so
the repo itself is public. Two log entries naming specific private habits were generalised before the flip,
and the unabridged wording is in `PRIVATE-NOTES.md`, which is gitignored. Third-party reference art stays
out for the same reason it always did: the publisher images are copyrighted and the rejected generations
have the Solo Leveling title baked into the pixels, both of which contradict §16.

**Canonical home for the text docs is this repo, not anyartifact.** `DECISIONS.md`, `CLAUDE.md` and
`SESSION-LOG.md` render on GitHub, are always current, and have no size cap, so they never need
republishing. The anyartifact pages are convenience copies of the *visual* work. When a doc and a design
page both change, update the repo and republish only the design.

**Two anyartifact traps.** Pages must be a full document with `<!DOCTYPE>` and `<meta charset=utf-8>`, or
Chrome falls back to windows-1252 and every em dash and `·` mojibakes. And **`update_artifact` only works on
the most recently published artifact for a given client** — publish anything else in between and the older
one becomes permanently unreachable through MCP. The escape hatch is the REST API the owner page uses:
`PUT /api/v1/artifacts/{id}/visibility?owner={token}` with `{"visibility":"private"}`, which still works on
older artifacts. Content updates over REST need an `Authorization` header that the owner token does not
satisfy, so an older page can be retired but not edited. **Keep every owner token.** In practice
`update_artifact` refused even on the most recent publish, so treat updating as unreliable: expect to
publish a new page and retire the old one, and batch changes so that happens once per cycle rather than
three times.

**Prose standard for anything added later.** A self-audit found the writing, not the design, read as
machine-generated. Two habits caused it and both are now capped:

- **Em dash as an all-purpose connector.** It was doing the work of a colon, a full stop and "because"
  at once, at 17.6 per 1,000 words in the decision prose and 18.4 in the feature spec. Now under 5.
- **Balanced negation as the default sentence shape** — "*craft is the gate, not the idea*". 31 instances
  in the feature spec, 21 in the decision prose. Cut by two thirds; what remains is where the contrast
  genuinely *is* the claim, such as "30 days, not twelve months".

The product copy was already clean at 4 em dashes per 1,000 words, and the visual design showed none of
the usual generated-design markers. Keep both as they are.

**Still outstanding, and the only things blocking the build:**

- Art from Gemini: Envoy full-body crops, full-body crops for all 7 classes for the profile screen, flat
  small-size rank emblems (the ornate set is verified mush at 48px), a Tanker regenerated on pure black.
- Kotlin phase 01: the aura and rank engine as pure functions, plus one test file. Nothing written yet.

**Blocks launch, not the build:** a proper USPTO trademark search on *Gakseong*, and a Korean speaker's read
on how 각성 sounds. Neither stops Kotlin work starting. Also grab `gakseong.app` — unregistered as of
2026-07-28.

---

# Design decisions — locked

Every decision Mukul made in the design session, with the reasoning that produced it.
Where a decision reversed an earlier recommendation, both are recorded — the reversal is the decision.

---

## 1. Product thesis

**Your phone is the dungeon.** The app is about attention, avoidance and isolation — not fitness.

- Health apps own the body. Nobody instruments **attention, avoidance, isolation**. That gap is the wedge.
- Body metrics stay in as *free garnish* (Health Connect is a day's integration), never as the subject.
- The six original problem buckets collapse to: **root** = attention capture, **amplifier** = isolation,
  **hinge** = sleep, **downstream** = fitness/food, **outcome** = stress.
- **Stress is never a feature.** It is the outcome the app is judged by. It became `CONDITION`.

**Positioning stat:** Gen Z averages 7h 43m screen time a day, rising 4.8% YoY. Teens above 4h are
2× as likely to report anxiety or depression symptoms.

## 2. Platform

**Android only.** Not a preference — a requirement.

- `UsageStats` gives real app names and durations. iOS Screen Time returns **opaque tokens**, so a
  screen-time leaderboard is impossible on iOS and possible on Android.
- Audience (15–30, anime-adjacent, India/SEA/LATAM skew) is overwhelmingly Android.
- **Native Kotlin, not Flutter/React Native.** Health Connect, UsageStats, foreground services and
  Glance widgets are all platform APIs — cross-platform saves nothing when most of the app *is* the platform.

## 3. The core mechanic — catch, don't block

**Detection over prevention.** The System never stops you failing; it records and charges you.

- Blocking is defeated by a determined user in ~15 seconds. Detection cannot be un-done — you cannot un-scroll.
- Also more faithful: the System never once prevented failure in the source material.
- **Query history, don't watch live.** `UsageStats` is retroactive, so settle on app open + widget refresh
  + a 15-minute `WorkManager` job. Animate from last known state so it *feels* live with no persistent service.
  This is what keeps the app alive against OEM battery managers.
- **Speed bump, not a wall** — a dismissible overlay during focus sessions only, on `SYSTEM_ALERT_WINDOW`.
  Tapping through is logged. Converts a reflex into a deliberate choice.

**Containment** (real blocking) is **earned, not default**:
- Trigger: scroll target missed 3 days running, or 5 in 7
- **Permission requested at the trigger, never at onboarding** — the accessibility dialog would kill
  installs shown to a stranger on day one, but lands fine on someone who has already failed four times
- Duration until target cleared 2 days running, max 7. Emergency unlock costs 500 aura, logged.
- Revoking is permitted, recorded, and carries a rank penalty.

**Uninstall prevention is impossible and would be a Play removal.** Instead make leaving *pointless*:
server-side progress, absence counted as failure, guild visibility, no amnesty on return.

## 4. The aura economy

Mukul's design, and the strongest mechanical work in the app.

| | |
|---|---|
| **Threshold** | daily minimum. Miss it → penalty zone |
| **Cap** | daily maximum that counts toward **Rank** |
| **Bonus** | one per day, spawned at random; raises today's ceiling by 120–450 |
| **Overflow** | above the cap builds **Level**, never Rank |

- The band is the design: you must land *inside* it. "More is better" stops being the strategy;
  showing up every day starts being it.
- The cap solves four problems at once: absorbs balance drift in generated quests, caps cheat damage,
  kills grinding as a strategy, and puts a **hard ceiling on how much the app can reward in one day** —
  a stronger safety property than any clamp.
- **Exactly one bonus per day, drawn at random, spawned at a time the user does not pick.** It arrives as a
  push notification and a popup — not a menu you visit and not a list you choose from. **It can only be
  started within 1 hour of spawning**; after that it is gone until tomorrow. Letting it expire costs
  nothing — no penalty, you simply do not get the extra ceiling.
  **Why:** a menu of bonuses is a shop, and a shop gets optimised. A single unpredictable offer with a
  short fuse is an event — it rewards being available rather than being efficient, and it cannot be
  farmed because there is nothing to compare and nothing to pick.
- **Aura ceiling by provability:** sensor-proven = full · app-initiated = medium · declared = low.
  Honest players climb; nobody reaches S-rank without doing things that can be checked.
- **The System assigns aura. The player never does.**

## 5. Progression

- **Level** — private, only rises, the reward. **Rank** — public E→S, rises *and falls*, the stakes.
- **Rank is the adaptive difficulty engine, done in arithmetic** — the thing competitors ship an opaque AI to do.
- **Three tiers per rank** (E-III → E-II → E-I → D-III). 18 promotion moments instead of 6.
- Rank titles: **Awakened · Sentinel · Vanguard · Ascendant · Sovereign · Unbound.**
  Top rank means freedom from compulsion — the thesis as a reward. **"Monarch" avoided** (distinctive coinage).
- **Shields** — 7 consecutive days = 1 shield, max 3. A missed day consumes one instead.
  **Penalty scales with league, never with streak length** — a long streak buys *insurance*, not exposure.
  The intuitive version (lose more the longer your streak) produces fear then uninstall.

## 6. Punishment

**Punish hard, but escalate in sequence, never in parallel.**

```
day 1  streak broken        day 3  aura −200
day 5  demotion warning     day 7  demotion · containment authorised
```

- Simultaneity is the failure mode, not severity. Five penalties at once are illegible — the player
  learns nothing and feels only noise. A countdown frightens; an avalanche numbs.
- **Every penalty screen shows the road back.** Punishment stops producing effort and starts producing
  *exit* precisely when recovery stops looking achievable. `"3 consecutive days restores D-rank."`
- **No penalty anywhere demands additional physical effort.**

## 7. The private track — REVERSED

Originally specced with **no penalties** (reasoning: a penalty on a compulsion is shame with a number
attached, and lands hardest on the youngest users).

**Mukul's decision: strict penalty. Overruled, and correctly** — the no-consequence version made it a toy.
A streak counter with no stakes creates no commitment.

**Final design:**
- Relapse → **streak to zero, shadow progress forfeited, aura −300, daily bonus stops. Rank unaffected.**
- **Silence counts as a break.** Miss the 17:00 check-in and the day logs as broken. Not answering is answering.
- **Daily yes/no check-in per item** — HELD / BROKE, explicit, one tap each.
- **Nothing is auto-detected** except app-mediated vices (gambling, delivery). Everything else is
  self-reported and the screen says so outright.
- **Fingerprint or face required** every time the track opens.
- **Notification never names anything**: *"Check-in pending. One confirmation. Nothing else."* — because
  anyone can read a lock screen. Time configurable, default 17:00.
- **Still never shared** — no guild notification, no leaderboard, no went-dark flag. This is the one line held:
  the penalty is real and it is invisible to everyone else.
- The seeded examples span private compulsions, substances, small self-defeating habits and app categories
  the phone can already see, plus a free-text *"something else, you name it"*. The specific list is in the
  app, not in this repo, because this repo is public and the list reads as a confession when it is not one.

## 8. AI

- **Schema-constrained generation, not free text.** The model fills a verifier grammar it cannot escape:
  a verifier from a closed set of 8, plus numeric params. **It never touches the aura value** —
  `aura = base[verifier] × difficulty(params) × condition_modifier`, a formula we own.
- Free-text quests can't be tiered, so the ladder dies. That's where all eight competitors landed.
- **The prompt is a hint; the validator is the guarantee.** Describing the verifier catalogue raises the
  hit rate. It does not stop a 400-minute focus session. Validate on return, discard failures to the static bank.
- **Clamps live in code, after generation, never in the prompt.** A prompt is a request; a clamp is a guarantee.
  `minutes ≤ 180 · steps ≤ 12000 · distance ≤ 5km`, no fasting verifier exists, no exercise-as-penalty
  verifier exists, `CONDITION: FATIGUED` drops all ceilings 40%.
- **Build-time AI is free forever.** Generate the System's whole voice offline and ship it in the APK.
  Runtime inference is reserved for what can't be pre-computed.
- **Runtime uses:** daily quest generation · weekly post-mortem · onboarding intent→weights ·
  **first three actions derived from the user's own sentence** · mood-aware load · custom standing
  directives · gate naming.
- **BYOK (Gemini).** Removes the only real unit-economics risk. Must be an *unlock, never a requirement* —
  the app is complete without it. Key in Keystore, never logged, never leaves the device.
  **Free-tier Google AI traffic may train their models** — disclosed explicitly before the chat is used.
- **The AI conversation is switchable between text and voice, with push-to-talk as the voice mode** — a
  single centre circle, hold to talk, release to send, tap once for hands-free. Speech is recognised
  **on the device** (Android `SpeechRecognizer`); the **audio never leaves the phone** — only the
  transcript goes to the user's own AI key, and the reply is read back with TTS.
  **Why:** the thing this app is for is often easier to say than to type, and typing a confession into a
  text field at 1am is a higher bar than speaking it. On-device recognition is what makes that
  defensible — a product that ships voice recordings of people's worst days to a server should not exist.
- **Chat rate-limited to 10/day**, with guardrails: no medical advice, no calorie or weight targets,
  no exercise as punishment, crisis language routes to real resources not the System's voice.
- **Never:** AI verification, mood inference from text, unbounded generation, per-interaction inference.

## 9. Social — the only real moat

- **Guilds before global leaderboards.** Small groups solve cold start; a global ladder looks abandoned
  until thousands are active. A guild of six works on launch day.
- **Weekly leagues of ~30 inside your own rank.** Ranks *are* the divisions — one system, not two.
  Competitive from the first thirty users and never stops working.
- **Raid focus** — live session with a friend, mutual failure. **Base aura is never at risk; only the
  raid bonus.** The one who broke it loses base as well. That single line is the difference between
  accountability and resentment.
- **Two classes of objective: proven and on-your-word.** Detectable ones (zero phone use, no social, study
  session, steps, distance) settle themselves from usage history and pay full aura. **Non-confirmable ones —
  meditation, a music session, just sitting with no screen, talking to your parents for 30 minutes, talking
  to a friend, eating a meal with people — cannot be verified by any sensor, so they pay roughly a third**
  (150–180 against 400–450), and when the clock runs out **The System asks once: did you actually do it?**
  **Why:** these are the tasks that matter most and the phone can see none of them. Excluding them would
  make the app only about what is measurable, which is how it becomes another screen-time tracker. Paying
  them the same as proven work would make self-reporting the rational way to farm aura. Low pay plus a
  single honest question is the only stable answer: the honest answer is what keeps it worth anything.
- **Raid objectives are drawn at random from 18** — zero phone use, study session,
  deep work, no social media, no browser, no shorts, walk it out, gym block, screen off and outside,
  night hold, no delivery apps, reply-don't-scroll. Both partners get the same one.
- **Raids are not only focus sessions.** A raid can be a meditation, a music session, idle sitting with no
  phone and no screen at all, or a family conversation — including objectives nothing can confirm.
- **The raid invite IS the install loop.** A link shared over the OS share sheet; if the recipient has no
  app it sends them to Play and drops them into the same raid on first launch.
  Dynamic Links is dead — **App Links + Play Install Referrer API**.
- **The feed ends.** Twenty posts a day, then the System closes it. An infinite scroll inside an
  anti-scroll app defeats the app. Guild-scoped only; global needs moderation, reporting, blocking, 16+ gate.

## 10. The Pact

Opt-in hard mode, separate from the humane default loop.

- 21 / 45 / 75 days. **No carry-forward, no banking ahead, no shields. Fail once → reset to day 1.**
- **The Pact is where the user grants powers their future self can't revoke without cost** —
  containment on, thresholds up one band, shields suspended. Consented in advance by someone motivated,
  applying to someone who won't be. Stronger than a technical lock precisely because it was chosen.
- Difficulty comes from **consistency, never volume**. No pact quest exceeds standard param clamps.
- One floor: `CONDITION: EXHAUSTED` waives a single day per pact.
- **Completion screen** shows before/after on all four measures and the title earned.

## 11. Titles

Short, evocative, **with the earn condition and live progress** — that's what makes them chaseable.

`Pacifist` · `Nightbound` · `Unbroken` · `Ascetic` · `Wayfarer` · `Silent` · `Hermit Broken` ·
`Ironhanded` · `Void-Touched` · `Sovereign` · `Monarch's Equal`

Locked titles show `18 / 30` with a progress bar, and ones above 50% render brighter so the two
nearest surface themselves.

## 12. Motivation

- **The story only moves when you do.** Episodic narrative fragments gated on daily completion, in the
  System's voice, citing real data. Curiosity beats achievement as a *daily* motivator, and it costs
  writing rather than engineering — fragments pre-generate at build time.
- **Polish scales with frequency, not ceremony.** The rank-up ceremony is seen ~6 times; the daily
  completion moment 365. The ceremony drives *acquisition* (it gets screenshotted); completion drives *retention*.
- **Never show a bare number.** Always distance to a named thing — `560 to D · II`, `Shadow in 3 days`.
- **Variable reward on completion, never on opening.** Randomness attached to completing something real
  reinforces the behaviour; randomness attached to opening the app builds the compulsion being treated.
  No daily-login rewards, no timed chests.
- **The System does not praise. It acknowledges.** *"Adequate." "Recorded." "No error noted."*
  This audience is moved by respect earned from something hard to impress, not encouragement.

## 13. Onboarding — 4 asks, everything else staged

**Setup asks four questions and nothing more.** It was eleven screens with two contradictory step
counters (`3 of 8` beside `3 of 7`); most of what it collected was not needed to issue quest one.

**Only screens that ask for something are numbered.** A progress counter is a promise about how much
work is left, so putting one on a reveal spends the goodwill the reveal just bought.

**Minute one — the four asks (~90 seconds):**

1. **Access** — usage, Health Connect, notifications, location. First, because everything downstream is
   read from it. *(The accessibility permission is still requested at its trigger, never here — §3.)*
2. **Intent** — free text, plus **15 quoted statements, pick at least five.** Sentences in the user's own
   voice out-pull one-word chips.
3. **Apps** — **five already selected from the user's own 30 days**, circular icons, not names. This is a
   *confirm*, not a *choose*: the data to make the choice already exists, so asking the user to make it
   from scratch is asking them to do the app's work. Auto-detection covers everything installed later, so
   the list never needs maintaining.
4. **Terms** — what the System may and may not do. The single accept gate; it issues the first quest.

**Immediately after, with no input required:** Reality (`206 hours` in the **last 30 days** — not 12
months, because at install the app can only read what Android retained, and an extrapolated figure would
contradict the screen's whole power: *"the System did not compute this, your phone already had it"*) →
Awakening (class, derived from intent) → Baseline and the first three quests, band set low for you.

**Staged, in context, never in setup:**

| when | what | why not in setup |
|---|---|---|
| Day 2 | **Self rating** — 1–5 on Focus, Sleep, Body, Scrolling, Mind, Company, each beside its measured number | The *gap* is the point, and the gap is more legible once a day of data exists |
| Day 3 | **Private track** — optional, fingerprint and reminder inline | The heaviest, most sensitive ask in the app. Minute one has not earned it |
| Day 7 | **Raise your band** — offered after seven clean days | Nobody can calibrate their own difficulty before trying once |
| Any time | **Weighting** — derived from intent, adjustable in Settings | Derivable, so it is derived. Asking for it is asking twice |

**Why:** the audience is 15–30 and installing on impulse. Eleven screens of forms before any payoff is
where that impulse dies. Everything above still ships — it moved, it was not cut — and each piece now
arrives at the moment it is legible instead of in a queue at minute one.

**New-app detection runs at every app start, for the app's whole life** — not only at setup.
A stored package list matched on device; no package list ever leaves the phone.

## 14. The name — Gakseong

**The product is Gakseong (각성), Korean for *awakening*.** The in-app voice stays **The System**. Every
screen says The System; the Play listing says Gakseong. Because the two are separate, the store identity can
change later without touching a screen.

**Why a Korean word.** The source material is Korean (나 혼자만 레벨업, *Na Honjaman Level Up*, "Only I Level
Up"), so Korean vocabulary signals the genre authentically while staying an ordinary noun nobody can own.
*Gakseong* names the exact moment the app is selling: an ordinary person finding out they have a rank.
각성자 (*gakseongja*, "an Awakened") is the in-universe term, but 각성 itself is everyday Korean, which is
what makes it registrable.

**Why not "Solo Leveling" in the name.** Seven competitors ship and **not one uses the exact phrase** — they
all run near-misses: *Solo Leveller : The System*, *Solo Level : System*, *Solo Mode*, *Solo Grinding*,
*Solo X Player*, *ARISE SOLO*. One ships as *Shadow Lord: Legends Knight* while hiding
`shadow.lord.solo.leveling` in its package id. The reason is that Netmarble ships **Solo Leveling: Arise**,
an officially licensed game, in the same store and category, so the mark is actively commercialised in
exactly the channel we would be using. A deliberate misspelling is not a loophole; *confusingly similar* is
the legal standard for infringement, so a near-miss carries the exposure without the recognition. Per §17,
a takedown at 200 installs costs nothing and one at 50,000 costs the listing, ranking, reviews and install
base at once.

**Where the discovery actually comes from.** Play indexes the long description, not only the title. The
Solo Leveling reference goes there as nominative reference. Being the eighth "Solo Something" would also
sort us in with seven apps we beat on craft, which is the only moat we have.

**Availability, checked 2026-07-28.** No Play Store app named Gakseong. `gakseong.app` and `gakseong.io`
both unregistered. `gakseong.com` was registered 2025-10-05 through a Korean host and serves nothing.
Rejected alternatives: *Honja* (혼자, "alone" — clean, but launches into a cluster of phonetically similar
apps: Hona, Huna, Honey Jar), *Gatebound* (`.app` taken), *Solo Leveller* (an existing live app, with our
subtitle).

**Title:** `Gakseong — Level Up IRL`, inside Play's 30-character limit.

**Open, and do not skip it:** no authoritative trademark clearance yet. Justia blocks automated queries and
USPTO search needs a session, so "nothing surfaced in general search" is a weak signal rather than
clearance. Run USPTO properly, or pay for one, before spending on the icon and listing. Also worth a Korean
speaker's ear on whether 각성 reads naturally or slightly clinical to a 20-year-old.

## 15. Cut, and why

| Cut | Reason |
|---|---|
| Public confessions | Anonymous UGC + minors + no moderation team = delisting. Survives as a *private* chat |
| Calorie logging | Granularity kills it, not manual entry. Four booleans capture most of it |
| Camera meditation tracking | Fakeable, spends a camera permission on nothing |
| Call-log verification | Play-restricted permission. `READ_PHONE_STATE` gives duration without it |
| Strava-style feed | You will not beat Strava at Strava |
| Real money stakes | Payments + minors + regulation is a wall |
| Native watch app | Health Connect already carries watch data |
| One global leaderboard | Looks abandoned. Leagues of 30 work from user 30 |
| Infinite feed | Defeats the app's own thesis |
| Uninstall prevention | Impossible on a consumer install; attempting it is a Play *removal* |
| Free-text AI quests | Untierable, so the ladder dies. Schema-constrained generation replaces it |

## 16. IP

- **Free:** gate, dungeon, hunter, awakening, raid, guild, rank E–S, shadow, penalty zone, mana —
  genre vocabulary, most of it older than the series. **Real geography is free and better.**
- **Not free:** character names, "Solo Leveling", "Shadow Monarch", named guilds, the specific System
  window design.
- **The risk scales with success.** Listings currently using the names openly are the ones nobody noticed.
  It is a tax you only pay once it's working.
- Original class roster instead of a borrowed one — you own the asset, and no notice can take it.

## 17. Honest assessment

- **There is no technical moat.** Every mechanic is rebuildable in a quarter.
- What defends: **taste** (acquisition) · **switching cost** (progression compounds per user) ·
  **the guild graph** (the only genuine network effect, defends both).
- Moats are earned after launch, not designed before it. The pre-launch questions are wedge and retention.
- **Craft is the gate, not the idea.** Eight Solo Leveling apps ship and none is beautiful. That's the
  opening — and it means the real question is whether this one can be.
- Lowest-ARPU segment in mobile. Retention mechanics are strong by design; monetisation is genuinely hard.
- **Test the screenshot before writing the code.** The riskiest assumption is aesthetic, not functional.

---

## 18. Screen-off double-counting

- **While a focus session or a raid is running, phone-down / screen-off credit pauses.** Turning the screen
  off during a session is fine and encouraged — the session timer keeps running — but those minutes pay the
  session, not the phone-down quest. The pause is shown on the Focus Active screen rather than left implicit.
  **Why:** the two rewards overlap perfectly. Without the pause the optimal play is to start a focus session
  and put the phone down, collecting twice for one behaviour, which quietly doubles the daily ceiling the
  cap exists to hold.

## 19. Cold start, growth and instrumentation

Five additions Mukul specified on 2026-07-28, after the design lock.

**Thin leagues are padded with shadow pacers, not fake people.** A division under thirty real hunters gets
System-run shadows holding a fixed pace, each carrying a `◇` and the label `pacer`, with a card on the screen
stating plainly that they are not people and are never counted as members. Every real hunter who joins
replaces one.
**Why:** the goal was that a new user should not feel alone, and fabricated usernames achieve that until
someone notices the member who never posts in the guild feed. The ladder is the one thing in this app that
has to be trustworthy, so the padding is labelled. It also fits the fiction, since shadows are already what
this world is made of. *(Rejected: the original design already shipped unlabelled fake names in the league
renderer. They were replaced.)*

**A raid partner can be a shadow.** For when nobody is free. The shadow never breaks, so only the user can
fail, and it pays **+180 against +450** for a person.
**Why:** the raid bonus is paying for accountability, not for the timer. A partner who cannot be let down is
worth less, and pricing it that way keeps the human raid the thing worth reaching for.

**Referral: one rewarded summon a week, +600 aura, paid when the invitee clears day 3.**
**Why day 3:** paying on install rewards installing the app twice. Paying on day 3 rewards bringing someone
who stays. Same device, same person, or an uninstall-reinstall pays nothing.
**Why aura and not Level:** I argued for Level-only so that social growth could never buy ladder position.
**Mukul overruled it, and the daily cap already absorbs most of the risk** — referral aura sits under the
same ceiling as everything else, so on a day already at cap it rolls into Level anyway.

**Analytics: Firebase, on by default, switchable off.** Screen and feature counts, plus crash stack traces.
Never a package name from the user's app list, never a duration, never screen contents.
**The private track emits nothing at all** — not a screen view, not a count, not "a check-in happened" — and
that exemption is unconditional rather than tied to the toggle.

**Referral tracking needs no email and no phone.** The link carries an opaque code from the inviter's
Anonymous Auth UID; the **Play Install Referrer API** hands the app that code once on first launch; the pair
is a write-once Firestore doc keyed on the invitee's own anonymous UID. Three cleared thresholds inside
fourteen days, not necessarily consecutive, releases the aura.
**Why no identifier:** collecting a phone number for an audience that includes 15-year-olds, in an app with a
private track, is a liability with almost no fraud benefit — free mailboxes and cheap SIMs are easier to get
than three days of real threshold clears. **The day-3 gate is the anti-fraud mechanism**, not just a retention
nicety: a fake account has to defeat the same `UsageStats` and Health Connect `dataOrigin` checks as real play,
which costs more than 600 capped aura is worth. Residual hole stated plainly: anonymous UIDs rotate on
reinstall, so someone with a spare device can farm roughly one a week. Hardening without PII is Firebase App
Check plus a rate limit in security rules. Real verification would need one Cloud Function, which breaks §8's
"no server code" — a trade to make if abuse appears, not upfront.

**Coming Soon with an email waitlist.** Four sealed gates — Cloud Sync, iOS, Guild Wars, Watch Face — each
with a one-line reason, and an optional email field. One mail per launch.

**Both of the above reverse §8's "no account, no email, no cloud".** Mukul confirmed the trade deliberately.
The consequences are real and are not optional:
- The Access screen no longer claims nothing leaves the device. It now names the three things that do: guild
  identity, anonymous feature counts, crash traces.
- A **privacy policy URL** and a **Play Data Safety declaration** become mandatory, because both are Play
  requirements the moment anything leaves the device.
- Collecting an email from an audience that includes 15-year-olds means the listing has to be honest about
  it, and the field stays optional with skipping it costing nothing.

## 20. Branding in the app, and the splash

**The name is visible in exactly two places:** the splash, and a small wordmark in the Home header —
`각성` with `GAKSEONG` beneath it as a spaced-out sub-label. Everywhere else the app still speaks as
**The System**.
**Why so restrained:** §14 keeps the store identity and the in-app voice separate so either can change
without touching the other. A wordmark is a mark, not a voice. The System never refers to itself as Gakseong
in a sentence, because the fiction is that it is a system, not an app someone shipped.

**Home's header leads with the wordmark** as a vertical lockup — `각성` at 1.15rem in ink with an accent
glow, `GAKSEONG` beneath it in the class accent at 0.5rem with wide tracking — and the *Daily Quest* eyebrow
sits on its own line below. Rank and level moved to a right-aligned stack.

**This was built wrong once and it is worth recording why.** The first attempt put the wordmark inline at
0.52rem in `--dim`, sharing a row with `LV 34`, `14d streak` and `D · III`. It was technically present and
effectively invisible: at that size and colour, over the hero art, it read as a fourth metadata chip rather
than a name. **A wordmark needs to differ from its neighbours in kind, not just in content** — its own line,
its own scale, its own weight. The fix was a vertical lockup with a glow and a drop shadow so it survives a
busy background, and moving the stat chips into a stack so nothing competes with it.

**The splash is the platform's, not ours.** Android 12+ shows a system splash whether or not you ask for one,
so it is built with `androidx.core.splashscreen` and customised. **A custom splash Activity layered on top
produces a visible double-splash** — the most common way this feature ships broken.

- Icon animation is an `AnimatedVectorDrawable` in `windowSplashScreenAnimatedIcon`, designed to the
  system's ~1000 ms budget because anything longer is simply cut off.
- `setOnExitAnimationListener` takes over the `SplashScreenView` for the brand moment: the hangul resolving
  out of a blur, an aura ring expanding, then the gate splitting to reveal Home. Around 600 ms.
- `setKeepOnScreenCondition` holds only until the first frame is ready. **Never hold a splash to finish an
  animation.** Cold start is already the worst moment in the app and delaying first paint is a retention cost.
- Reduced motion is honoured through `Settings.Global.ANIMATOR_DURATION_SCALE`; at 0 it cross-fades straight
  to Home. The design file mirrors this with `prefers-reduced-motion`.
- The reveal is decoration over an already-rendered Home, so a failure anywhere in the sequence still lands
  the user on a working screen.

## 21. Telling the user what their class means

The Awakening screen already proved *why* a class was assigned — three lines pulled from the user's own
record. It never explained what being a Fighter actually **means** for the play ahead, so a **What a Fighter
is** block now sits under the evidence, per class:

- **How it plays** in one line. Fighter moves; Ranger leaves the house; Sage sits still; Healer sleeps;
  Assassin starves the feeds; Tanker shows up; Envoy talks to people.
- **Quest weighting** as three real bars summing to 100, so the user can see their quests will skew to Body,
  or Attention, or People. This is the same weighting §13 derives from intent rather than asking for.
- **First shadow** and a **title to chase** — Bull/Ironhanded, Wolf/Wayfarer, Knight/Ascetic,
  Serpent/Nightbound, Wraith/Void-Touched, Beast/Unbroken, Spearman/Pacifist. Per §11, a title the user can
  see and aim at is what makes them play for it.
- **The trade**, stated plainly. Every class is worse at something: Sage loses a whole day to one broken
  session, Envoy runs half on your word so it pays less, Assassin has no excuses because usage history
  proves everything.

**Why the trade line matters:** without it the class reads as flattery, and an override becomes arbitrary.
With it the assignment is an argument the user can disagree with — which is the only reason the override in
§13 means anything.

## 22. Do Not Disturb during sessions

**A session turns DND on and takes it off again when it ends** — focus sessions, the night gate, raids.
Shown on the Focus screen beside the screen-off pause, and governed by two Settings rows.

- **Built on `AutomaticZenRule`, never `setInterruptionFilter`.** A registered rule appears in system Settings
  under the app's own name so the user can edit or kill it, and a condition-driven rule **expires by itself if
  the app dies mid-session.** Setting the filter by hand can leave someone in permanent silence after a crash,
  which would be the worst bug this feature could ship.
- **`ACCESS_NOTIFICATION_POLICY` is a special access**, granted through a Settings screen rather than a
  runtime dialog. Requested **at the first session, never at onboarding**, per §3. Not Play-restricted, so no
  declaration form.
- **Calls always ring.** The rule allows calls and repeat callers.
  **Why this is not negotiable:** the audience includes fifteen-year-olds, and an app that silences a parent
  for forty-five minutes is indefensible whatever it does for focus. It also matches the dialer whitelist that
  already exists during focus sessions.
- **Read the prior filter and restore it.** Someone who already had DND on for their own reasons keeps it.
- **Skipped during the night gate if system Bedtime mode already owns DND.** Do not fight the platform for
  the same setting.
- Off until the grant exists, and a refused grant degrades to an ordinary session rather than blocking one.

**Why add it at all:** the design catches rather than blocks, and DND fits that. It removes the interruption
that causes the failure instead of preventing the user from acting. A notification is the most common reason a
focus session dies, and silencing one is not the same as locking the phone.

## 23. The voice and speech stack

**ElevenLabs generates the System's spoken lines at build time**, shipped as audio in the APK. The System's
script is fixed, so there is nothing to generate live: zero inference cost, zero latency, works offline, no
key on the device. This is §8's build-time principle applied to audio rather than text.
**Live AI replies are read by Android `TextToSpeech`** instead. A dynamic reply cannot be pre-rendered, and
paying per chat message is the fastest way to break the unit economics the BYOK decision exists to protect.

**Speech to text has two paths.** Android `SpeechRecognizer` on device by default. **Groq Whisper as an
opt-in upgrade**, which uploads audio.
**Why offer the upload at all:** the on-device recogniser handles Hinglish and Indian English badly, and this
audience speaks both. That is a real quality gap rather than a nicety, and voice mode is worthless if the
transcript is wrong.
**Why it stays opt-in:** it reverses the "the recording never leaves your phone" line the voice screen used
to carry, so the copy now names both engines and what each one does.

**Two lines held:**
- **Private-track voice never leaves the device**, whatever the accuracy setting says. Someone speaking a
  relapse out loud at 1am must never have that audio uploaded to a third party. The routing is
  unconditional, not a preference.
- **ElevenLabs stays at build time.** A line that needs generating at runtime is a signal it should have been
  in the script.

## 24. Reading — the one quest that keeps you on the phone

**`READ_SESSION`:** an in-app reader, N minutes foregrounded with scroll progress advancing.
**App-initiated tier**, never sensor-proven, because the phone can only confirm the reader was open.

**Why a screen-time quest belongs in an anti-screen-time app:** because **the passage ends and nothing loads
beneath it.** Twenty minutes of something finite replaces twenty minutes of something that never stops. That
is the same principle as the feed ending at twenty posts in §9. An infinite reader would defeat the app
exactly as an infinite feed would, so the reader must never autoload.

- **Content is public domain or originally generated. Never copyrighted excerpts** — the same rule as the art
  in §16, and for the same reason.
- The AI picks against the user's own record: a focus problem gets something on attention, a sleep problem
  something on rest. Pre-generated at build time like every other AI-authored string.
- Time in the reader counts as reading, not as screen time, in every threshold that measures screen time.
- It also works as a raid objective, since two people can read the same passage.

**Aura Exchange is in Coming Soon, cosmetics only.** Banked aura buys art, sigils, themes and shadow skins.
**Nothing that touches a threshold, a shield or a rank.** Aura measures what the phone proved you did; the
moment it can be spent on an advantage, the ladder stops meaning anything and every earlier decision about
provability is wasted.

## 25. The AI gate — lock the feature, never the app

Five things need a key: generated quests, the weekly post-mortem, chat, reader passage selection, title
generation. Each shows a **locked state in place**, with a blurred sample of the real output and one
`Awaken it` action.

**This is bounded by §8's line that AI is an unlock and never a requirement.** The daily quest never locks —
it falls back to the static bank, which is exactly why the bank gets built regardless. A user with no key gets
quests, thresholds, streaks, gates, raids, leagues, shadows, the private track and the entire ladder.
**If any of those ever require a key, the build has gone wrong.**

- **A locked state, not a nag.** No modal over the core loop, no countdown, no repeated prompting.
  `Not now` is a real answer that costs nothing and does not re-ask on a schedule. An app that gates its
  actual value behind a key it did not pay for has no standing to pester anyone.
- **Framed as awakening the System, never as setup.** *"The System reads. It does not yet speak."* Pasting a
  key reads as unlocking the real version; a form labelled "configure API access" reads as homework. This
  audience already lists tech as an interest, so the paste itself is not the barrier — the framing is.
- **Three steps and no card:** aistudio.google.com, create key, paste. Say plainly that it is free and needs
  no billing, because most people assume otherwise and stop there.
- **The free-tier warning comes before the paste.** Google may train on free-tier traffic and a paid key is
  excluded, so the user decides with that in hand rather than discovering it later. The private track never
  touches AI on either tier.
- **The key lives in the Android keystore**, never logged, never in analytics, never sent anywhere but
  Google's endpoint.

## 26. Running — the raid syncs an hour, not a place

Feedback from a runner: a raid drawn at random and answered on the spot assumes the phone is the whole world.
Running happens somewhere specific, at a time the runner already chose, and two hunters cannot both be at a
track because a timer said so.

**So a running raid commits a window and never a location.** Both hunters agree on 18:00 to 19:00. Each runs
wherever they actually run. Neither ever learns where the other was.

- **One new objective in the eighteen**, on the existing `DISTANCE` verifier. The target scales with rank like
  every other threshold. No new verifier, no new permission, no map, no route, no GPS trace, and
  `LOCATION_CHECKIN` keeps doing only what it already does.
- **Sensor-proven tier**, so it pays full and reaches S-rank. Health Connect carries `dataOrigin` and the
  check is the one steps objectives already pass.
- **Settle is retroactive, not at the whistle.** Strava, Nike and Samsung Health write to Health Connect on
  sync, which can land minutes after a run ends, so a query fired at window close reads zero kilometres. The
  window closes, the 15-minute `WorkManager` job re-checks for up to 30 minutes, and the screen holds a
  `settling` state. Declaring a failure the app may have to reverse is worse than making somebody wait. This
  is §2's retroactive settle doing the job it was designed for.
- **The economy is untouched.** Base aura never at risk, only the raid bonus; whoever broke it loses base as
  well. Human partner +450, shadow pacer +180.
- **The pacer was already in the design.** §9 says a shadow holds a fixed pace and never breaks, so only the
  user can fail. For a run that pace is the target distance over the window, shown as `6:00/km`. A runner with
  nobody free still gets a raid, which is the entire reason shadow raids exist.
- **Users who have not granted Health Connect never see the objective drawn**, the same gate steps objectives
  already sit behind.

### The session starts where the runner already starts it

Gakseong never asks anybody to press start in Gakseong. A runner starts their run in Strava, or Nike, or
Samsung Health, because that is where their history lives and they were going to open it anyway. The window is
the commitment; the recording belongs to their own app. This is the same posture as §3 throughout: the app
reads the record rather than owning the activity.

- **`Start in Strava` is a launch intent, not an integration.**
  `PackageManager.getLaunchIntentForPackage` on the package that wrote their last distance record, which
  Health Connect already reports as `dataOrigin` and which never leaves the device. No URI scheme to guess, no
  OAuth, no API. A user whose runs have no recorded origin simply gets no button and the window alone.
- **A running raid is not a focus session.** No foreground service, no speed bump, no return grace, and no
  penalty of any kind for leaving Gakseong. Leaving is the intended behaviour, and the existing session
  machinery would read it as abandonment.
- **Foreground time in the recording app during a committed window does not count as screen time,** in any
  threshold that measures screen time. Without this the run defeats its own player: Strava holds the
  foreground for the length of the run, `UsageStatsManager` counts all of it, and a 10 km evening eats the
  daily screen budget. §24 already carves out the reader on a softer argument than this one.
- DND during the raid follows §22 unchanged. It suppresses notifications, leaves media alone, and always lets
  calls ring, so a runner keeps their music and their mother.

**Deliberately not distinguishing a run from a fast walk.** The committed window is the filter. Five
kilometres inside an hour is real work whichever gait produced it, and `ExerciseSessionRecord` type checking
is the upgrade path if pace gaming ever shows up.

**Rejected: the running vertical.** Named tracks with check-ins, route or segment ladders, a runner class and
its own screens. Two costs killed it. User-named places need moderation, which is the wall that keeps the feed
guild-scoped in §9, and a route ladder invites GPS spoofing, which is the class of problem `dataOrigin`
currently keeps out. A distance league beside the aura league was rejected for now on a thinner argument: it
needs its own balancing and its own pacers, and one ladder that means something beats two that dilute.

## 27. Strava — read it, do not integrate it

Asked directly whether the Strava API is worth wiring in, given how many features it could carry. It is not,
and the reason is not effort.

**Strava's Android app already writes time, distance and calories from GPS activities into Health Connect.** A
runner who uses Strava therefore delivers their run to Gakseong with no OAuth, no tokens, no account and no
agreement to accept, identically to a runner on Nike, Garmin, Fitbit or Samsung Health. `dataOrigin` comes
back as Strava's own package, so the settle screen can say `5.02 km · via Strava` and be telling the truth.
That is the whole integration: one conditional on the origin string.

**Why the API itself is the wrong trade for this app.** The API Agreement says *"Strava Data related to other
users, even if such data is publicly viewable on the Strava Platform, may not be displayed or disclosed,"* and
separately that *"you may not create applications that compete with or replicate Strava functionality."* A
raid where you can see your partner's distance is the first clause. So is a distance ladder, and so is a guild
feed post about somebody's run. Gakseong is a ladder, so the API forbids the half of it that would matter
here. What stays permitted is personal-only: your own route on your own screen, your own segment records. That
buys map tiles and a polyline renderer and moves nothing.

OAuth would also introduce an account into an app whose whole posture in §13 is that there is no account.

**The one real temptation was webhooks.** An activity-finished callback would delete the settle latency above.
It needs a public HTTPS endpoint, which is server code, which §8 does not have. Thirty minutes of `settling`
copy is cheaper than a backend.

**Known ceilings, stated.** Health Connect receives GPS activities, so a treadmill run typed into Strava by
hand may never arrive. The sync is Android-only, which costs nothing here. Press coverage of the November 2024
agreement update also reported a ban on using API data for AI or ML training; that clause is not in the
current agreement text, so it is recorded as unverified rather than as fact.

## 28. Sharing an ascension — the invite with a picture

Feedback: the app should let you share a screenshot the way Strava does. Strava's real trick is that the image
*is* the invite, and §9 already has the install loop, so this is that loop with a picture attached.

**Trigger: rank ascension, and nothing else yet.** It is the highest-status moment in the app, the ceremony art
already exists, and one moment is enough to learn whether anybody shares at all. Gate clears and raid clears
follow only if they do.

**Flow:** ceremony, then `Share this ascension`, then a preview screen showing the card at 9:16, then the
system share sheet. The preview is not politeness. Capture needs the composable laid out on screen anyway, and
it means nobody posts something they have not seen.

**Render:** `rememberGraphicsLayer()`, `record {}`, `toImageBitmap()`, a PNG in `cacheDir`, handed out through
`FileProvider` with `FLAG_GRANT_READ_URI_PERMISSION`. No storage permission and no new dependency. The card is
the real ceremony composable, so the two cannot drift apart.

**What the card may carry, as an allowlist:** the class portrait, rank before and after, level, streak days,
one line from the fixed System script, the `각성 GAKSEONG` wordmark, and `gakseong.app/s/<code>` in legible
type.

**What it may never carry:** a package name, a duration, any screen-time number, an app icon from the device, a
guild member's name, or anything at all from the private track. §10's three-things rule has no "but the user
chose to" exemption. `Instagram · 4h 12m` is the card that gets posted and then regretted, and the app should
not be able to produce it. **No share affordance exists anywhere in the private track,** which follows from §7
without needing a new rule.

**The link is baked into the pixels and repeated in `EXTRA_TEXT`.** Instagram Stories drops the text when an
image is attached, which is why Strava puts its branding in the image. WhatsApp carries both, and WhatsApp is
the channel that matters for this audience.

**Ceiling: the code dies on reinstall.** Anonymous Auth UIDs rotate, so a card posted before a reinstall
carries a code that credits nobody. The link still installs the app, so a dead code costs the credit rather
than the install. Not worth a server to fix.

**If capture fails, share text only.** The ceremony never blocks on a bitmap.

**Drawn on 2026-07-30, taking the design page from 46 screens to 49:** `runraid` (window commit, with the
`Start in Strava` launch intent and the not-a-focus-session rule on the screen), `runsettle` (the retroactive
verdict, `5.02 km · via Strava`), and `share` (the 9:16 card in its preview, with the allowlist stated on the
screen next to it). The Invite screen's stale `system.app/r/` link was corrected to `gakseong.app/r/` in the
same pass.

## Live artifacts

- Feature spec / roadmap — https://claude.ai/code/artifact/8fa0df80-201f-4380-88dc-cb8f3c98ffb3
- Full app design, 38 screens × 7 classes — https://claude.ai/code/artifact/7a66e316-7246-464f-a701-06d58ad1346f
- Rank ceremony standalone — https://claude.ai/code/artifact/fe312f0c-032a-42d3-bb9e-6ed251cdc82a

Assets: `assets/` (100 files) · `assets/MANIFEST.md` · `assets/PROMPTS.md`
