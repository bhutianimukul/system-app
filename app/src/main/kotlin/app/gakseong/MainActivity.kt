package app.gakseong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.gakseong.ui.screens.AriseScreen
import app.gakseong.ui.screens.BreakScreen
import app.gakseong.ui.screens.CeremonyScreen
import app.gakseong.ui.screens.FocusScreen
import app.gakseong.ui.screens.FeedScreen
import app.gakseong.ui.screens.GateScreen
import app.gakseong.ui.screens.GuildScreen
import app.gakseong.ui.screens.HomeScreen
import app.gakseong.ui.screens.InviteScreen
import app.gakseong.ui.screens.LeagueScreen
import app.gakseong.ui.screens.PrivateScreen
import app.gakseong.ui.screens.ProfileScreen
import app.gakseong.ui.screens.ReferScreen
import app.gakseong.ui.screens.ReportScreen
import app.gakseong.ui.screens.AppsScreen
import app.gakseong.ui.screens.ContractScreen
import app.gakseong.ui.screens.PermsScreen
import app.gakseong.ui.screens.RealityScreen
import app.gakseong.ui.screens.SplashScreen
import app.gakseong.ui.screens.AwakeningScreen
import app.gakseong.ui.screens.BaselineScreen
import app.gakseong.ui.screens.IntentScreen
import app.gakseong.ui.screens.StagedScreen
import app.gakseong.ui.screens.NewAppScreen
import app.gakseong.ui.screens.PrivateSetupScreen
import app.gakseong.ui.screens.RateScreen
import app.gakseong.ui.screens.ThresholdScreen
import app.gakseong.ui.screens.WeightsScreen
import app.gakseong.ui.screens.WelcomeScreen
import app.gakseong.ui.screens.SoonScreen
import app.gakseong.ui.screens.RaidHubScreen
import app.gakseong.ui.screens.RaidScreen
import app.gakseong.ui.screens.RunRaidScreen
import app.gakseong.ui.screens.RunSettleScreen
import app.gakseong.ui.screens.ShareScreen
import app.gakseong.ui.theme.GakseongTheme
import app.gakseong.ui.theme.HunterClass

/**
 * Navigation does not exist yet, so which screen to show comes from an intent extra. That is also the hook the
 * screenshot check uses, one screen per launch:
 *
 * `adb shell am start -n app.gakseong/.MainActivity --es screen ceremony --es class ranger`
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The platform splash, taken over rather than replaced. A custom splash Activity on top of this is the
        // single most common way this ships wrong, and it produces a visible double-splash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Art bleeds under the status bar exactly as the design page shows it, so the app draws edge to edge.
        enableEdgeToEdge()

        val screen = intent?.getStringExtra("screen") ?: "home"
        val hunter = intent?.getStringExtra("class")
            ?.let { name -> HunterClass.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
            ?: HunterClass.ASSASSIN

        setContent {
            // Dark is the design's default. The light theme is real rather than an inversion; it follows the
            // system once onboarding owns this choice.
            GakseongTheme(hunterClass = hunter, dark = true) {
                Route(screen)
            }
        }
    }
}

@Composable
private fun Route(screen: String) {
    when (screen) {
        "focus" -> FocusScreen()
        "ceremony" -> CeremonyScreen()
        "raid" -> RaidScreen()
        "raidhub" -> RaidHubScreen()
        "runraid" -> RunRaidScreen()
        "runsettle" -> RunSettleScreen()
        "share" -> ShareScreen()
        "arise" -> AriseScreen()
        "league" -> LeagueScreen()
        "gate" -> GateScreen()
        "break" -> BreakScreen()
        "invite" -> InviteScreen()
        "guild" -> GuildScreen()
        "feed" -> FeedScreen()
        "refer" -> ReferScreen()
        "soon" -> SoonScreen()
        "profile" -> ProfileScreen()
        "private" -> PrivateScreen()
        "report" -> ReportScreen()
        "splash" -> SplashScreen()
        "welcome" -> WelcomeScreen()
        "perms" -> PermsScreen()
        "diag" -> RealityScreen()
        "apps" -> AppsScreen()
        "contract" -> ContractScreen()
        "intent" -> IntentScreen()
        "class" -> AwakeningScreen()
        "assess" -> BaselineScreen()
        "stage" -> StagedScreen()
        "rate" -> RateScreen()
        "privset" -> PrivateSetupScreen()
        "thresh" -> ThresholdScreen()
        "weights" -> WeightsScreen()
        "newapp" -> NewAppScreen()
        else -> HomeScreen()
    }
}
