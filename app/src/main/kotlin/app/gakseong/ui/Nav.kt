package app.gakseong.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.gakseong.data.Repo
import app.gakseong.ui.screens.AiGateScreen
import app.gakseong.ui.screens.AppsScreen
import app.gakseong.ui.screens.AriseScreen
import app.gakseong.ui.screens.AwakeningScreen
import app.gakseong.ui.screens.BonusScreen
import app.gakseong.ui.screens.BreakScreen
import app.gakseong.ui.screens.CeremonyScreen
import app.gakseong.ui.screens.ChatScreen
import app.gakseong.ui.screens.CompleteScreen
import app.gakseong.ui.screens.ContainScreen
import app.gakseong.ui.screens.ContractScreen
import app.gakseong.ui.screens.FeedScreen
import app.gakseong.ui.screens.FocusScreen
import app.gakseong.ui.screens.GateScreen
import app.gakseong.ui.screens.GatesScreen
import app.gakseong.ui.screens.GuildScreen
import app.gakseong.ui.screens.HomeScreen
import app.gakseong.ui.screens.IntentScreen
import app.gakseong.ui.screens.InviteScreen
import app.gakseong.ui.screens.LeagueScreen
import app.gakseong.ui.screens.MonarchScreen
import app.gakseong.ui.screens.NewAppScreen
import app.gakseong.ui.screens.PactScreen
import app.gakseong.ui.screens.PermsScreen
import app.gakseong.ui.screens.PrivateScreen
import app.gakseong.ui.screens.PrivateSetupScreen
import app.gakseong.ui.screens.ProfileScreen
import app.gakseong.ui.screens.RaidHubScreen
import app.gakseong.ui.screens.RaidScreen
import app.gakseong.ui.screens.ReaderScreen
import app.gakseong.ui.screens.RealityScreen
import app.gakseong.ui.screens.ReferScreen
import app.gakseong.ui.screens.ReportScreen
import app.gakseong.ui.screens.RunRaidScreen
import app.gakseong.ui.screens.RunSettleScreen
import app.gakseong.ui.screens.SettingsScreen
import app.gakseong.ui.screens.ShadowsScreen
import app.gakseong.ui.screens.ShareMoment
import app.gakseong.ui.screens.ShareScreen
import app.gakseong.ui.screens.SoonScreen
import app.gakseong.ui.screens.SplashScreen
import app.gakseong.ui.screens.StagedScreen
import app.gakseong.ui.screens.StoreScreen
import app.gakseong.ui.screens.ThresholdScreen
import app.gakseong.ui.screens.TypeSpecimenScreen
import app.gakseong.ui.screens.WeightsScreen
import app.gakseong.ui.screens.WelcomeScreen
import app.gakseong.ui.screens.WidgetScreen

/** Navigate by route name from anywhere, without threading a NavController through forty-eight signatures. */
val LocalNav = compositionLocalOf<(String) -> Unit> { {} }

/** The five bottom-nav destinations, in bar order. Index matches `BottomNav(active = ...)`. */
val NAV_TABS = listOf("home", "gates", "raidhub", "guild", "profile")

/**
 * Every route the app has. The intent extra in [app.gakseong.MainActivity] picks the start destination out of
 * this same table, so `adb shell am start ... --es screen <name>` keeps working for the screenshot sheet.
 */
@Composable
fun GakseongNav(start: String) {
    val nav = rememberNavController()
    val state by Repo.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val animate = remember { animationsEnabled(context) }
    val haptics = rememberHaptics()
    val entry by nav.currentBackStackEntryAsState()
    LaunchedEffect(entry?.destination?.route) { haptics.tick() }

    CompositionLocalProvider(
        LocalSystem provides state,
        LocalNav provides { route -> nav.navigate(route) { launchSingleTop = true } },
    ) {
        NavHost(
            navController = nav,
            startDestination = start,
            // The design page's own transition: `.4s cubic-bezier(.16,1,.3,1)` with an 8px rise. Reduced motion
            // collapses every duration to zero rather than branching, so there is one path to keep correct.
            enterTransition = {
                fadeIn(tween(if (animate) PG_IN_MS else 0, easing = PgIn)) +
                    slideInVertically(tween(if (animate) PG_IN_MS else 0, easing = PgIn)) { it / 40 }
            },
            exitTransition = { fadeOut(tween(if (animate) PG_IN_MS / 2 else 0)) },
            popEnterTransition = { fadeIn(tween(if (animate) PG_IN_MS else 0, easing = PgIn)) },
            popExitTransition = { fadeOut(tween(if (animate) PG_IN_MS / 2 else 0)) },
        ) {
            composable("home") { HomeScreen() }
            composable("focus") { FocusScreen() }
            composable("ceremony") { CeremonyScreen() }
            composable("raid") { RaidScreen() }
            composable("raidhub") { RaidHubScreen() }
            composable("runraid") { RunRaidScreen() }
            composable("runsettle") { RunSettleScreen() }
            composable("share") { ShareScreen() }
            composable("shareraid") { ShareScreen(ShareMoment.RAID) }
            composable("arise") { AriseScreen() }
            composable("league") { LeagueScreen() }
            composable("gate") { GateScreen() }
            composable("break") { BreakScreen() }
            composable("invite") { InviteScreen() }
            composable("guild") { GuildScreen() }
            composable("feed") { FeedScreen() }
            composable("refer") { ReferScreen() }
            composable("soon") { SoonScreen() }
            composable("profile") { ProfileScreen() }
            composable("private") { PrivateScreen() }
            composable("report") { ReportScreen() }
            composable("splash") { SplashScreen() }
            composable("welcome") { WelcomeScreen() }
            composable("perms") { PermsScreen() }
            composable("diag") { RealityScreen() }
            composable("apps") { AppsScreen() }
            composable("contract") { ContractScreen() }
            composable("intent") { IntentScreen() }
            composable("class") { AwakeningScreen() }
            composable("stage") { StagedScreen() }
            composable("privset") { PrivateSetupScreen() }
            composable("thresh") { ThresholdScreen() }
            composable("weights") { WeightsScreen() }
            composable("newapp") { NewAppScreen() }
            composable("read") { ReaderScreen() }
            composable("shadows") { ShadowsScreen() }
            composable("complete") { CompleteScreen() }
            composable("monarch") { MonarchScreen() }
            composable("gates") { GatesScreen() }
            composable("aikey") { AiGateScreen() }
            composable("bonus") { BonusScreen() }
            composable("widget") { WidgetScreen() }
            composable("pact") { PactScreen() }
            composable("contain") { ContainScreen() }
            composable("chat") { ChatScreen() }
            composable("store") { StoreScreen() }
            composable("type") { TypeSpecimenScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
