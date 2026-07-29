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

/**
 * `data-s="feed"` — twenty posts a day, guild-scoped, then it closes. An endless scroll inside an anti-scroll
 * app defeats the app, which is the same argument that keeps the reader finite.
 */
@Composable
fun FeedScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Aura(0.80f, 0.18f, top = 0.02f, left = 0.10f, alpha = 0.36f)
        Art(R.drawable.sc_monarch, top = -0.11f, left = -0.04f, width = 1.06f, alpha = 0.58f)
        Shade(0f to 0.48f, 0.08f to 0f, 0.22f to 0.75f, 0.32f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Guild Feed"); Filler(); Pill("14 / 20 today", on = true)
            }
            Gap(17.6)
            Text("This feed ends.", style = t.md.copy(fontSize = m.s(20.5)))
            Gap(7.2)
            Tag("Twenty posts a day · then the System closes it")

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                Post("Rahul", "held a 45-minute focus session", "+300", "18:20")
                Post("Aditi", "cleared the night gate", "+260", "06:02")
                Post("Kabir", "reached C · III", null, "yesterday")
                Post("Neha", "ran 5.02 km inside the raid window", "+450", "yesterday")

                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true) {
                    Tag("6 posts remain")
                    Gap(3.2)
                    Text(
                        "Guild only. No strangers, no discovery, no infinite scroll.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }
                Gap(11.2)
            }
        }
        BottomNav(active = 3)
    }
}

@Composable
private fun Post(who: String, what: String, aura: String?, when_: String) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), padding = 11.2) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
            Sig(size = 18)
            Column(Modifier.weight(1f)) {
                Text(who, style = t.questTitle.copy(fontSize = m.s(12.8)))
                Gap(1.9)
                Text(what, style = t.body.copy(fontSize = m.s(12.2)))
            }
            Column(horizontalAlignment = Alignment.End) {
                if (aura != null) Text(aura, style = t.monoSmall.copy(fontSize = m.s(9.6)))
                Gap(1.9)
                Tag(when_, t.key)
            }
        }
    }
}
