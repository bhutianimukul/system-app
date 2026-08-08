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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/** `data-s="guild"` — invite only, twenty at most. Guilds exist before any global ladder does. */
@Composable
fun GuildScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val sys = LocalSystem.current
    val rank = sys.hunter.toEngine().rank

    Screen {
        Bg()
        Bg2()
        Aura(0.82f, 0.20f, top = 0.03f, left = 0.09f, alpha = 0.42f)
        Art(R.drawable.sc_ruins, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.66f)
        Shade(0f to 0.52f, 0.08f to 0f, 0.24f to 0.77f, 0.34f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Guild"); Filler(); Tag("Not connected")
            }
            Gap(19.2)
            Text("No guild yet", style = t.md.copy(fontSize = m.s(21.6)))
            Gap(8)
            Tag("Guilds arrive when the System reaches the network")

            Gap(17.6)
            Card(Modifier.fillMaxWidth(), dashed = true, padding = 13.6) {
                Tag("Why this is empty", t.tag.copy(color = p.hot))
                Gap(4.2)
                Text(
                    "The ladder is the one thing in this app that has to be trustworthy, so nobody is shown " +
                        "here until there is somebody real to show. A guild feed that nobody posts in gets " +
                        "noticed, and so does a member who never appears.",
                    style = t.body.copy(fontSize = m.s(12.2)),
                )
            }

            Gap(8.8)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                // Your own row is real today. Everyone else's arrives from Firestore in phase 07.
                MemberRow("You", rank.label, sys.today.auraEarned.toString(), you = true)
                Gap(11.2)
            }
        }
        BottomNav(active = 3)
    }
}

@Composable
private fun MemberRow(name: String, rank: String, aura: String, you: Boolean = false) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), lit = you, padding = 10.4) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
            Text(name, style = t.questTitle.copy(fontSize = m.s(12.8)))
            Tag(rank, t.key.copy(color = if (you) p.hot else p.faint))
            Filler()
            Text(aura, style = t.monoSmall.copy(color = if (you) p.soft else p.dim))
        }
    }
}
