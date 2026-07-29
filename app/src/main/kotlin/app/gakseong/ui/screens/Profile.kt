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
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/** `data-s="profile"` — the hunter record. Level is here and nowhere public; Rank is the part others see. */
@Composable
fun ProfileScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current

    Screen {
        Bg()
        Bg2()
        Aura(0.80f, 0.24f, top = 0.08f, left = 0.10f, alpha = 0.44f)
        Art(hunter.portrait, top = -0.03f, left = -0.01f, width = 1.02f)
        Shade(0f to 0.57f, 0.12f to 0f, 0.40f to 0f, 0.54f to 0.88f, 0.64f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Hunter Record"); Filler(); Tag("Level 34")
            }
            Gap(134.4)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                MaskedImage(R.drawable.em_d, width = 60, mask = Mask.CIRCLE)
                Column {
                    Text("SENTINEL", style = t.md.copy(fontSize = m.s(20.8), letterSpacing = 0.08.em))
                    Gap(3.5)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                        Sig(size = 18)
                        Tag("${hunter.label} · Rank D · Tier III")
                    }
                }
            }

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                Card(Modifier.fillMaxWidth(), big = true, padding = 14.4) {
                    Tag("Condition")
                    Gap(8)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                        PortraitChip(R.drawable.cd_1_normal, width = 48, height = 60, alpha = 0.92f)
                        Column {
                            Text("NORMAL", style = t.md.copy(fontSize = m.s(16), color = Ok, letterSpacing = 0.06.em))
                            Gap(2.4)
                            Text(
                                "HRV within baseline · sleep debt 0.4h\nFull quest load.",
                                style = t.body.copy(fontSize = m.s(11.5)),
                            )
                        }
                    }
                }

                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                    listOf("STR" to 15, "AGI" to 27, "VIT" to 13, "INT" to 21).forEach { (n, v) ->
                        Card(Modifier.weight(1f).fillMaxHeight(), padding = 11.2) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Tag(n)
                                Gap(4.2)
                                Text("$v", style = t.lg(20))
                            }
                        }
                    }
                }

                Card(Modifier.fillMaxWidth(), padding = 14.4) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Tag("Shadow army"); Filler(); Tag("6 of 10", t.tag.copy(color = p.soft))
                    }
                    Gap(8.8)
                    Row(horizontalArrangement = Arrangement.spacedBy(m.d(4.2))) {
                        listOf(
                            R.drawable.sh_01_knight, R.drawable.sh_02_wolf, R.drawable.sh_03_serpent,
                            R.drawable.sh_04_wraith, R.drawable.sh_05_bull, R.drawable.sh_06_drone,
                        ).forEach { MaskedImage(it, width = 34, height = 40, mask = Mask.SQUARISH, keyed = true) }
                    }
                }

                Card(Modifier.fillMaxWidth(), padding = 14.4) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Tag("Titles · chase the locked ones"); Filler()
                        Tag("3 of 11 earned", t.tag.copy(color = p.soft))
                    }
                    Gap(8.8)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                        TitleRow("Nightbound", earned = true)
                        TitleRow("Unbroken", earned = true)
                        TitleRow("Silent", earned = true)
                        // Above half renders brighter, so the two nearest surface themselves.
                        TitleRow("Ascetic", progress = 18 to 30)
                        TitleRow("Wayfarer", progress = 11 to 25)
                        TitleRow("Void-Touched", progress = 3 to 40)
                    }
                }
                Gap(11.2)
            }
        }
        BottomNav(active = 4)
    }
}

@Composable
private fun TitleRow(name: String, earned: Boolean = false, progress: Pair<Int, Int>? = null) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val ratio = progress?.let { it.first.toFloat() / it.second }
    val near = (ratio ?: 0f) > 0.5f
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                name,
                style = t.questTitle.copy(
                    fontSize = m.s(12.8),
                    color = when {
                        earned -> p.ink
                        near -> p.dim
                        else -> p.ghost
                    },
                ),
            )
            Filler()
            if (earned) Tag("earned", t.key.copy(color = p.hot))
            else Tag("${progress!!.first} / ${progress.second}", t.key.copy(color = if (near) p.soft else p.faint))
        }
        if (ratio != null) {
            Gap(3.2)
            Meter(fill = ratio, height = 4)
        }
    }
}
