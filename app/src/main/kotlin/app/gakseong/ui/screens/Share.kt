package app.gakseong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.gakseong.R
import app.gakseong.data.SystemState
import app.gakseong.ui.Art
import app.gakseong.ui.Aura
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.Card
import app.gakseong.ui.Cta
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.LocalSystem
import app.gakseong.ui.Pill
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Radius
import gakseong.engine.Rank

/** Which moment the card is for. §28 started with ascension; a raid clear is the other one worth posting. */
enum class ShareMoment { ASCENSION, RAID }

/**
 * `data-s="share"` — DECISIONS.md §28. The preview exists because capture needs the card laid out anyway, and
 * because nobody should post something they have not seen. Everything on the card comes from an allowlist.
 *
 * A raid card cannot name the partner. §28's allowlist excludes guild members, so the card says it was held with
 * one other hunter and leaves it there. Whoever posts it can name them in their own caption if they want to.
 */
@Composable
fun ShareScreen(moment: ShareMoment = ShareMoment.ASCENSION) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current
    val sys = LocalSystem.current
    val card = cardFields(sys, hunter.label, moment)

    Screen {
        Bg()
        Bg2()
        Grain()
        TopFade()

        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye(if (moment == ShareMoment.ASCENSION) "Ascension · Share" else "Raid · Share")
                Filler()
                Tag("1080 × 1920")
            }

            Gap(14.4)
            // The card at the ratio it will be written out in. At full width a 9:16 card is taller than the
            // screen once the note and the share row are accounted for, so the preview is inset and centred.
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.72f)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(m.d(Radius.BIG)))
                    .background(p.base)
                    .border(1.dp, p.line, RoundedCornerShape(m.d(Radius.BIG))),
            ) {
                Art(
                    if (moment == ShareMoment.ASCENSION) R.drawable.sc_arise else R.drawable.sc_hero,
                    top = 0f, left = -0.02f, width = 1.04f, alpha = 0.95f,
                )
                Aura(widthFraction = 0.76f, heightFraction = 0.22f, top = 0.26f, left = 0.12f, alpha = 0.5f)
                // The card carries text over its own art, so it darkens earlier than a screen would: everything
                // below the portrait has to stay legible in someone else's Instagram feed.
                Shade(0f to 0.35f, 0.22f to 0f, 0.45f to 0.55f, 0.60f to 0.92f, 0.72f to 1f)
                Column(
                    Modifier
                        .matchParentSize()
                        .padding(horizontal = m.d(14.4), vertical = m.d(16)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    PortraitChip(hunter.portrait, width = 46, height = 56)
                    Gap(9.6)
                    // Every line below reads a field of `card` and nothing else. Adding anything to this card
                    // means adding a field to CardFields first, which is what makes §10 an allowlist.
                    Tag("${card.hunterClass} · ${card.streakDays} days held")
                    Gap(4.8)
                    Text(
                        buildAnnotatedString {
                            append("${card.rankBefore} → ")
                            withStyle(SpanStyle(color = p.soft, fontWeight = FontWeight(900))) {
                                append(card.rankAfter)
                            }
                        },
                        style = t.md.copy(fontSize = m.s(21.6)),
                    )
                    Gap(4.8)
                    Tag("Level ${card.level}")
                    Gap(11.2)
                    Text(
                        card.script,
                        style = t.body.copy(fontSize = m.s(11.8), textAlign = TextAlign.Center),
                        modifier = Modifier.width(m.d(190)),
                    )
                    Gap(14.4)
                    Text("각성 GAKSEONG", style = t.wordmarkLatin.copy(fontSize = m.s(9.3), color = p.soft))
                    Gap(4)
                    Text("gakseong.app/s/7F2K9Q", style = t.questSub.copy(fontSize = m.s(9)))
                }
            }

            Gap(8)
            Card(Modifier.fillMaxWidth(), big = true) {
                Tag("What the card can never carry", t.tag.copy(color = p.hot))
                Gap(4.8)
                Text(
                    "No app name, no duration, no screen-time number, no guild member, nothing from the private " +
                        "track. Built from an allowlist, not a blocklist.",
                    style = t.body.copy(fontSize = m.s(12.5)),
                )
            }

            Filler()
            Cta(if (moment == ShareMoment.ASCENSION) "Share ascension" else "Share the raid")
            Gap(8.8)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                Pill("WhatsApp")
                Pill("Instagram")
                Pill("Copy")
            }
            Gap(8)
            Tag("The link is in the image and in the text · Stories drops the text")
            Gap(17.6)
        }
    }
}

/**
 * §10, as a type. Everything a share card may carry, and nothing else.
 *
 * This is an allowlist rather than a blocklist on purpose: putting anything new on the card means adding a
 * field here first, so a package name, a duration or a screen-time number cannot arrive by an innocent edit.
 * There is no "but the user chose to" exemption, and no share affordance exists anywhere in the private track.
 *
 * The enumerated list in CLAUDE.md is: class portrait, rank before and after, level, streak days, one line of
 * fixed System script, the 각성 GAKSEONG wordmark, gakseong.app/s/<code>.
 */
private data class CardFields(
    val hunterClass: String,
    val rankBefore: String,
    val rankAfter: String,
    val level: Int,
    val streakDays: Int,
    val script: String,
)

// Fixed System script, one line per moment. Never generated, never per-user.
private const val ASCENSION_SCRIPT =
    "The gate did not open for you. You were simply strong enough to walk through it."
private const val RAID_SCRIPT = "Neither of us broke. The System had nothing to take."

/**
 * ponytail: `rankBefore` is the last rank in history that differs from the current one. Until phase 05 writes
 * settlements, history is empty and the card shows the tier below, which is what an ascension always is.
 */
private fun cardFields(sys: SystemState, hunterClass: String, moment: ShareMoment): CardFields {
    val rank = sys.hunter.toEngine().rank
    val before = Rank(maxOf(0, rank.ordinal - 1))
    return CardFields(
        hunterClass = hunterClass,
        rankBefore = before.label,
        rankAfter = rank.label,
        level = sys.level,
        streakDays = sys.hunter.streak,
        script = if (moment == ShareMoment.ASCENSION) ASCENSION_SCRIPT else RAID_SCRIPT,
    )
}
