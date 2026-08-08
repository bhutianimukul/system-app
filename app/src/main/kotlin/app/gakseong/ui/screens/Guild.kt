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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.gakseong.cloud.createGuild
import app.gakseong.data.Repo
import kotlinx.coroutines.launch
import app.gakseong.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import app.gakseong.cloud.GUILD_MAX
import app.gakseong.cloud.Guild
import app.gakseong.cloud.handleFor
import app.gakseong.cloud.readGuild
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
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    var refresh by remember { mutableIntStateOf(0) }
    val guild by produceState<Guild?>(initialValue = null, sys.uid, refresh) {
        value = readGuild(context, sys.uid)
    }

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
                Eye("Guild"); Filler(); Tag(guild?.let { "${it.members.size} of $GUILD_MAX" } ?: "No guild")
            }
            Gap(19.2)
            Text(guild?.name ?: "No guild yet", style = t.md.copy(fontSize = m.s(21.6)))
            Gap(8)
            Tag(guild?.let { "Founded ${it.foundedOn} · invite only" } ?: "Twenty at most · invite only")

            Gap(17.6)
            if (guild == null) Card(Modifier.fillMaxWidth(), dashed = true, padding = 13.6) {
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
                MemberRow("You", rank.label, sys.today.auraEarned.toString(), you = true)
                // Everyone else by handle. §Social forbids plausible human usernames, and a guild feed is
                // exactly where a fabricated member would be noticed.
                guild?.members.orEmpty().filterNot { it == sys.uid }.forEach { member ->
                    MemberRow(handleFor(member, rank.letter), rank.label, "—")
                }
                Gap(11.2)
            }

            if (guild == null) {
                // §Social: the id is the invite code, so founding one is the whole of joining one. No friend
                // list, no pairing step, no code to type beyond the link itself.
                Cta("Found a guild", onClick = {
                    scope.launch {
                        createGuild(context, "Shadow Wardens", sys.uid, Repo.today())
                        refresh++
                    }
                })
                Gap(17.6)
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
