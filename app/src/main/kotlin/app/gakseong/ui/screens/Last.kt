package app.gakseong.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import app.gakseong.widget.AuraReceiver
import app.gakseong.widget.DailyQuestReceiver
import app.gakseong.widget.NightGateReceiver
import app.gakseong.widget.StreakReceiver
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok
import app.gakseong.ui.theme.pct

/**
 * `data-s="aikey"` — the AI gate. §25: lock the feature, never the app. A blurred sample of the real output and
 * one action, shown in place, and `Not now` is a real answer that does not re-ask on a schedule.
 */
@Composable
fun AiGateScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(0.80f, 0.20f, top = 0.08f, left = 0.10f, alpha = 0.34f)
        Art(R.drawable.sc_eye, top = -0.08f, left = -0.04f, width = 1.06f, alpha = 0.34f)
        Shade(0f to 0.48f, 0.22f to 0.72f, 0.40f to 1f)
        Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Dormant", color = p.soft); Filler(); Tag("optional, always")
            }

            Gap(14.4)
            Card(Modifier.fillMaxWidth(), dashed = true, padding = 13.6) {
                Tag("◈ This is what a locked feature looks like", t.tag.copy(color = p.hot))
                Gap(6.4)
                // The sample is blurred rather than hidden: previewing the value is the whole point of §25.
                Text(
                    "“Every failure this week came after 9pm.”",
                    style = t.questTitle.copy(fontSize = m.s(13.8), color = p.dim),
                    modifier = Modifier.blur(3.dp),
                )
                Gap(4.8)
                Tag("blurred sample · the System has the data, not the voice", t.key.copy(color = p.faint))
                Gap(9.6)
                Row(horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                    Pill("Awaken it", on = true); Pill("Not now")
                }
            }

            Gap(17.6)
            Text("THE SYSTEM READS.\nIT DOES NOT YET SPEAK.", style = t.display(24))
            Gap(9.6)
            Tag("Paste a free Gemini key and five things wake up")

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(6.4)),
            ) {
                Locked("Generated quests", "The static bank still runs without a key")
                Locked("The weekly post-mortem", "Numbers work; the verdict is the locked part")
                Locked("Chat", "Speak to the System, ten messages a day")
                Locked("Reader passage selection", "Chosen against your own record")
                Locked("Title generation", "Names for what you have held")
                Gap(4)
                SystemWindow {
                    Tag("Three steps, no card", t.tag.copy(color = p.hot))
                    Gap(4.8)
                    Text(
                        "aistudio.google.com, create a key, paste it. It is free and needs no billing. Google may " +
                            "train on free-tier traffic and a paid key is excluded, which you should know before " +
                            "you paste rather than after.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }
                Card(Modifier.fillMaxWidth(), padding = 13.6) {
                    Text(
                        "The daily quest never locks. Quests, thresholds, streaks, gates, raids, leagues, shadows " +
                            "and the private track all work with no key at all.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }
                Gap(4)
            }
            Cta("Awaken the System")
            Gap(7.2)
            Cta("Not now", ghost = true)
            Gap(17.6)
        }
    }
}

/** `data-s="bonus"` — one a day, spawned at a time the user does not pick, and it expires within the hour. */
@Composable
fun BonusScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(0.70f, 0.15f, top = 0.09f, left = 0.15f, alpha = 0.38f)
        Art(R.drawable.sc_flame, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.52f)
        Shade(0f to 0.42f, 0.06f to 0f, 0.22f to 0.67f, 0.34f to 1f)
        Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Bonus Spawned"); Filler(); Tag("once a day")
            }

            Gap(12.8)
            Card(Modifier.fillMaxWidth(), padding = 11.2) {
                Row(horizontalArrangement = Arrangement.spacedBy(m.d(8.8))) {
                    Box(Modifier.size(m.d(24)).clip(RoundedCornerShape(m.d(7)))) {
                        Image(painterResource(R.drawable.ic_app), null, Modifier.matchParentSize())
                    }
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Tag("The System", t.tag.copy(color = p.dim, letterSpacing = 0.20.em))
                            Filler()
                            Tag("now", t.key)
                        }
                        Gap(3.8)
                        Text("⚡ A bonus has spawned.", style = t.questTitle.copy(fontSize = m.s(12.8)))
                        Gap(1.6)
                        Text("Tap within the hour or it is gone.", style = t.body.copy(fontSize = m.s(11.5)))
                    }
                }
            }

            Gap(16)
            Text("TAKE IT NOW,\nOR LOSE IT.", style = t.display(24))

            Gap(14.4)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
                XlNumber("41:08", designPx = 40)
                Column {
                    Tag("until it expires", t.key)
                    Gap(6.4)
                }
            }

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                Card(Modifier.fillMaxWidth(), big = true, lit = true, padding = 15.2) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(9.6))) {
                        Sig()
                        Column(Modifier.weight(1f)) {
                            Text("Phone down · 2 hours", style = t.listItem)
                            Gap(2.2)
                            Tag("raises today's ceiling", t.key)
                        }
                        Text("+400", style = t.monoSmall)
                    }
                }
                SystemWindow {
                    Text(
                        "One offer, drawn at random, on a short fuse. A menu of bonuses would be a shop, and a " +
                            "shop gets optimised. Letting it expire costs nothing.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }
                Gap(4)
            }
            Cta("Start it")
            Gap(7.2)
            Cta("Let it go", ghost = true)
            Gap(17.6)
        }
    }
}

/** `data-s="widget"` — Glance widgets. The System without opening the app, which is where retention actually is. */
@Composable
fun WidgetScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val context = LocalContext.current
    val haptics = rememberHaptics()
    val sys = LocalSystem.current

    // requestPinAppWidget is the only way an app can offer this: the launcher shows its own confirmation and the
    // user still decides. Nothing is added behind their back.
    val pin: (Class<*>) -> Unit = { cls ->
        haptics.tick()
        val manager = AppWidgetManager.getInstance(context)
        if (manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(ComponentName(context, cls), null, null)
        }
    }
    Screen {
        Bg(); Bg2()
        Art(R.drawable.sc_sigilup, top = -0.03f, left = -0.03f, width = 1.06f, alpha = 0.46f, feather = true)
        Shade(0f to 0.42f, 0.10f to 0f, 0.28f to 0.59f, 0.42f to 1f)
        Grain(); TopFade()
        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Home Screen"); Filler(); Tag("6 widgets · Glance")
            }
            Gap(16)
            Text("THE SYSTEM, WITHOUT\nOPENING THE APP", style = t.display(24))

            Gap(14.4)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(13.6)),
            ) {
                WidgetPreview("Daily quest", "4×2", { pin(DailyQuestReceiver::class.java) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Tag("Daily Quest", t.tag.copy(color = p.hot)); Filler(); Tag("D · III")
                    }
                    Gap(8)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                        repeat(5) { i ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .size(m.d(22))
                                    .clip(RoundedCornerShape(m.d(5)))
                                    .background(if (i < 3) p.hot.pct(0.5f) else p.card)
                            )
                        }
                    }
                    Gap(8)
                    Text("640 · threshold cleared", style = t.questSub.copy(color = p.soft))
                }
                WidgetPreview("Aura today", "2×2", { pin(AuraReceiver::class.java) }) {
                    Text("640", style = t.lg(28))
                    Gap(3.2)
                    Tag("560 to D · II", t.key)
                }
                WidgetPreview("Night gate", "4×1", { pin(NightGateReceiver::class.java) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("☾ ${sys.settings.nightGateStart} — ${sys.settings.nightGateEnd}", style = t.questTitle.copy(fontSize = m.s(12.8)))
                        Filler()
                        Tag("pending", t.key.copy(color = p.soft))
                    }
                }
                WidgetPreview("Streak", "2×1", { pin(StreakReceiver::class.java) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("14d", style = t.lg(20))
                        Filler()
                        Tag("2 shields", t.key)
                    }
                }
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        "Widgets refresh on the same retroactive settle as the app: app open, widget refresh, and " +
                            "a 15-minute job. Never a foreground service, because OEM battery managers kill those.",
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                }
                Gap(11.2)
            }
        }
        BottomNav(active = 0)
    }
}

/** `data-s="pact"` — a signed commitment that grants the System more power for a fixed window. */
@Composable
fun PactScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = app.gakseong.ui.theme.LocalHunterClass.current
    Screen {
        Bg(); Bg2()
        Aura(0.92f, 0.30f, top = 0.16f, left = 0.04f, alpha = 0.5f)
        Art(hunter.portrait, top = -0.02f, left = 0.02f, width = 0.96f)
        Shade(0f to 0.57f, 0.12f to 0f, 0.40f to 0f, 0.54f to 0.88f, 0.64f to 1f)
        Grain(); TopFade()
        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("The Pact")
                Filler()
                XlNumber("47", designPx = 62.4)
                Gap(7.2)
                Tag("of 75 days", t.tag.copy(letterSpacing = 0.30.em))
                Gap(17.6)
                Meter(fill = 0.63f)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Tag("Signed 47 days ago", t.key); Tag("28 remain", t.key)
                }

                Gap(19.2)
                Card(Modifier.fillMaxWidth(), big = true, padding = 15.2) {
                    Tag("Powers granted", t.tag.copy(color = p.hot))
                    Gap(9.6)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(8))) {
                        PactRow("Containment enabled", "active", p.soft)
                        PactRow("Thresholds raised one band", "C-level", p.soft)
                        PactRow("Shields suspended", "none", Bad)
                        PactRow("One full waiver", "unused", Ok)
                    }
                }

                Gap(8)
                Card(Modifier.fillMaxWidth(), accent = Bad, padding = 14.4) {
                    Tag("Breaking it", t.tag.copy(color = Bad))
                    Gap(4)
                    Text(
                        "A broken Pact costs the title and the shadow it would have paid. Nothing else: the " +
                            "penalty sequence in §6 still applies as normal and does not stack on top.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }

                Filler()
                Cta("Hold the line")
                Gap(17.6)
            }
        }
    }
}

/**
 * `data-s="contain"` — real blocking, earned by repeated failure and consented to at awakening. Never switched on
 * by default, because the accessibility permission it needs would kill installs on day one.
 */
@Composable
fun ContainScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(1.0f, 0.26f, top = 0.22f, left = 0f, alpha = 0.5f)
        Art(R.drawable.sc_dragon, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.66f)
        Shade(0f to 0.52f, 0.08f to 0f, 0.26f to 0.75f, 0.38f to 1f)
        Grain(); TopFade()
        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Containment Authorised")
                Filler()
                Text(
                    "YOU HAVE FAILED TO CONTAIN THIS FOUR DAYS RUNNING.",
                    style = t.display(30.4).copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth(0.92f),
                )
                Gap(12.8)
                Text(
                    "The System will contain it for you.",
                    style = t.body.copy(fontSize = m.s(13.1), textAlign = TextAlign.Center, color = p.ink),
                )

                Gap(20.8)
                Card(Modifier.fillMaxWidth(), big = true, padding = 15.2) {
                    Tag("Terms · consented at awakening", t.tag.copy(color = p.hot))
                    Gap(9.6)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(8))) {
                        PactRow("Duration", "until 2 clear days", p.soft)
                        PactRow("Scope", "your declared apps", p.soft)
                        PactRow("Emergency unlock", "−500 aura", Bad)
                        PactRow("Maximum", "7 days", p.soft)
                    }
                }

                Gap(8)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 13.6) {
                    Text(
                        "Every unlock is logged. Containment is a power you granted, and it ends on its own; it is " +
                            "never a state the app can keep you in.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }

                Filler()
                Cta("Accept containment")
                Gap(7.2)
                Cta("Revoke the power", ghost = true)
                Gap(17.6)
            }
        }
    }
}

/** `data-s="chat"` — speak to the System. Ten messages a day, and the cap is the feature. */
@Composable
fun ChatScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Art(R.drawable.sc_eye, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.5f)
        Shade(0f to 0.42f, 0.06f to 0f, 0.20f to 0.67f, 0.32f to 1f)
        Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Speak to the System"); Filler(); Pill("6 of 10 left today", on = true)
            }
            Gap(8.8)
            Row(horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                Pill("⌨ Text", on = true); Pill("◉ Voice")
            }
            Gap(9.6)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(4.8))) {
                Pill("I'm exhausted"); Pill("Make today harder")
            }

            Gap(12.8)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                Message(
                    "i keep opening instagram without deciding to. like my thumb does it before i do",
                    mine = true,
                )
                Message(
                    "That is the mechanism, not a failure of character. Your own record says the reach happens " +
                        "within ninety seconds of the screen coming on. So the fight is not inside the app; it is " +
                        "at the unlock.",
                    mine = false,
                )
                Message("so what do i do", mine = true)
                Message(
                    "Tonight's quest is already set: the phone stays down for the first ten minutes after you " +
                        "wake. Win the unlock and the app never gets its turn.",
                    mine = false,
                )
                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        "Ten a day, and the limit is deliberate. An unlimited companion becomes the thing you open " +
                            "instead of doing the quest.",
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                }
                Gap(4)
            }
            Card(Modifier.fillMaxWidth(), padding = 12) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tag("Type anything…", t.tag.copy(color = p.ghost, letterSpacing = 0.10.em))
                    Filler()
                    Box(
                        Modifier.size(m.d(30)).clip(CircleShape).background(p.hot),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("↑", style = t.md.copy(fontSize = m.s(13), color = Color(0xFF07080C)))
                    }
                }
            }
            Gap(17.6)
        }
    }
}

/** `data-s="store"` — the Play listing. Genre vocabulary lives here, which is what Play actually indexes. */
@Composable
fun StoreScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Art(R.drawable.sc_monarch, top = -0.02f, left = -0.02f, width = 1.04f, alpha = 0.38f)
        Shade(0f to 0.57f, 0.08f to 0f, 0.24f to 0.77f, 0.36f to 1f)
        Grain(); TopFade()
        Body {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Tag("Google Play")
                Gap(9.6)
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(m.d(16)))) {
                    Image(painterResource(R.drawable.sc_banner), null, Modifier.fillMaxWidth())
                    Column(
                        Modifier.matchParentSize().padding(horizontal = m.d(15.2)),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("YOUR PHONE IS\nTHE DUNGEON.", style = t.display(24))
                        Gap(6.4)
                        Tag("Rank E to S", t.tag.copy(color = p.soft))
                    }
                }

                Gap(16)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                    Box(
                        Modifier
                            .size(m.d(54))
                            .clip(RoundedCornerShape(m.d(14)))
                            .border(1.dp, p.line, RoundedCornerShape(m.d(14)))
                    ) {
                        Image(painterResource(R.drawable.ic_app), null, Modifier.matchParentSize())
                    }
                    Column {
                        Text("The System", style = t.md.copy(fontSize = m.s(16.8)))
                        Gap(3.2)
                        Tag("Health & Fitness · Teen")
                        Gap(2.6)
                        Tag("4.8 ★ · 12K", t.tag.copy(color = p.soft))
                    }
                }

                Gap(16)
                Card(Modifier.fillMaxWidth(), big = true, padding = 15.2) {
                    Text(
                        "A discipline game where every quest is proven by your own phone. Awaken, take a rank from " +
                            "E to S, clear gates, raid with one other hunter, and extract a shadow from every habit " +
                            "you beat.",
                        style = t.body.copy(fontSize = m.s(12.8)),
                    )
                    Gap(9.6)
                    Row(horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                        Pill("dungeon"); Pill("hunter"); Pill("awakening")
                    }
                    Gap(5.4)
                    Row(horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                        Pill("guild"); Pill("shadow"); Pill("rank E–S")
                    }
                }

                Gap(8)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        "The store name is Gakseong; every screen inside says The System. Genre words go in the " +
                            "long description, which is what Play indexes, and the licensed mark goes nowhere.",
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                }
                Gap(11.2)
            }
            Cta("Install")
            Gap(17.6)
        }
    }
}

// ── shared ────────────────────────────────────────────────────────────────────

@Composable
private fun Locked(title: String, detail: String) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), padding = 11.5) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = t.questTitle.copy(fontSize = m.s(12.8), color = p.dim))
                Gap(1.9)
                Tag(detail, t.key)
            }
            Tag("locked", t.key.copy(color = p.faint))
        }
    }
}

@Composable
private fun PactRow(label: String, value: String, color: Color) {
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = t.body.copy(fontSize = m.s(12.6)))
        Filler()
        Tag(value, t.tag.copy(color = color))
    }
}

@Composable
private fun WidgetPreview(label: String, size: String, onPin: () -> Unit, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    val t = LocalType.current
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Tag(label); Filler(); Tag(size, t.tag.copy(color = p.soft))
        }
        Gap(5.6)
        Card(Modifier.fillMaxWidth(), padding = 12.8) { content() }
        Gap(5.6)
        Row(Modifier.fillMaxWidth().clickable(onClick = onPin)) {
            Pill("Add to home screen")
        }
    }
}

@Composable
private fun Message(text: String, mine: Boolean) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Box(
            Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(m.d(16)))
                .background(if (mine) p.hot.pct(0.22f) else p.card)
                .border(1.dp, if (mine) p.hot.pct(0.40f) else p.line, RoundedCornerShape(m.d(16)))
                .padding(m.d(12.8)),
        ) {
            Text(
                text,
                style = t.body.copy(fontSize = m.s(12.8), color = if (mine) p.ink else p.dim),
            )
        }
    }
}
