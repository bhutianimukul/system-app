package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/**
 * `data-s="read"` — the one quest that keeps you on the phone. The justification is entirely that the passage
 * ends: twenty minutes of something finite replacing twenty minutes of something that never stops.
 */
@Composable
fun ReaderScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Art(R.drawable.sc_library, top = -0.10f, left = -0.04f, width = 1.06f, alpha = 0.24f)
        Shade(0f to 0.54f, 0.26f to 0.77f, 0.44f to 1f)
        Grain(); TopFade()
        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Reading"); Filler(); Pill("14:22 of 20:00", on = true)
            }
            Gap(12.8)
            Meter(fill = 0.71f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Tag("Passage 2 of 3", t.key)
                Tag("+300 at twenty", t.key)
            }

            Gap(14.4)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                Card(Modifier.fillMaxWidth(), padding = 16) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Tag("◈ Chosen for your record", t.tag.copy(color = p.hot)); Filler()
                        Tag("public domain", t.tag.copy(color = p.faint))
                    }
                    Gap(9.6)
                    Text(
                        "“He who has a why to live can bear almost any how.” The passage runs on for four " +
                            "paragraphs, and then it stops. There is no next one loading beneath it, and nothing " +
                            "arrives while you read.",
                        style = t.body.copy(fontSize = m.s(14.4), lineHeight = 1.72.em, color = p.ink),
                    )
                    Gap(11.2)
                    Tag("Picked because your longest phone-free block was 38 minutes", t.key.copy(color = p.faint))
                }
                SystemWindow {
                    Text(
                        "Time in here counts as reading, not as screen time. That exemption is the only reason a " +
                            "reader belongs in this app at all.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        "App-initiated tier, so it pays medium. The phone can confirm the reader was open and " +
                            "scrolling, and nothing more than that.",
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                }
                Gap(4)
            }
            Cta("End the session")
            Gap(17.6)
        }
    }
}

/** `data-s="shadows"` — the army. The count is public; §7 keeps what earned it private. */
@Composable
fun ShadowsScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val extracted = listOf(
        R.drawable.sh_01_knight, R.drawable.sh_02_wolf, R.drawable.sh_03_serpent,
        R.drawable.sh_04_wraith, R.drawable.sh_05_bull, R.drawable.sh_06_drone,
    )
    Screen {
        Bg(); Bg2()
        Aura(0.84f, 0.20f, top = 0.02f, left = 0.08f, alpha = 0.4f)
        Art(R.drawable.sc_monarch, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.62f)
        Shade(0f to 0.52f, 0.08f to 0f, 0.24f to 0.77f, 0.34f to 1f)
        Grain(); TopFade()
        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Shadow Army"); Filler(); Tag("6 / 10")
            }
            Gap(19.2)
            Text("Extracted from\nwhat you beat", style = t.md.copy(fontSize = m.s(20.8)))
            Gap(8)
            Tag("7 · 30 · 90 day thresholds")

            Gap(17.6)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(6.4)),
            ) {
                extracted.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                        row.forEach { res ->
                            Card(Modifier.weight(1f), padding = 8) {
                                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    MaskedImage(res, width = 74, height = 88, mask = Mask.SQUARISH, keyed = true)
                                }
                            }
                        }
                        repeat(3 - row.size) { Card(Modifier.weight(1f), dashed = true, padding = 8) { Gap(88) } }
                    }
                }
                Gap(8)
                Tag("Extracted")
                Card(Modifier.fillMaxWidth(), padding = 14.4) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                        MaskedImage(R.drawable.sh_01_knight, width = 44, height = 52, mask = Mask.SQUARISH, keyed = true)
                        Column {
                            Text("Silence of the Third Watch", style = t.questTitle.copy(fontSize = m.s(13.4)))
                            Gap(2.9)
                            Tag("Rank C · passive +3% aura", t.key)
                        }
                    }
                }
                Card(Modifier.fillMaxWidth(), padding = 14.4) {
                    Tag("Locked")
                    Gap(4)
                    Text(
                        "Four remain. The System does not disclose which fight yields which shadow.",
                        style = t.body.copy(fontSize = m.s(12.5)),
                    )
                }
                Gap(11.2)
            }
        }
        BottomNav(active = 4)
    }
}

/** `data-s="complete"` — a Pact closed clean. The rarest screen in the app, so it says almost nothing. */
@Composable
fun CompleteScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Art(R.drawable.sc_hero, top = -0.03f, left = -0.03f, width = 1.06f, alpha = 0.78f)
        Shade(0f to 0.45f, 0.10f to 0f, 0.42f to 0f, 0.58f to 0.82f, 0.70f to 1f)
        Grain(); TopFade()
        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Pact Complete")
                Filler()
                XlNumber("21", designPx = 73.6)
                Gap(1.6)
                Text("DAYS.\nNOT ONE MISSED.", style = t.display(24.8).copy(textAlign = TextAlign.Center))

                Gap(17.6)
                SystemWindow {
                    Tag("Earned", t.tag.copy(color = p.hot))
                    Gap(9.6)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(8))) {
                        EarnedRow("Title") { Pill("Unbroken", on = true) }
                        EarnedRow("Shadow") { Text("Warden of Quiet Nights", style = t.monoSmall) }
                        EarnedRow("Aura") { Text("+4,200", style = t.monoSmall) }
                        EarnedRow("Rank") { Text("D · III → D · II", style = t.monoSmall) }
                    }
                }

                Gap(8)
                Card(Modifier.fillMaxWidth(), accent = Ok, padding = 14.4) {
                    Tag("The next Pact is longer", t.tag.copy(color = Ok))
                    Gap(4)
                    Text(
                        "Twenty-one days held means the System will offer forty-five. Committing for longer is " +
                            "harder to break, and that is the entire mechanism.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }

                Filler()
                Cta("Take the next Pact")
                Gap(7.2)
                Cta("Rest first", ghost = true)
                Gap(17.6)
            }
        }
    }
}

/** `data-s="monarch"` — the monthly S-rank gate. Everything is required, so partial credit does not exist. */
@Composable
fun MonarchScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(1.10f, 0.30f, top = 0.20f, left = -0.05f, alpha = 0.44f, color = Bad)
        Art(R.drawable.sc_dragon, top = 0f, left = -0.04f, width = 1.06f, alpha = 0.92f)
        Shade(0f to 0.67f, 0.14f to 0.14f, 0.32f to 0f, 0.50f to 0.67f, 0.64f to 0.95f, 0.74f to 1f)
        Grain(); TopFade()
        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Monarch Gate · S", color = Bad)
                Filler()
                Tag("Monthly · 7 day window", t.tag.copy(color = Bad))
                Gap(5.6)
                Text("THE SUNDERED", style = t.display(34.4).copy(textAlign = TextAlign.Center))

                Gap(19.2)
                Card(Modifier.fillMaxWidth(), big = true, accent = Bad, padding = 15.2) {
                    Tag("All required", t.tag.copy(color = Bad))
                    Gap(9.6)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(8))) {
                        Requirement("Night gate · 7 nights", "5/7", Ok)
                        Requirement("Focus sessions held", "11/14", Ok)
                        Requirement("Zero forbidden app opens", "6/7", Ok)
                        Requirement("A raid cleared with a person", "0/1", Bad)
                    }
                }

                Gap(8)
                Card(Modifier.fillMaxWidth(), padding = 14.4) {
                    Text(
                        "No partial credit. An S-rank gate that paid out for most of the work would make S mean " +
                            "the same thing as A.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }

                Filler()
                Cta("Enter the Monarch Gate", bad = true)
                Gap(17.6)
            }
        }
    }
}

/** `data-s="gates"` — five tiers of open gate. Hotter art means harder. */
@Composable
fun GatesScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Screen {
        Bg(); Bg2()
        Aura(0.84f, 0.18f, top = 0.02f, left = 0.08f, alpha = 0.4f)
        Grain(); TopFade()
        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Open Gates"); Filler(); Tag("3 nearby")
            }
            Gap(16)
            Text("Five tiers.\nHotter means harder.", style = t.md.copy(fontSize = m.s(20.8)))

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(6.4)),
            ) {
                GateRow(R.drawable.gt_1_iron, "Iron Hollow", "E · 0.8 km", "400", true)
                GateRow(R.drawable.gt_2_silver, "Silver Reach", "D · 1.4 km", "500", true)
                GateRow(R.drawable.gt_3_brass, "Hollow Ward", "C · 2.1 km", "600", true)
                GateRow(R.drawable.gt_4_gold, "Gilded Fault", "B · 4.6 km", "800", false)
                GateRow(R.drawable.gt_5_molten, "Molten Spire", "A · 9.2 km", "1,100", false)
                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        "Location is checked once, on a tap, when you arrive. Passive geofencing would need a " +
                            "restricted permission and a service that OEM battery managers kill anyway.",
                        style = t.body.copy(fontSize = m.s(12)),
                    )
                }
                Gap(11.2)
            }
        }
        BottomNav(active = 1)
    }
}

// ── shared ────────────────────────────────────────────────────────────────────

@Composable
private fun EarnedRow(label: String, trailing: @Composable () -> Unit) {
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = t.body.copy(fontSize = m.s(12.8)))
        Filler()
        trailing()
    }
}

@Composable
private fun Requirement(label: String, progress: String, color: Color) {
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = t.body.copy(fontSize = m.s(12.8)))
        Filler()
        Tag(progress, t.tag.copy(color = color))
    }
}

@Composable
private fun GateRow(art: Int, name: String, detail: String, reward: String, near: Boolean) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), lit = near, padding = 11.2) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
            PortraitChip(art, width = 38, height = 44)
            Column(Modifier.weight(1f)) {
                Text(name, style = t.questTitle.copy(fontSize = m.s(13.1)))
                Gap(2.4)
                Tag(detail, t.key)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(reward, style = t.monoSmall)
                Gap(1.9)
                Tag(if (near) "nearby" else "too far", t.key.copy(color = if (near) p.hot else p.faint))
            }
        }
    }
}
