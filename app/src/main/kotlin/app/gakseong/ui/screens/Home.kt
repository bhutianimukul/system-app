package app.gakseong.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.font.FontWeight
import app.gakseong.R
import app.gakseong.ui.Art
import app.gakseong.ui.Aura
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.BottomNav
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.Meter
import app.gakseong.ui.QuestCard
import app.gakseong.ui.QuestState
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Sig
import app.gakseong.ui.SystemWindow
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.Wordmark
import app.gakseong.ui.XlNumber
import app.gakseong.data.QuestInstance
import app.gakseong.ui.LocalNav
import app.gakseong.ui.LocalSystem
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import gakseong.engine.Balance
import gakseong.engine.Provability
import gakseong.engine.Rank
import gakseong.engine.award
import gakseong.engine.bandFor

/**
 * Screen 1, Home. The layer order and every number here come straight from the design page's `data-s="home"`
 * section, so the two can be diffed side by side.
 */
@Composable
fun HomeScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current
    val sys = LocalSystem.current

    val engine = sys.hunter.toEngine()
    val rank = engine.rank
    val band = bandFor(rank)
    val next = if (rank.ordinal < Rank.MAX) Rank(rank.ordinal + 1) else rank
    val toNext = (bandFor(next).threshold - sys.today.auraEarned).coerceAtLeast(0)
    val cleared = sys.today.auraEarned >= band.threshold
    val fill = (sys.today.auraEarned.toFloat() / band.cap).coerceIn(0f, 1f)
    val marker = (band.threshold.toFloat() / band.cap).coerceIn(0f, 1f)
    val toShield = Balance.SHIELD_EVERY_DAYS - (sys.hunter.streak % Balance.SHIELD_EVERY_DAYS)
    val shieldLine = if (sys.hunter.shields >= Balance.MAX_SHIELDS) "Shields full" else "Shadow in $toShield days"

    Screen {
        // Painted in z-index order, not markup order: aura 1, art 2, shade 3, grain 4, topfade 5, body 8, nav 9.
        Bg()
        Bg2()
        Aura(widthFraction = 0.78f, heightFraction = 0.22f, top = 0.06f, left = 0.11f, alpha = 0.5f)
        Art(
            R.drawable.sc_portal, top = -0.06f, left = 0.24f, width = 0.52f, alpha = 0.95f,
            blend = BlendMode.Screen, feather = true,
        )
        Art(hunter.portrait, top = -0.03f, left = 0.02f, width = 0.96f)
        Shade(0f to 0.57f, 0.10f to 0f, 0.34f to 0f, 0.44f to 0.82f, 0.52f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(verticalAlignment = Alignment.Bottom) {
                Wordmark()
                Filler()
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(m.d(2.6))) {
                    Tag(rank.label, t.tag.copy(color = p.hot, fontWeight = FontWeight(700)))
                    Tag("LV ${sys.level} · ${sys.hunter.streak}d", t.tag.copy(color = p.soft))
                }
            }

            Gap(10.4)
            Eye("Daily Quest")

            Gap(118.4)
            Tag("Aura today")
            Gap(4)
            Row(verticalAlignment = Alignment.Bottom) {
                XlNumber(sys.today.auraEarned.toString())
                Filler()
                Column {
                    Tag("$toNext to ${next.label}", t.tag.copy(color = p.soft))
                    Gap(8)
                }
            }

            Gap(17.6)
            Meter(fill = fill, marker = marker)
            Gap(6.7)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Tag(if (cleared) "Threshold cleared" else "Below threshold", t.key)
                Tag(shieldLine, t.key)
            }

            Gap(17.6)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                // The value on a card is award(baseAura, provability), never a literal, so a declared quest
                // cannot show a sensor rate no matter what the bank hands over.
                val narrow = sys.today.quests.filterNot { it.wide }
                narrow.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
                        pair.forEach { q -> QuestCard(q, Modifier.weight(1f)) }
                        // An odd count would otherwise stretch the last card across the full width.
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                sys.today.quests.filter { it.wide }.forEach { q ->
                    QuestCard(q, Modifier.fillMaxWidth())
                }

                sys.today.bonus?.let { bonus ->
                    Gap(3.2)
                    SystemWindow {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Tag("⚡ Bonus spawned", t.tag.copy(color = p.hot))
                            Filler()
                            Tag(expiryLabel(bonus.expiresAtEpochMs), t.tag.copy(color = p.soft))
                        }
                        Gap(7.2)
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(m.d(9.6)),
                        ) {
                            Sig()
                            Column(Modifier.weight(1f)) {
                                Text(bonus.title, style = t.listItem)
                                Gap(2.2)
                                Tag(bonus.detail, t.tag.copy(letterSpacing = t.key.letterSpacing))
                            }
                            Text("+${bonus.aura}", style = t.monoSmall)
                        }
                    }
                }
                Gap(11.2)
            }
        }

        BottomNav(active = 0)
    }
}

/**
 * One quest instance as a card. The aura shown is the engine's answer, not the template's base.
 *
 * A focus quest is the only one with somewhere to go: the others are proven by living, and a card that opened
 * a screen for "have a meal with people" would be the app getting in the way of the thing it asked for.
 */
@Composable
private fun QuestCard(q: QuestInstance, modifier: Modifier = Modifier) {
    val nav = LocalNav.current
    val startable = q.id.startsWith("focus-") || q.id == "read-20"
    QuestCard(
        icon = q.icon,
        title = q.title,
        sub = q.sub,
        value = "+${award(q.baseAura, Provability.valueOf(q.provability))}",
        state = QuestState.valueOf(q.state),
        wide = q.wide,
        modifier = if (startable && q.state != "DONE") {
            modifier.clickable { nav(if (q.id == "read-20") "read" else "focus") }
        } else {
            modifier
        },
    )
}

/** `expires 41:08`, and `expired` once the clock has run out rather than a negative count. */
private fun expiryLabel(expiresAtEpochMs: Long): String {
    val left = expiresAtEpochMs - System.currentTimeMillis()
    if (left <= 0L) return "expired"
    val minutes = left / 60_000
    return "expires %02d:%02d".format(minutes / 60, minutes % 60)
}
