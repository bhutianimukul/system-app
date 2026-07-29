package app.gakseong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.gakseong.ui.screens.HomeScreen
import app.gakseong.ui.theme.GakseongTheme
import app.gakseong.ui.theme.HunterClass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The platform splash, taken over rather than replaced. A custom splash Activity on top of this is the
        // single most common way this ships wrong, and it produces a visible double-splash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Art bleeds under the status bar exactly as the design page shows it, so the app draws edge to edge.
        enableEdgeToEdge()
        setContent {
            // Class comes from onboarding later. Dark is the design's default; the light theme is real, not an
            // inversion, and switches with the system.
            GakseongTheme(hunterClass = HunterClass.ASSASSIN, dark = true) {
                HomeScreen()
            }
        }
    }
}
