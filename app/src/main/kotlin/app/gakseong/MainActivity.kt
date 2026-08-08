package app.gakseong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.gakseong.data.Repo
import app.gakseong.ui.GakseongNav
import app.gakseong.ui.theme.GakseongTheme
import app.gakseong.ui.theme.HunterClass

/**
 * Navigation lives in `ui/Nav.kt`. The intent extra survives as a debug start-destination override, because
 * `screenshots/index.html` drives one route per launch:
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

        val start = intent?.getStringExtra("screen")
            ?: if (Repo.state.value.onboarded) "home" else "welcome"

        val hunter = intent?.getStringExtra("class")
            ?.let { name -> HunterClass.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
            ?: HunterClass.entries.firstOrNull { it.name == Repo.state.value.profile.hunterClass }
            ?: HunterClass.ASSASSIN

        setContent {
            // Dark is the design's default. The light theme is real rather than an inversion; it follows the
            // system once onboarding owns this choice.
            GakseongTheme(hunterClass = hunter, dark = true) {
                GakseongNav(start)
            }
        }
    }
}
