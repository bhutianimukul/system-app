package app.gakseong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Warn
import app.gakseong.ui.theme.pct

/**
 * `data-s="settings"` — five tabs. Every term §13 promised is reversible is reversible here, which is what makes
 * the single accept gate at onboarding honest.
 */
@Composable
fun SettingsScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val haptics = rememberHaptics()
    var tab by remember { mutableStateOf(Tab.GENERAL) }

    Screen {
        Bg(); Bg2()
        Art(R.drawable.sc_eye, top = -0.03f, left = 0.18f, width = 0.64f, alpha = 0.5f, feather = true)
        Shade(0f to 0.42f, 0.10f to 0f, 0.26f to 0.59f, 0.38f to 1f)
        Grain(); TopFade()

        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Settings"); Filler(); Tag("Rank D · Level 34")
            }

            Gap(16)
            // `.settabs` — a pill of segments, the pressed one filled with the accent.
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Color.White.pct(0.04f))
                    .border(1.dp, p.line, CircleShape)
                    .padding(m.d(4.5)),
                horizontalArrangement = Arrangement.spacedBy(m.d(4.2)),
            ) {
                Tab.entries.forEach { candidate ->
                    val on = candidate == tab
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(if (on) p.hot else Color.Transparent)
                            .clickable {
                                haptics.tick()
                                tab = candidate
                            }
                            .padding(vertical = m.d(7.4)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            candidate.label.uppercase(),
                            style = t.key.copy(
                                color = if (on) Color(0xFF07080C) else p.faint,
                                fontWeight = if (on) FontWeight(800) else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                }
            }

            Gap(13.6)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(6.4)),
            ) {
                if (tab == Tab.AI) {
                    SystemWindow {
                        Tag("Standing directives", t.tag.copy(color = p.hot))
                        Gap(4.2)
                        Text(
                            "“Never give me exercise quests on Mondays. I work nights. Push focus blocks to " +
                                "afternoons.”",
                            style = t.body.copy(fontSize = m.s(12)),
                        )
                        Gap(8.8)
                        Row(horizontalArrangement = Arrangement.spacedBy(m.d(4.8))) {
                            Pill("3 active", on = true); Pill("Edit")
                        }
                    }
                    Gap(3)
                }

                rows(tab).forEach { row -> SetRow(row) }

                if (tab == Tab.PRIVACY) {
                    Gap(3)
                    Card(Modifier.fillMaxWidth(), accent = Warn, padding = 13.6) {
                        Tag("Free-tier AI", t.tag.copy(color = Warn))
                        Gap(3.2)
                        Text(
                            buildAnnotatedString {
                                append("Google may use free-tier traffic to improve their models. ")
                                withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                    append("Do not write anything in the chat you would not want read.")
                                }
                            },
                            style = t.body.copy(fontSize = m.s(11.8)),
                        )
                    }
                }
                Gap(11.2)
            }
        }
        BottomNav(active = 4)
    }
}

private enum class Tab(val label: String) {
    GENERAL("General"), SOUND("Sound"), AI("AI"), PRIVACY("Privacy"), DATA("Data")
}

/** A row is either a value or a switch. `kind` in the design's data is exactly that distinction. */
private data class Setting(val title: String, val value: String, val detail: String, val switch: Boolean? = null)

@Composable
private fun SetRow(s: Setting) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    var on by remember(s.title) { mutableStateOf(s.switch == true) }
    val haptics = rememberHaptics()

    val clickable = if (s.switch != null) {
        Modifier.clickable {
            haptics.tick()
            on = !on
        }
    } else {
        Modifier
    }

    Card(Modifier.fillMaxWidth().then(clickable), padding = 12.8) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.title, style = t.listItem.copy(fontSize = m.s(13.3)))
                if (s.detail.isNotEmpty()) {
                    Gap(2.9)
                    Tag(s.detail, t.key)
                }
            }
            if (s.switch != null) {
                // `.sw` — 34×19 track, 14px knob, and the accent only when it is on.
                Box(
                    Modifier
                        .width(m.d(34))
                        .height(m.d(19))
                        .clip(CircleShape)
                        .background(if (on) p.hot else Color.White.pct(0.12f)),
                ) {
                    Box(
                        Modifier
                            .offset(x = m.d(if (on) 17 else 2.5), y = m.d(2.5))
                            .size(m.d(14))
                            .clip(CircleShape)
                            .background(if (on) Color.White else Color(0xFF8C93A6))
                    )
                }
            } else {
                Text(
                    s.value,
                    style = t.questValue.copy(fontSize = m.s(9.6), textAlign = TextAlign.End),
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }
}

private fun rows(tab: Tab): List<Setting> = when (tab) {
    Tab.GENERAL -> listOf(
        Setting("Quest issued at", "00:01", "When the daily quest arrives"),
        Setting("Night gate window", "00:30 – 06:00", "Adjustable 30 min either way"),
        Setting("Do Not Disturb in sessions", "", "Turns itself off when the session ends", switch = true),
        Setting("Let through", "Calls · starred", "Repeat callers always ring, whatever else is muted"),
        Setting("Rest day", "1 per week unused", "Waives threshold, keeps the streak"),
        Setting("Scroll apps", "4 selected", "Instagram · Reddit · X · Shorts"),
        Setting("Detect new apps", "", "Checks social, browser, VPN at every start", switch = true),
        Setting("Widgets", "6 active", "Home screen and lock screen"),
        Setting("Language", "English", "System default"),
    )
    Tab.SOUND -> listOf(
        Setting("Sound", "", "Master switch", switch = true),
        Setting("Sound pack", "Deep", "Deep · Crystalline · Industrial · Silent"),
        Setting("Rank ascension", "low thud + swell", ""),
        Setting("Threshold cleared", "single chime", ""),
        Setting("Session broken", "dry snap", ""),
        Setting("Shadow extracted", "rising drone", ""),
        Setting("Aura bleeding", "haptic only", "No sound, ever"),
        Setting("Haptics", "Strong", "Thud, tick and click per event"),
        Setting("Fire on silent", "", "Haptics still play when muted", switch = true),
    )
    Tab.AI -> listOf(
        Setting("Gemini key", "••••7Qd", "Yours. Stored in Keystore"),
        Setting("Daily quest generation", "", "Schema-constrained, aura set by formula", switch = true),
        Setting("Weekly post-mortem", "", "Finds the pattern you cannot see", switch = true),
        Setting("Speak to the System", "", "Rate limited daily", switch = true),
        Setting("Mood-aware load", "", "Adjusts today within clamps", switch = true),
        Setting("Standing directives", "3 active", "Instructions the System obeys daily"),
        Setting("Gate naming", "", "Flavour text only, never mechanics", switch = true),
        Setting("Guardrails", "Always on", "No medical, no calories, crisis routing"),
    )
    Tab.PRIVACY -> listOf(
        Setting("Private track lock", "Fingerprint", "Required every time it opens"),
        Setting("Private check-in", "17:00 daily", "Reminder never names what it is for"),
        Setting("Guild visibility", "Rank and streak", "Never your private track"),
        Setting("League name", "Set by you", "Shown to 29 others"),
        Setting("Share screen time", "", "Only aggregate aura leaves the device", switch = false),
        Setting("Usage analytics", "", "Which screens get opened. Never which apps", switch = true),
        Setting("Crash reports", "", "Stack traces only. No screen contents", switch = true),
        Setting("Private track is exempt", "Always", "Emits no event of any kind, on or off"),
    )
    Tab.DATA -> listOf(
        Setting("Storage", "On device", "Room. Your history, app list and private track never leave"),
        Setting("What does leave", "3 things", "Guild identity, anonymous feature counts, crash traces"),
        Setting("Cloud sync", "Guild only", "Identity, friend pairs, live raids"),
        Setting("Waitlist email", "Not given", "Only if you asked to hear about a new gate"),
        Setting("Privacy policy", "gakseong.app/privacy", "Required once anything leaves the device"),
        Setting("Export", "JSON", "Everything, yours to take"),
        Setting("Delete everything", "Irreversible", "The record goes, the app stays"),
    )
}
