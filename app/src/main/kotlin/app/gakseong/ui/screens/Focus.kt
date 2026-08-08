package app.gakseong.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.gakseong.R
import app.gakseong.ui.Art
import app.gakseong.ui.Aura
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.Card
import app.gakseong.ui.Cta
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.gakseong.session.FocusService
import app.gakseong.session.Status
import app.gakseong.session.status
import app.gakseong.ui.LocalNav
import app.gakseong.ui.LocalSystem
import kotlinx.coroutines.delay
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.Pill
import app.gakseong.ui.Plate
import app.gakseong.ui.RingTimer
import app.gakseong.ui.rememberHaptics
import app.gakseong.ui.Screen
import app.gakseong.ui.ShadeRadial
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/** `data-s="focus"`. A session bounded by its own length, with the speed bump shown in place. */
@Composable
fun FocusScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val haptics = rememberHaptics()
    val context = LocalContext.current
    val nav = LocalNav.current
    val sys = LocalSystem.current
    var bumped by remember { mutableStateOf(false) }

    val session by FocusService.state.collectAsStateWithLifecycle()
    // The length comes from whichever focus quest the day drew, not from a literal.
    val minutes = sys.today.quests.firstOrNull { it.id.startsWith("focus-") }
        ?.let { Regex("""(\d+) min""").find(it.title)?.groupValues?.get(1)?.toIntOrNull() }
        ?: 45

    // A one-second tick so the ring moves. Cheap, and only while this screen is on top.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(session) {
        while (session != null) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    // Opening this screen with nothing running starts the session. §22 asks for DND access here, at the first
    // session, never at onboarding: an app that asks to silence your phone before you have used it once is one
    // most people decline.
    LaunchedEffect(Unit) { if (session == null) FocusService.start(context, minutes) }

    val live = session?.let { status(it, now) }
    val target = minutes * 60_000L
    val remaining = when (live) {
        is Status.Running -> live.remainingMs
        is Status.Grace -> live.remainingMs
        is Status.Complete -> 0L
        else -> target
    }
    val progress = ((target - remaining).toFloat() / target).coerceIn(0f, 1f)
    val forbidden = sys.profile.watchedPackages

    // A session is bounded by its own length. Leaving is what ends it, so back cannot be a silent exit: it
    // raises the speed bump the screen already carries, and only Proceed actually gives the session up.
    BackHandler(enabled = true) {
        if (!bumped) {
            bumped = true
            haptics.thud()
        }
    }

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.90f, heightFraction = 0.30f, top = 0.24f, left = 0.05f, alpha = 0.42f)
        Art(R.drawable.sc_flame, top = -0.04f, left = -0.04f, width = 1.06f, alpha = 0.9f)
        ShadeRadial(0.5f, 0.34f, 0.62f, 0f to 0.42f, 0.62f to 0.82f, 1f to 0.95f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Focus Active")
                Filler()
                RingTimer(progress = progress, time = clock(remaining), of = "of ${clock(target)}")
                Gap(20.8)
                Tag(
                    when (live) {
                        is Status.Grace -> "Return within ${live.graceLeftMs / 1000}s"
                        Status.Broken -> "The session broke"
                        is Status.Complete -> "Held"
                        else -> "Forbidden for the next ${remaining / 60_000} minutes"
                    },
                )
                Gap(9.6)
                Row(horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
                    if (forbidden.isEmpty()) Pill("Nothing chosen yet")
                    else forbidden.take(3).forEach { Pill(it) }
                }

                Gap(12.8)
                // Flex children stretch to the tallest sibling; Compose needs to be told.
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(m.d(5.4)),
                ) {
                    Card(Modifier.weight(1f).fillMaxHeight()) {
                        Tag("◐ Do Not Disturb", t.tag.copy(color = p.hot))
                        Gap(2.2)
                        Tag("calls still ring", t.tag.copy(color = p.faint))
                    }
                    Card(Modifier.weight(1f).fillMaxHeight()) {
                        Tag("⏸ Screen-off credit", t.tag.copy(color = p.hot))
                        Gap(2.2)
                        Tag("paused · ${minutes} min", t.tag.copy(color = p.faint))
                    }
                }

                Gap(8)
                Plate(Modifier.width(m.d(250))) {
                    Text(
                        "The System is watching.",
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center, color = p.ink),
                    )
                    Text(
                        "Opening any of these ends the session.",
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center, color = p.ink),
                    )
                    Text(
                        "Screen off is fine — the timer keeps running. Phone-down just stops collecting, so the " +
                            "same minutes are never paid twice.",
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center),
                    )
                }

                Filler()
                if (bumped) Card(Modifier.fillMaxWidth(), big = true, lit = true) {
                    Tag("⚠ Speed bump", t.tag.copy(color = p.hot))
                    Gap(6.4)
                    Text("The System does not permit this", style = t.md)
                    Gap(3.2)
                    Text(
                        "${remaining / 60_000} minutes remain. Proceeding ends your session.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                    Gap(13.6)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                        Cta("Return", modifier = Modifier.weight(1f), onClick = { bumped = false })
                        Cta(
                            "Proceed · lose the session",
                            ghost = true,
                            modifier = Modifier.weight(1f),
                            // Only this gives it up. Back raises the bump; nothing else ends a session early.
                            onClick = {
                                FocusService.stop(context, broken = true)
                                bumped = false
                                nav("home")
                            },
                        )
                    }
                }
                Gap(17.6)
            }
        }
    }
}

/** `31:12`. Minutes and seconds, never hours: no session in this app is that long. */
private fun clock(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(total / 60, total % 60)
}
