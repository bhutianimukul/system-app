package app.gakseong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.pct

// The staged asks. None of these are in setup, which is the point: §13 keeps setup to four questions and lets
// everything else arrive when it is relevant.

/** `data-s="rate"` — day 2. Self-rating covers the half the sensors cannot see, and the data still wins. */
@Composable
fun RateScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(0.70f, 0.15f, top = 0.09f, left = 0.15f, alpha = 0.4f)
        Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Day 2", color = p.soft); Filler(); Tag("Optional · not in setup")
            }
            Gap(19.2)
            Text("NOW RATE\nYOURSELF", style = t.display(28))
            Gap(11.2)
            Tag("One day in. The System has your numbers — this is the half it cannot see.")

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(7)),
            ) {
                SelfRate("Focus", "How much of today did you actually choose?", 2)
                SelfRate("Rest", "Woke up recovered, or woke up tired?", 3)
                SelfRate("Body", "Did you move like someone who lives in it?", 1)
                SelfRate("Company", "Did you speak to anybody who matters?", 4)
                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        buildAnnotatedString {
                            append("Where your rating and your data disagree, ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                append("the System trusts the data")
                            }
                            append(", and tells you so.")
                        },
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                }
            }
            Cta("Continue")
            Gap(17.6)
        }
    }
}

/** `data-s="privset"` — day 3. Skipping costs nothing, which is what makes the opt-in honest. */
@Composable
fun PrivateSetupScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2(); Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(7.2))) {
                    Box(Modifier.size(m.d(4)).clip(CircleShape).background(p.dim))
                    Tag("Day 3", t.tag.copy(color = p.dim))
                }
                Filler()
                Tag("Optional · not in setup")
            }
            Gap(17.6)
            Text("ANYTHING YOU\nFIGHT ALONE?", style = t.display(27.2))
            Gap(11.2)
            Tag("Skip this and nothing is lost. It can be added any time.")

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(6.4)),
            ) {
                // The shipped seed list is not in this repo. §7 keeps it out because the repo is public.
                PrivPick("Substance", "Something you take that you would rather not")
                PrivPick("Compulsion", "Something you do that you did not decide to")
                PrivPick("App-mediated", "Gambling, delivery — the phone can see these ones")
                PrivPick("Something else", "You name it. The System never asks what it means.")
                Gap(4)
                SystemWindow {
                    Tag("How this one works", t.tag.copy(color = p.hot))
                    Gap(4.2)
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                append("The System cannot detect any of these.")
                            }
                            append(
                                " You confirm once a day or the streak stops. Locked behind your fingerprint, " +
                                    "stored only on this phone, and never shown to your guild or any leaderboard."
                            )
                        },
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                    Gap(9.6)
                    Row(horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                        Pill("☉ Require fingerprint", on = true)
                        Pill("Remind at 17:00")
                    }
                }
            }
            Cta("Set up private track")
            Gap(7.2)
            Cta("Skip · add later", ghost = true)
            Gap(17.6)
        }
    }
}

/** `data-s="thresh"` — offered after seven clean days. Raising the band is the user's call, never the app's. */
@Composable
fun ThresholdScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(0.74f, 0.16f, top = 0.09f, left = 0.13f, alpha = 0.42f)
        Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("After 7 clean days", color = p.soft); Filler(); Tag("System's call")
            }
            Gap(17.6)
            Text("SET LOW.\nRAISE IT YOURSELF.", style = t.display(27.2))

            Gap(16)
            SystemWindow {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tag("Daily band", t.tag.copy(color = p.hot)); Filler()
                    Tag("Rank E · I", t.tag.copy(color = p.soft))
                }
                Gap(11.2)
                // The band as one bar: penalty, then the zone that counts, then overflow.
                Row(Modifier.fillMaxWidth().height(m.d(14)).clip(CircleShape), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(Modifier.weight(26f).fillMaxHeight().background(Brush.horizontalGradient(listOf(Bad.pct(0.4f), Bad.pct(0.2f)))))
                    Box(Modifier.weight(56f).fillMaxHeight().background(Brush.horizontalGradient(listOf(p.deep, p.hot))))
                    Box(Modifier.weight(18f).fillMaxHeight().background(Color.White.pct(0.08f)))
                }
                Gap(7.2)
                Row(Modifier.fillMaxWidth()) {
                    // weight, not fillMaxWidth: a fraction inside a Row measures against what is left, so the
                    // third column would get 18% of the remainder and wrap one letter per line.
                    BandLabel("Penalty", "< 150", Bad, Modifier.weight(26f))
                    BandLabel("Rank counts here", "150 – 500", p.hot, Modifier.weight(56f))
                    BandLabel("Level only", "500 +", p.faint, Modifier.weight(18f))
                }
            }

            Gap(14.4)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(6.4)),
            ) {
                Tag("Targets · raise any of them")
                Gap(2)
                ThreshRow("Movement", "2,000", "steps", "AGI", true)
                ThreshRow("Walk or run", "1.5", "km", "AGI", true)
                ThreshRow("Screen off", "45", "min block", "INT", true)
                ThreshRow("Scroll cap", "3", "h max", "INT", false)
                ThreshRow("Sleep", "6h 30", "m", "VIT", true)
                ThreshRow("Focus session", "20", "min", "INT", true)
                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        "Auto-raises at rank up. Committing for longer is harder to break.",
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                    Gap(8)
                    Row(horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                        Pill("7 days", on = true); Pill("21 days"); Pill("45 days"); Pill("75 days")
                    }
                }
            }
            Cta("Accept the band")
            Gap(17.6)
        }
    }
}

/** `data-s="weights"` — weighting decides which quests; rank decides how hard. Kept separate on purpose. */
@Composable
fun WeightsScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(0.70f, 0.15f, top = 0.09f, left = 0.15f, alpha = 0.4f)
        Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Settings · any time", color = p.soft); Filler(); Tag("Derived")
            }
            Gap(20.8)
            Text("WHERE YOUR\nQUESTS WILL COME FROM", style = t.display(28))
            Gap(11.2)
            Tag("The System derived this from your intent. Change it whenever.")

            Gap(17.6)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(9.6)),
            ) {
                Slider("Attention", "Screens, feeds, focus", 0.42f)
                Slider("Rest", "Sleep and recovery", 0.24f)
                Slider("Body", "Steps, distance, training", 0.16f)
                Slider("Isolation", "People, calls, going outside", 0.12f)
                Slider("Intake", "Food and ordering", 0.06f)
                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        buildAnnotatedString {
                            append("Weighting decides ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) { append("which") }
                            append(" quests. Your rank decides ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) { append("how hard") }
                            append(". They are separate.")
                        },
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                }
            }
            Cta("Lock weighting")
            Gap(17.6)
        }
    }
}

/** `data-s="newapp"` — new-app detection runs at every app start for the app's whole life, not just at setup. */
@Composable
fun NewAppScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(0.80f, 0.18f, top = 0.10f, left = 0.10f, alpha = 0.44f)
        Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("System Notice"); Filler(); Tag("Day 18")
            }
            Gap(19.2)
            Text("SOMETHING NEW\nARRIVED.", style = t.display(30.4))
            Gap(11.2)
            Tag("Checked at every app start · not just at setup")

            Gap(19.2)
            SystemWindow {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                    Box(
                        Modifier
                            .size(m.d(44))
                            .clip(RoundedCornerShape(m.d(13)))
                            .background(Color(0x2E000000))
                            .border(1.5.dp, p.hot, RoundedCornerShape(m.d(13))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("TH", style = t.questSub.copy(color = p.hot, fontSize = m.s(11.5), fontWeight = FontWeight(800)))
                    }
                    Column {
                        Text("Threads", style = t.md.copy(fontSize = m.s(16.8)))
                        Gap(3.2)
                        Tag("Installed 4 days ago", t.key)
                    }
                }
                Gap(13.6)
                Column(verticalArrangement = Arrangement.spacedBy(m.d(8))) {
                    BaselineLine("Used since install", "3h 12m")
                    BaselineLine("Yesterday alone", "1h 06m")
                    BaselineLine("Category", "social · short video")
                }
            }

            Gap(8)
            Card(Modifier.fillMaxWidth(), padding = 14.4) {
                Text(
                    "No package name left your phone to work this out. The list of categories ships with the app " +
                        "and the match happens on device.",
                    style = t.body.copy(fontSize = m.s(12.2)),
                )
            }

            Filler()
            Cta("Add to the watched list")
            Gap(7.2)
            Cta("Leave it alone", ghost = true)
            Gap(17.6)
        }
    }
}

// ── shared ────────────────────────────────────────────────────────────────────

/** Row children need a height to fill; the band bar sets it on the Row itself. */
private fun Modifier.fillMaxHeight(): Modifier = this.then(Modifier.height(1000.dp).then(Modifier))

@Composable
private fun BandLabel(label: String, range: String, color: Color, modifier: Modifier) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Column(modifier) {
        Tag(label, t.key.copy(color = color))
        Gap(1.6)
        Text(range, style = t.questSub.copy(color = p.faint, fontSize = m.s(9)))
    }
}

@Composable
private fun ThreshRow(label: String, value: String, unit: String, stat: String, adjustable: Boolean) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), padding = 11.5) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = t.questTitle.copy(fontSize = m.s(12.8)))
                Gap(1.9)
                Tag(stat, t.key.copy(color = p.faint))
            }
            Text(value, style = t.md.copy(fontSize = m.s(15.2), color = p.soft))
            GapW(4)
            Tag(unit, t.key)
            if (adjustable) {
                GapW(9.6)
                Text("＋", style = t.tag.copy(color = p.hot, fontSize = m.s(12.8)))
            }
        }
    }
}

@Composable
private fun Slider(label: String, detail: String, fraction: Float) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = t.questTitle.copy(fontSize = m.s(12.8)))
                Gap(1.9)
                Tag(detail, t.key)
            }
            Text("${(fraction * 100).toInt()}%", style = t.monoSmall)
        }
        Gap(5.4)
        Meter(fill = fraction, height = 7)
    }
}

@Composable
private fun SelfRate(label: String, question: String, score: Int) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), padding = 13.6) {
        Text(label, style = t.questTitle.copy(fontSize = m.s(13.4)))
        Gap(2.9)
        Tag(question, t.key)
        Gap(9.6)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
            (1..5).forEach { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(m.d(30))
                        .clip(RoundedCornerShape(m.d(9)))
                        .background(if (i <= score) p.hot.pct(0.22f) else p.card)
                        .border(1.dp, if (i <= score) p.hot.pct(0.5f) else p.line, RoundedCornerShape(m.d(9))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$i", style = t.monoSmall.copy(color = if (i <= score) p.ink else p.faint))
                }
            }
        }
    }
}

@Composable
private fun PrivPick(label: String, detail: String) {
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
        Text(label, style = t.questTitle.copy(fontSize = m.s(13.1)))
        Gap(2.9)
        Tag(detail, t.key)
    }
}

@Composable
private fun BaselineLine(label: String, value: String) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = t.body.copy(fontSize = m.s(12.5), color = p.dim))
        Filler()
        Text(value, style = t.body.copy(fontSize = m.s(12.5), color = p.ink, fontWeight = FontWeight(660)))
    }
}
