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
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/** `data-s="soon"` — sealed gates. Naming what is not built yet is cheaper than pretending it is. */
@Composable
fun SoonScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_ruins, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.4f)
        Shade(0f to 0.42f, 0.08f to 0f, 0.24f to 0.67f, 0.38f to 1f)
        Grain()
        TopFade()

        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Not Yet Open"); Filler(); Tag("4 gates sealed")
            }
            Gap(16)
            Text("Gates that have\nnot opened yet.", style = t.display(24))

            Gap(14.4)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                Sealed("Aura Exchange", "Banked aura buys art, sigils, themes and shadow skins. Cosmetics only, and never anything that touches a threshold, a shield or a rank.")
                Sealed("Global ladder", "Opens when the population can fill it. A global ladder looks abandoned at low numbers, so leagues of thirty come first.")
                Sealed("Containment", "Real blocking. Earned by repeated failure rather than switched on, because the permission it needs would kill installs on day one.")
                Sealed("Dungeon parties", "More than two hunters against one objective. The two-person raid has to prove itself first.")
                Gap(11.2)
            }
        }
    }
}

@Composable
private fun Sealed(title: String, why: String) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
            Text("◎", style = t.md.copy(fontSize = m.s(14.4), color = p.soft))
            Text(title, style = t.questTitle.copy(fontSize = m.s(13.4)))
            Filler()
            Tag("sealed", t.key)
        }
        Gap(4.8)
        Text(why, style = t.body.copy(fontSize = m.s(11.8)))
    }
}
