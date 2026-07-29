package app.gakseong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.mix

/** `data-s="intent"` — ask 2 of 4. Free text plus quoted statements, pick at least five. */
@Composable
fun IntentScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg()
        Bg2()
        Aura(0.70f, 0.16f, top = 0.12f, left = 0.15f, alpha = 0.4f)
        Grain()
        TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Ask 2 of 4"); Filler(); Tag("Intent")
            }
            Gap(22.4)
            Text("WHAT ARE YOU\nHERE TO FIX?", style = t.display(27.2))
            Gap(11.2)
            Tag("In your own words. The System will translate.")

            Gap(17.6)
            Card(
                Modifier.fillMaxWidth().heightIn(min = m.d(104)),
                big = true,
                accent = p.hot,
                padding = 16,
            ) {
                Text(
                    buildAnnotatedString {
                        append("stop wasting my life on reels, sleep before 1am, actually talk to people")
                        withStyle(SpanStyle(color = p.hot)) { append("|") }
                    },
                    style = t.body.copy(fontSize = m.s(13.8), color = p.ink),
                )
            }

            Gap(14.4)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Tag("Pick at least five"); Filler(); Pill("3 of 5", on = true)
            }

            Gap(9.6)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(6)),
            ) {
                Starter("◉", "I lose whole evenings to reels", "Attention", true)
                Starter("☾", "I go to bed at 3am and hate it", "Rest", true)
                Starter("⌖", "I have not left the house in days", "Isolation", true)
                Starter("◈", "I start things and never finish", "Attention", false)
                Starter("○", "I order food instead of cooking", "Intake", false)
                Starter("⚡", "I stopped training and feel it", "Body", false)
                Starter("✦", "I scroll instead of replying to people", "Isolation", false)
                Starter("♡", "I am tired all the time", "Rest", false)
                Starter("◉", "I check my phone before I am fully awake", "Attention", false)
                Starter("◈", "I cannot sit through anything without picking it up", "Attention", false)
                Starter("⌖", "I say I will go out and then do not", "Isolation", false)
                Starter("○", "I eat at midnight most nights", "Intake", false)
                Starter("⚡", "I have not walked anywhere in weeks", "Body", false)
                Starter("✦", "I compare myself to strangers online", "Mind", false)
                Starter("♡", "I feel behind on everything", "Mind", false)
            }
            Cta("Continue")
            Gap(19.2)
        }
    }
}

/** `data-s="class"` — Awakening. The class is derived from intent, never chosen, so nothing here is a picker. */
@Composable
fun AwakeningScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current
    Screen {
        Bg()
        Bg2()
        Aura(0.76f, 0.26f, top = 0.16f, left = 0.12f, alpha = 0.5f)
        Art(R.drawable.sc_portal, top = -0.06f, left = 0.24f, width = 0.52f, alpha = 0.95f, feather = true)
        Art(hunter.portrait, top = -0.02f, left = -0.02f, width = 1.04f)
        Shade(0f to 0.57f, 0.12f to 0f, 0.44f to 0f, 0.58f to 0.88f, 0.68f to 1f)
        Grain()
        TopFade()
        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Awakening")
                Filler()
                Sig(size = 40)
                Gap(9.6)
                Text(hunter.label.uppercase(), style = t.display(33.6).copy(letterSpacing = 0.02.em))
                Gap(5.6)
                Tag("Trains · Screen discipline")
                Gap(9.6)
                Text(
                    "Your quests weight toward attention. The System comes for your feeds first.",
                    style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center),
                    modifier = Modifier.width(m.d(240)),
                )

                Gap(14.4)
                SystemWindow {
                    Tag("Why this class", t.tag.copy(color = p.hot))
                    Gap(4.2)
                    Text(
                        "Three of your five statements were about attention. The class is read from what you " +
                            "said, not picked from a menu, so it cannot be gamed into the easy one.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }

                Gap(8)
                SystemWindow {
                    Tag("Assessment complete", t.tag.copy(color = p.hot))
                    Gap(4.8)
                    Text(
                        buildAnnotatedString {
                            append("7 days of history read. Starting rank ")
                            withStyle(SpanStyle(color = p.hot, fontWeight = FontWeight(650))) { append("E · I") }
                            append(". The System reads what your phone already knows.")
                        },
                        style = t.body.copy(fontSize = m.s(12.5)),
                    )
                }

                Gap(14.4)
                Row(horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                    repeat(7) { i ->
                        Box(
                            Modifier
                                .size(m.d(6))
                                .clip(CircleShape)
                                .background(if (i == 4) p.hot else p.line2)
                        )
                    }
                }
                Filler()
                Cta("Accept the class")
                Gap(19.2)
            }
        }
    }
}

/** `data-s="stage"` — what the System asks for, and when. Four asks in minute one; everything else in context. */
@Composable
fun StagedScreen() {
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_eye, top = -0.03f, left = 0.16f, width = 0.68f, alpha = 0.44f, feather = true)
        Shade(0f to 0.42f, 0.10f to 0f, 0.26f to 0.59f, 0.38f to 1f)
        Grain()
        TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Setup"); Filler(); Tag("4 asks · ~90 seconds")
            }
            Gap(16)
            Text("FOUR QUESTIONS.\nTHEN IT STARTS.", style = t.display(25.6))
            Gap(9.6)
            Tag("Everything else arrives when it is relevant")

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(9.6)),
            ) {
                StageGroup("Minute one", "4 asks", true, listOf(
                    Triple("Access", "Usage, Health Connect, notifications", "one tap"),
                    Triple("Intent", "What you are here to fix, in your words", "type or tap"),
                    Triple("Apps", "Five it already picked from your data", "confirm"),
                    Triple("Terms", "What the System may and may not do", "accept"),
                ))
                StageGroup("Immediately after", "no input", false, listOf(
                    Triple("Your 206 hours", "Read from what Android already kept", "reveal"),
                    Triple("Awakening", "Your class, from your intent", "reveal"),
                    Triple("Baseline + first three quests", "Band set for you, low", "reveal"),
                ))
                StageGroup("Day 2", "optional", false, listOf(
                    Triple("Rate yourself", "The half the sensors cannot see", "skippable"),
                ))
                StageGroup("Day 3", "optional", false, listOf(
                    Triple("Private track", "Only if you have something to fight alone", "skippable"),
                ))
                StageGroup("Day 7", "offered", false, listOf(
                    Triple("Raise your band", "Only after seven clean days", "your call"),
                ))
                StageGroup("Any time", "Settings", false, listOf(
                    Triple("Weighting", "Derived from intent · change whenever", "settings"),
                    Triple("Thresholds, sound, AI, data", "Everything is reversible", "settings"),
                ))
                Gap(11.2)
            }
        }
    }
}

// ── shared ────────────────────────────────────────────────────────────────────

@Composable
private fun Starter(glyph: String, statement: String, domain: String, picked: Boolean) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), lit = picked, padding = 11.5) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(9.6))) {
            Box(
                Modifier
                    .size(m.d(28))
                    .clip(RoundedCornerShape(m.d(9)))
                    .background(p.hot.mix(Color(0xFF12162B), if (picked) 0.30f else 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(glyph, style = t.tag.copy(fontSize = m.s(11.2), color = p.ink))
            }
            Text(statement, style = t.questTitle.copy(fontSize = m.s(12.5)), modifier = Modifier.weight(1f))
            Tag(domain, t.key.copy(color = if (picked) p.hot else p.faint))
        }
    }
}

@Composable
private fun StageGroup(head: String, note: String, live: Boolean, rows: List<Triple<String, String, String>>) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val content: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Tag(head, t.tag.copy(color = if (live) p.hot else p.faint)); Filler(); Tag(note, t.key)
        }
        Gap(9.6)
        Column(verticalArrangement = Arrangement.spacedBy(m.d(8))) {
            rows.forEach { (title, detail, kind) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(title, style = t.questTitle.copy(fontSize = m.s(12.8)))
                        Gap(1.9)
                        Tag(detail, t.key)
                    }
                    Tag(kind, t.key.copy(color = p.soft))
                }
            }
        }
    }
    if (live) SystemWindow { content() }
    else Card(Modifier.fillMaxWidth(), padding = 13.6) { content() }
}
