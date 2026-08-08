package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.Art
import app.gakseong.ui.Aura
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.BottomNav
import app.gakseong.ui.Card
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.GapW
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import app.gakseong.cloud.LEAGUE_SIZE
import app.gakseong.cloud.Standing
import app.gakseong.cloud.memberCount
import app.gakseong.cloud.pushStanding
import app.gakseong.cloud.readLadder
import app.gakseong.ui.LocalSystem
import app.gakseong.ui.Grain
import app.gakseong.ui.Mask
import app.gakseong.ui.MaskedImage
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/**
 * `data-s="league"` — a weekly division of about thirty.
 *
 * The rows come from §9's rules rather than from the design page's row markup, which is generated in JS I could
 * not locate: thin divisions are padded with System-run shadows carrying `◇` and the label `pacer`, they are
 * stated on screen not to be people, and they are never counted as members. No fabricated human usernames.
 */
@Composable
fun LeagueScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    val sys = LocalSystem.current
    val context = LocalContext.current
    val rank = sys.hunter.toEngine().rank

    // The ladder, read once per visit. §Social: thin divisions are padded with labelled pacers, and the count
    // shown is members rather than rows.
    val ladder by produceState<List<Standing>?>(initialValue = null, sys.uid, rank.letter) {
        pushStanding(context, sys.uid, rank.letter, sys.today.auraEarned)
        value = readLadder(context, sys.uid, rank.letter, sys.today.auraEarned)
    }
    // null is loading, empty is a real answer. Conflating them left the screen saying "reading"
    // forever on a project whose Firestore was never provisioned.
    val members = memberCount(ladder.orEmpty())

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.80f, heightFraction = 0.20f, top = 0.02f, left = 0.10f, alpha = 0.42f)
        Art(R.drawable.sc_ruins, top = -0.11f, left = -0.04f, width = 1.06f, alpha = 0.58f)
        Shade(0f to 0.52f, 0.08f to 0f, 0.22f to 0.77f, 0.32f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Weekly League")
                Filler()
                Tag("2d 04h")
            }

            Gap(19.2)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                MaskedImage(R.drawable.em_d, width = 52, mask = Mask.CIRCLE)
                Column {
                    Text("D Division", style = t.md.copy(fontSize = m.s(20.8)))
                    Gap(3.2)
                    Tag("$members of $LEAGUE_SIZE real · same thresholds")
                }
            }

            Gap(16)
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                ZoneCard("Promote", "Top 5", Ok, Modifier.weight(1f).fillMaxHeight())
                ZoneCard("Hold", "6–25", p.dim, Modifier.weight(1f).fillMaxHeight())
                ZoneCard("Demote", "26–30", Bad, Modifier.weight(1f).fillMaxHeight())
            }

            Gap(14.4)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                if (ladder == null) {
                    Tag("Reading the division", t.key)
                } else {
                    ladder!!.forEachIndexed { i, row ->
                        LeagueRow(
                            i + 1,
                            if (row.you) "You" else row.handle,
                            "%,d".format(row.aura),
                            you = row.you,
                            pacer = row.pacer,
                        )
                    }
                }
                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true) {
                    Tag("◇ Pacers are not people", t.tag.copy(color = p.soft))
                    Gap(3.8)
                    Text(
                        "The System runs them at a fixed pace to fill a thin division. They hold no place in the " +
                            "guild and are never counted as members. Each hunter who joins replaces one.",
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
private fun ZoneCard(label: String, range: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    val t = LocalType.current
    Card(modifier, padding = 9.6) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Tag(label)
            Gap(3.8)
            Tag(range, t.tag.copy(color = color, letterSpacing = 0.16.em))
        }
    }
}

@Composable
private fun LeagueRow(place: Int, name: String, aura: String, you: Boolean = false, pacer: Boolean = false) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), lit = you, dashed = pacer, padding = 10.4) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("$place", style = t.monoSmall.copy(color = if (you) p.hot else p.faint), modifier = Modifier.width(m.d(22)))
            // §9 says a pacer carries the diamond and the label. One label: rendering the name and the tag
            // side by side read as "pacerPACER".
            if (pacer) {
                Text("◇", style = t.tag.copy(color = p.soft, fontSize = m.s(11.2)))
                GapW(6.4)
            }
            Text(
                name,
                style = t.questTitle.copy(
                    fontSize = m.s(12.8),
                    color = if (pacer) p.soft else p.ink,
                ),
            )
            if (pacer) {
                GapW(6.4)
                Tag("not a person", t.key.copy(color = p.faint))
            }
            Filler()
            Text(aura, style = t.monoSmall.copy(color = if (you) p.soft else p.dim))
        }
    }
}
