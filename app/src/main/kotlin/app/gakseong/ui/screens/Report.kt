package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import app.gakseong.ui.*
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok
import app.gakseong.ui.theme.Warn

/**
 * `data-s="report"` — the weekly post-mortem. It is one of the five things that need a key, so the dormant state
 * is shown in place at the top rather than as a modal: numbers still work, the prose does not.
 */
@Composable
fun ReportScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current

    Screen {
        Bg()
        Bg2()
        Aura(0.82f, 0.22f, top = 0.04f, left = 0.09f, alpha = 0.42f)
        Art(hunter.portrait, top = -0.08f, left = 0.06f, width = 0.88f, alpha = 0.62f)
        Shade(0f to 0.82f, 0.18f to 0.52f, 0.36f to 0.88f, 0.52f to 1f)
        Grain()
        TopFade()

        Body {
            Card(Modifier.fillMaxWidth(), dashed = true, padding = 9.6) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tag("◈ AI dormant · numbers only", t.tag.copy(color = p.soft))
                    Filler()
                    Pill("Awaken")
                }
            }
            Gap(9.6)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Weekly Report"); Filler(); Tag("Week 07")
            }

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                SystemWindow {
                    Tag("Verdict", t.tag.copy(color = p.hot))
                    Gap(5.6)
                    Text(
                        buildAnnotatedString {
                            append("You fail focus sessions started after 9pm.\n")
                            withStyle(SpanStyle(color = p.hot)) { append("Your successful ones average 2:40pm.") }
                        },
                        style = t.body.copy(fontSize = m.s(15.2), fontWeight = FontWeight(600), color = p.ink),
                    )
                }

                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                    Delta("Aura", "6,840", "+22%", Ok, Modifier.weight(1f).fillMaxHeight())
                    Delta("Scroll", "4h12", "−38%", Ok, Modifier.weight(1f).fillMaxHeight())
                }

                Card(Modifier.fillMaxWidth(), padding = 14.4) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Tag("Aura by day"); Filler(); Tag("5 of 7 in band", t.tag.copy(color = p.soft))
                    }
                    Gap(8.8)
                    AuraByDay(
                        values = listOf(1180, 640, 720, 380, 980, 1420, 860),
                        threshold = 500,
                        cap = 1200,
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                    )
                    Legend(
                        listOf(
                            Ok to "in band",
                            Bad to "below threshold",
                            p.ghost to "above cap · level only",
                        )
                    )
                }

                Card(Modifier.fillMaxWidth(), padding = 14.4) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Tag("When you scroll"); Filler(); Tag("peak 22:00–00:00", t.tag.copy(color = p.soft))
                    }
                    HoursHeatmap(
                        listOf(
                            0.05f, 0f, 0f, 0f, 0f, 0f, 0.1f, 0.2f, 0.35f, 0.25f, 0.2f, 0.3f,
                            0.4f, 0.3f, 0.25f, 0.3f, 0.45f, 0.5f, 0.4f, 0.55f, 0.7f, 0.85f, 1f, 0.9f,
                        )
                    )
                    Gap(5.4)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Tag("00", t.key); Tag("06", t.key); Tag("12", t.key); Tag("18", t.key); Tag("23", t.key)
                    }
                }

                Card(Modifier.fillMaxWidth(), padding = 14.4) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Tag("Last 28 days"); Filler()
                        // Row children can exceed the row, so a long right-hand label has to be constrained or
                        // it draws straight over the left one.
                        Text(
                            "24 HELD · 3 SHIELDED · 1 MISSED",
                            style = t.tag.copy(color = p.soft, textAlign = androidx.compose.ui.text.style.TextAlign.End),
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                    DayCalendar(twentyEightDays())
                    Legend(
                        listOf(
                            Ok to "✓ held",
                            Warn to "◆ shield spent",
                            Bad to "✕ missed",
                        )
                    )
                }

                SystemWindow {
                    Tag("◈ What the System found", t.tag.copy(color = p.hot))
                    Gap(9.6)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                        Finding("Every missed threshold this week followed a night gate you skipped.")
                        Finding("Your best three days all started with the phone-down quest before 10am.")
                        Finding("Scroll time moved later, not down, on the two days you cleared the cap.")
                    }
                }

                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                    StatCard("Rank", "E·I → D·III", Modifier.weight(1f).fillMaxHeight())
                    StatCard("Shadows", "+2", Modifier.weight(1f).fillMaxHeight())
                }

                Cta("Share report")
                Gap(17.6)
            }
        }
    }
}

/** 24 held, 3 shielded, 1 missed, which is what the header claims. */
private fun twentyEightDays(): List<DayResult> {
    val days = MutableList(28) { DayResult.HELD }
    listOf(4, 11, 19).forEach { days[it] = DayResult.SHIELDED }
    days[23] = DayResult.MISSED
    return days
}

@Composable
private fun Delta(label: String, value: String, delta: String, deltaColor: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    val t = LocalType.current
    val m = LocalMetrics.current
    Card(modifier, padding = 13.6) {
        Tag(label)
        Gap(4.8)
        Text(value, style = t.lg(24))
        Gap(3.2)
        Tag(delta, t.tag.copy(color = deltaColor))
    }
}

@Composable
private fun Finding(text: String) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
        Text("◈", style = t.tag.copy(color = p.hot, fontSize = m.s(8)))
        Text(text, style = t.body.copy(fontSize = m.s(12.2)))
    }
}
