package app.gakseong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.data.Repo
import androidx.compose.ui.platform.LocalContext
import app.gakseong.sense.RETAINED_DAYS
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.gakseong.sense.HEALTH_PERMISSIONS
import app.gakseong.sense.HealthAvailability
import app.gakseong.sense.hasHealthGrants
import app.gakseong.sense.hasUsageAccess
import app.gakseong.sense.healthAvailability
import app.gakseong.sense.usageAccessIntent
import app.gakseong.ui.*
import app.gakseong.ui.theme.Bad
import kotlinx.coroutines.launch
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.mix

// Onboarding is four asks and about ninety seconds. Only screens that ask something are numbered, so Splash,
// Reality, Awakening and Baseline carry no step counter: they cost the user nothing.

/** `data-s="splash"`. The real one is the platform SplashScreen; this is the handoff it hands off to. */
@Composable
fun SplashScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg()
        Grain()
        Column(
            Modifier.matchParentSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(m.d(62))
                    .clip(RoundedCornerShape(m.d(17)))
                    .border(1.dp, p.line2, RoundedCornerShape(m.d(17)))
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_app),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                )
            }
            Gap(22.4)
            Text("각성", style = t.display(48).copy(letterSpacing = 0.04.em, textAlign = TextAlign.Center))
            Gap(8)
            Text("GAKSEONG", style = t.wordmarkLatin.copy(fontSize = m.s(11.2)))
            Gap(17.6)
            Tag("The System is reading your record")
            Gap(28.8)
            Box(
                Modifier
                    .width(m.d(120))
                    .height(m.d(2))
                    .clip(CircleShape)
                    .background(p.line2)
            ) {
                Box(Modifier.fillMaxWidth(0.42f).height(m.d(2)).background(p.hot))
            }
        }
    }
}

/** `data-s="welcome"` — the thesis, once, before anything is asked. */
@Composable
fun WelcomeScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val nav = LocalNav.current
    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_sigilup, top = -0.04f, left = -0.04f, width = 1.06f)
        Shade(0f to 0.57f, 0.14f to 0f, 0.40f to 0f, 0.56f to 0.88f, 0.68f to 1f)
        Grain()
        TopFade()
        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Filler()
                Box(
                    Modifier
                        .size(m.d(56))
                        .clip(RoundedCornerShape(m.d(15)))
                        .border(1.dp, p.line2, RoundedCornerShape(m.d(15)))
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_app),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                    )
                }
                Gap(17.6)
                Text("YOUR PHONE IS\nTHE DUNGEON.", style = t.display(40).copy(textAlign = TextAlign.Center))
                Gap(16)
                Plate(Modifier.width(m.d(250))) {
                    Text(
                        buildAnnotatedString {
                            append("You already know where the hours go. ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                append("The System will start counting them.")
                            }
                        },
                        style = t.body.copy(fontSize = m.s(13.1), textAlign = TextAlign.Center),
                    )
                }
                Filler()
                Cta("Awaken", onClick = { nav("perms") })
                Gap(12.8)
                Tag("Already have a record? Restore")
                Gap(19.2)
            }
        }
    }
}

/** `data-s="perms"` — ask 1 of 4. Location is the only optional one, and it is asked for at the gate. */
@Composable
fun PermsScreen() {
    val m = LocalMetrics.current
    val t = LocalType.current
    val nav = LocalNav.current
    val context = LocalContext.current

    // Re-read on every resume: all three of these are granted in a screen the app does not own, so the only
    // moment the answer can have changed is on the way back.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var grants by remember { mutableStateOf(readGrants(context)) }
    var resumes by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                grants = readGrants(context).copy(health = grants.health)
                resumes++
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    // Refreshed on every resume and after the permission sheet closes, both of which bump `resumes`.
    LaunchedEffect(resumes) { grants = grants.copy(health = hasHealthGrants(context)) }

    val healthLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { resumes++ }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { resumes++ }

    Screen {
        Bg()
        Bg2()
        Aura(0.66f, 0.14f, top = 0.10f, left = 0.17f, alpha = 0.36f)
        Grain()
        TopFade()
        Body {
            AskHeader("Ask 1 of 4", "Access")
            Gap(22.4)
            Text("THE SYSTEM NEEDS\nTO SEE.", style = t.display(27.2))
            Gap(11.2)
            Tag("Nothing leaves your device")

            Gap(17.6)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(7)),
            ) {
                PermRow(
                    "◉", "Usage access", "Which apps, and for how long", "required",
                    granted = grants.usage,
                ) { context.startActivity(usageAccessIntent()) }
                PermRow(
                    "♡", "Health Connect", "Steps, distance and sleep", "required",
                    granted = grants.health,
                    unavailable = grants.healthAvailability != HealthAvailability.AVAILABLE,
                ) { healthLauncher.launch(HEALTH_PERMISSIONS) }
                PermRow(
                    "◈", "Notifications", "The daily quest at midnight", "required",
                    granted = grants.notifications,
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                // §Verifiers: passive geofencing needs a restricted permission, so location is asked for at the
                // gate, on a tap, and never here.
                PermRow("⌖", "Location", "Asked at the gate, not here", "optional", granted = false)
                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 13.6) {
                    Text(
                        "No account and no email to start. Your usage history, your app list and your private " +
                            "track stay on this phone. Anonymous counts of which features get used do leave, so " +
                            "the app can be improved, and never what you did, never which apps, never a private " +
                            "check-in. Switchable off in Settings.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }
            }
            // ponytail: navigates only. The actual grants are phase 04, where each row gets its own intent and
            // its real state. Wiring the route now keeps the four asks walkable end to end.
            Cta("Grant access", onClick = { nav("intent") })
            Gap(19.2)
        }
    }
}

/**
 * `data-s="diag"` — Reality. Unnumbered, because it asks nothing. The number is not computed by the app: Android
 * already had it, which is the entire rhetorical weight of the screen.
 */
@Composable
fun RealityScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val nav = LocalNav.current
    val context = LocalContext.current
    val usage by rememberUsage(RETAINED_DAYS)

    // Thirty days is what Android kept. The System did not compute this, which is the whole point of the screen.
    val hours = usage?.takeIf { it.available }?.let { (it.totalForegroundMs / 3_600_000.0) }
    val top = usage?.perPackageMs.orEmpty().entries.sortedByDescending { it.value }.take(4)
    val restMs = usage?.perPackageMs.orEmpty().entries.sortedByDescending { it.value }.drop(4).sumOf { it.value }

    Screen {
        Bg()
        Bg2()
        Aura(0.96f, 0.22f, top = 0.08f, left = 0.02f, alpha = 0.42f, color = Bad)
        Grain()
        TopFade()
        Body {
            Eye("Before we begin", color = Bad)
            Gap(20.8)
            Tag("Last 30 days, your phone", t.tag.copy(letterSpacing = 0.32.em))
            Gap(8)
            XlNumber(hours?.let { "%.0f".format(it) } ?: "—", designPx = 70.4)
            Gap(1.6)
            Text("HOURS AWAKE\nON A SCREEN", style = t.display(25.6))
            Gap(8.8)
            Tag(wakingLifeLine(hours, usage), t.tag.copy(color = p.soft, letterSpacing = 0.16.em))

            Gap(19.2)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                SystemWindow {
                    Tag("Where it went", t.tag.copy(color = p.hot))
                    Gap(10.4)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(9.6))) {
                        if (top.isEmpty()) {
                            // A zero list would read as a clean month. Say which of the two it actually is.
                            Tag(
                                if (usage == null) "Reading your record" else "No usage access granted",
                                t.key,
                            )
                        } else {
                            top.forEach { (pkg, ms) ->
                                WhereRow("◉", appLabel(context, pkg), hoursLabel(ms))
                            }
                            if (restMs > 0) WhereRow("·", "Everything else", hoursLabel(restMs))
                        }
                    }
                }
                Card(Modifier.fillMaxWidth(), padding = 13.6) {
                    Text(
                        buildAnnotatedString {
                            append("Thirty days is what Android kept. ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                append("The System did not compute this — your phone already had it.")
                            }
                            append(" It has been keeping this record whether or not anyone read it.")
                        },
                        style = t.body.copy(fontSize = m.s(12.5)),
                    )
                }
            }
            Cta("I want it back", bad = true, onClick = { nav("class") })
            Gap(17.6)
        }
    }
}

/** `data-s="apps"` — ask 3 of 4. A confirm, not a choose: five are already picked from the user's own 30 days. */
@Composable
fun AppsScreen() {
    val m = LocalMetrics.current
    val t = LocalType.current
    val nav = LocalNav.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // §Onboarding: five already selected from the user's own 30 days. A confirm, not a choose.
    val usage by rememberUsage(RETAINED_DAYS)
    val candidates = remember(usage) {
        usage?.perPackageMs.orEmpty()
            .entries
            .filterNot { it.key == context.packageName || it.key in SYSTEM_PACKAGES }
            // Under a minute a day is not where anybody's hours went, and a list of them makes the screen's
            // whole claim look silly.
            .filter { it.value / RETAINED_DAYS >= 60_000L }
            .sortedByDescending { it.value }
            .take(12)
            .map { it.key to it.value }
    }

    // The top five are ticked before the user arrives. Their own data chose them, which is the entire argument
    // the screen makes, and nothing about that list leaves the phone (§10).
    var picked by remember(candidates) { mutableStateOf(candidates.take(5).map { it.first }.toSet()) }

    Screen {
        Bg()
        Bg2()
        Aura(0.70f, 0.14f, top = 0.10f, left = 0.15f, alpha = 0.36f)
        Grain()
        TopFade()
        Body {
            AskHeader("Ask 3 of 4", "Confirm")
            Gap(22.4)
            // The headline counts what is actually there. It read "THESE FIVE" over a single row on a phone
            // with one heavy app, which is the same lie the intent and confirm counters used to tell.
            Text(
                when {
                    usage == null -> "READING WHAT\nYOUR PHONE KEPT."
                    usage?.available != true -> "THE SYSTEM\nCANNOT SEE YET."
                    candidates.isEmpty() -> "NOTHING TOOK\nYOUR HOURS YET."
                    candidates.size == 1 -> "THIS ONE\nTOOK YOUR HOURS."
                    else -> "THESE ${spelled(minOf(candidates.size, 5))}\nTOOK YOUR HOURS."
                },
                style = t.display(27.2),
            )
            Gap(11.2)
            Tag(
                when {
                    usage == null -> "Thirty days is what Android kept"
                    usage?.available != true -> "Grant usage access and the System will read it"
                    candidates.isEmpty() -> "The System found nothing worth naming on this phone"
                    else -> "Already picked from your own 30 days · tap to change"
                },
            )

            Gap(14.4)
            Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                Text(
                    "New social, browser or VPN apps are caught automatically later, so you do not have to " +
                        "maintain this list.",
                    style = t.body.copy(fontSize = m.s(12.2)),
                )
            }

            Gap(14.4)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(6.4)),
            ) {
                if (candidates.isEmpty()) {
                    // Three states, not two. A read still running, a refused grant, and a phone where nothing
                    // crossed the bar are different facts, and the headline above already picked one of them.
                    Tag(
                        when {
                            usage == null -> "Reading your own thirty days"
                            usage?.available != true -> "No usage access granted yet"
                            else -> "Nothing here averaged even a minute a day"
                        },
                        t.key,
                    )
                } else {
                    candidates.forEach { (pkg, ms) ->
                        AppRow(appLabel(context, pkg), perDayLabel(ms), pkg in picked) {
                            picked = if (pkg in picked) picked - pkg else picked + pkg
                        }
                    }
                }
            }
            // The label counts what is actually selected. It read "Confirm 5" over four ticked rows before.
            Cta("Confirm ${picked.size} · continue", onClick = {
                scope.launch { Repo.update { it.copy(profile = it.profile.copy(watchedPackages = picked.toList())) } }
                nav("contract")
            })
            Gap(19.2)
        }
    }
}

/** `data-s="contract"` — ask 4 of 4, the single accept gate. It issues the first quest. */
@Composable
fun ContractScreen() {
    val m = LocalMetrics.current
    val t = LocalType.current
    val nav = LocalNav.current
    val scope = rememberCoroutineScope()
    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_eye, top = -0.04f, left = 0.20f, width = 0.60f, alpha = 0.62f, feather = true)
        Shade(0f to 0.42f, 0.08f to 0f, 0.24f to 0.59f, 0.36f to 1f)
        Grain()
        TopFade()
        Body {
            AskHeader("Ask 4 of 4", "Terms")
            Gap(17.6)
            Text("LAST SCREEN.", style = t.display(28.8))
            Gap(8.8)
            Tag("Everything else the System will ask for later, in context")

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(7)),
            ) {
                TermRow("◈", "The System will not stop you",
                    "It records, then it charges you. Blocking only arrives if you repeatedly fail your own " +
                        "limits, and you will be asked first.")
                TermRow("⚠", "Missing a day costs you",
                    "Streak breaks, then aura, then rank. Sequenced over a week, never all at once. Every " +
                        "penalty screen shows the way back.")
                TermRow("○", "Absence counts as failure",
                    "No data for a day is a missed threshold. Going quiet is worse than logging a bad day.")
                // The design page says the private track is "never penalised". DECISIONS §7 reverses that and is
                // the authority: the penalty is real, and what makes it safe is that nobody else can see it.
                TermRow("◆", "Your private track is yours",
                    "Stored on this device only. Never a leaderboard, never a notification, never a flag that " +
                        "you went dark. It has real stakes, and no one but you will ever know them.")
                TermRow("♡", "Rest is not weakness",
                    "Fatigue from sleep and HRV reduces your load and protects your rank. One full waiver per Pact.")
            }
            // The single accept gate. It is the only place `onboarded` is set, so there is one door into the app.
            Cta("Accept · issue first quest", onClick = {
                scope.launch { Repo.update { it.copy(onboarded = true) } }
                nav("diag")
            })
            Gap(11.2)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Tag("Every term is reversible in Settings")
            }
            Gap(17.6)
        }
    }
}

// ── shared pieces ─────────────────────────────────────────────────────────────

@Composable
private fun ColumnScopeMarker() = Unit

@Composable
private fun AskHeader(step: String, label: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Eye(step); Filler(); Tag(label)
    }
}

/** The 34px rounded glyph tile every onboarding list row leads with. */
@Composable
private fun IconTile(glyph: String, size: Number = 34, radius: Number = 11) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Box(
        Modifier
            .size(m.d(size))
            .clip(RoundedCornerShape(m.d(radius)))
            .background(p.hot.mix(Color(0xFF12162B), 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, style = t.md.copy(fontSize = m.s(14.4), color = p.ink))
    }
}

@Composable
private fun PermRow(
    glyph: String,
    title: String,
    detail: String,
    requirement: String,
    granted: Boolean = false,
    unavailable: Boolean = false,
    onRequest: (() -> Unit)? = null,
) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    // A granted row is not tappable. Sending someone back into system Settings to re-grant what they already
    // granted is the kind of loop that reads as the app not knowing its own state.
    val action = if (granted || unavailable || onRequest == null) Modifier else Modifier.clickable(onClick = onRequest)
    val trailing = when {
        unavailable -> "unavailable"
        granted -> "granted"
        else -> requirement
    }
    Card(Modifier.fillMaxWidth().then(action), lit = granted, padding = 13.6) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
            IconTile(glyph)
            Column(Modifier.weight(1f)) {
                Text(title, style = t.questTitle.copy(fontSize = m.s(13.6)))
                Gap(3.2)
                Tag(detail, t.key)
            }
            Tag(
                trailing,
                t.key.copy(
                    color = when {
                        granted -> app.gakseong.ui.theme.Ok
                        trailing == "required" -> p.hot
                        else -> p.faint
                    },
                ),
            )
        }
    }
}

@Composable
private fun WhereRow(glyph: String, name: String, hours: String) {
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(9.6))) {
        Text(glyph, style = t.tag.copy(fontSize = m.s(10)))
        Text(name, style = t.questTitle.copy(fontSize = m.s(12.8)))
        Filler()
        Text(hours, style = t.monoSmall.copy(color = Bad, fontSize = m.s(13.8)))
    }
}

@Composable
private fun AppRow(name: String, perDay: String, selected: Boolean, onToggle: () -> Unit = {}) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth().clickable(onClick = onToggle), lit = selected, padding = 12.8) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
            Box(
                Modifier.size(m.d(30)).clip(RoundedCornerShape(m.d(9))).background(Color(0xFF12162B)),
                contentAlignment = Alignment.Center,
            ) {
                Text(name.take(1), style = t.questSub.copy(color = p.dim, fontSize = m.s(9.9)))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = t.questTitle.copy(
                        fontSize = m.s(13.4),
                        fontWeight = FontWeight(if (selected) 680 else 560),
                    ),
                )
                Gap(2.9)
                Tag("$perDay / day", t.key)
            }
            Box(
                Modifier
                    .size(m.d(22))
                    .clip(RoundedCornerShape(m.d(6)))
                    .background(if (selected) p.hot else Color.Transparent)
                    .border(1.dp, if (selected) p.hot else p.line2, RoundedCornerShape(m.d(6))),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Text("✓", style = t.pill.copy(fontSize = m.s(9), color = Color(0xFF07080C)))
            }
        }
    }
}

@Composable
private fun TermRow(glyph: String, title: String, detail: String) {
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), padding = 13.6) {
        Row(horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
            IconTile(glyph, size = 30, radius = 10)
            Column {
                Text(title, style = t.questTitle.copy(fontSize = m.s(13.3)))
                Gap(3.8)
                Text(detail, style = t.body.copy(fontSize = m.s(11.8)))
            }
        }
    }
}

/** `80h`, or `52m` when an app did not reach an hour. Never sent anywhere: §10 is about the wire. */
private fun hoursLabel(ms: Long): String {
    val minutes = ms / 60_000
    return if (minutes >= 60) "${minutes / 60}h" else "${minutes}m"
}

/**
 * `8.6 days · 28% of your waking life`, against a sixteen-hour waking day.
 *
 * Says which kind of nothing it is when there is no number: a read still in flight and a refused grant look
 * identical as a blank, and only one of them is the user's choice.
 */
private fun wakingLifeLine(hours: Double?, usage: app.gakseong.sense.UsageReading?): String = when {
    hours == null && usage == null -> "Reading what your phone already kept"
    hours == null -> "Usage access not granted · the System cannot see yet"
    else -> "%.1f days · %.0f%% of your waking life".format(hours / 24, hours / (RETAINED_DAYS * 16.0) * 100)
}

/** Every grant this screen reports, read together so the rows cannot disagree with each other. */
private data class Grants(
    val usage: Boolean,
    val healthAvailability: HealthAvailability,
    val notifications: Boolean,
    /** Read separately: Health Connect's grant set is an IPC, so it cannot join the synchronous snapshot. */
    val health: Boolean = false,
)

private fun readGrants(context: android.content.Context) = Grants(
    usage = hasUsageAccess(context),
    healthAvailability = healthAvailability(context),
    notifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.POST_NOTIFICATIONS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED,
)

/**
 * Packages that are never a scroll app, so the confirm list is the user's own choices and not their phone's.
 *
 * The launcher and the system UI are always near the top of a usage list, and offering to police them would be
 * the screen failing to understand its own question.
 */
private val SYSTEM_PACKAGES = setOf(
    "com.android.systemui",
    "com.google.android.apps.nexuslauncher",
    "com.android.launcher3",
    "com.google.android.googlequicksearchbox",
    "com.android.settings",
    "com.google.android.permissioncontroller",
    "com.google.android.gms",
)

/** `2h 41m / day`, averaged over the window Android actually kept. */
private fun perDayLabel(totalMs: Long): String {
    val minutes = totalMs / 60_000 / RETAINED_DAYS
    return if (minutes >= 60) "${minutes / 60}h ${minutes % 60}m" else "${minutes}m"
}

/** Small numbers as words, because "THESE 3 TOOK YOUR HOURS" reads like a receipt. */
private fun spelled(n: Int) = when (n) {
    2 -> "TWO"
    3 -> "THREE"
    4 -> "FOUR"
    else -> "FIVE"
}
