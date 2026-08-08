package app.gakseong.session

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

// The speed bump. §Verifiers: `SYSTEM_ALERT_WINDOW` behind a single user toggle, and **only while a session's
// foreground service is already alive**.
//
// This is the one part of the app that can draw over another app, so the conditions on it are deliberately
// narrow: a toggle the user set, a session that is genuinely running, and nothing else.

/**
 * Whether the overlay may be drawn.
 *
 * Three conditions, all required. The service check is the one that matters: an app that can draw over other
 * apps whenever it likes is a different and much worse thing than one that can do it for forty-five minutes a
 * user started.
 */
fun canBump(context: Context, enabled: Boolean): Boolean =
    enabled &&
        FocusService.state.value?.broken == false &&
        Settings.canDrawOverlays(context)

/** A single user toggle, granted through Settings rather than a runtime dialog. */
fun overlayIntent(context: Context): Intent =
    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))

/**
 * Raise the bump over whatever is in front.
 *
 * Deliberately plain Android views rather than Compose. A `ComposeView` in a window overlay needs its own
 * lifecycle, saved-state and recomposer wired by hand, which is a lot of machinery for three widgets that exist
 * for four seconds. `ui/Kit.kt` is unreachable from here anyway, the same way it is from a Glance widget.
 *
 * ponytail: no dismiss timer and no animation. The user dismisses it or goes back; a bump that fades on its own
 * is a bump that can be waited out.
 */
fun raiseBump(context: Context, remainingMinutes: Int, onProceed: () -> Unit): Boolean {
    if (!Settings.canDrawOverlays(context)) return false
    val wm = context.getSystemService(WindowManager::class.java) ?: return false

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(0xF20B0D1A.toInt())
        setPadding(64, 96, 64, 96)
        gravity = Gravity.CENTER
    }
    root.addView(
        TextView(context).apply {
            text = "The System does not permit this"
            setTextColor(0xFFF2F4FA.toInt())
            textSize = 22f
        },
    )
    root.addView(
        TextView(context).apply {
            text = "$remainingMinutes minutes remain. Proceeding ends your session."
            setTextColor(0xFF8B93A8.toInt())
            textSize = 14f
            setPadding(0, 24, 0, 48)
        },
    )
    root.addView(
        Button(context).apply {
            text = "Return"
            setOnClickListener { runCatching { wm.removeView(root) } }
        },
    )
    root.addView(
        Button(context).apply {
            // The way out is always visible. §Verifiers: this is a speed bump, not containment, and a bump with
            // no exit is a block that never asked permission to be one.
            text = "Proceed · lose the session"
            setOnClickListener {
                runCatching { wm.removeView(root) }
                onProceed()
            }
        },
    )

    val type =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

    return runCatching {
        wm.addView(
            root,
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                // Focusable, because the buttons have to be pressable. Not TOUCH_MODAL: the point is to
                // interrupt, not to trap somebody in a window they cannot leave.
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT,
            ),
        )
        true
    }.getOrDefault(false)
}
