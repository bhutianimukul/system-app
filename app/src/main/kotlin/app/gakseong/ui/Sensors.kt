package app.gakseong.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import app.gakseong.sense.UsageReading
import app.gakseong.sense.readUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Read the last [days] of usage off the device, off the main thread.
 *
 * `null` while the read is in flight, so a screen can show its own waiting state rather than a zero that reads
 * as a clean month. An ungranted read returns [UsageReading.Unavailable], which is a third state again.
 */
@Composable
fun rememberUsage(days: Int): State<UsageReading?> {
    val context = LocalContext.current
    return produceState<UsageReading?>(initialValue = null, days) {
        value = withContext(Dispatchers.IO) {
            val end = System.currentTimeMillis()
            readUsage(context, end - TimeUnit.DAYS.toMillis(days.toLong()), end)
        }
    }
}

/**
 * A package's human label, falling back to the last segment of the package name.
 *
 * Resolved on device and never sent anywhere. §10: a package name from the user's app list is one of the things
 * that must never leave the phone, and that rule is about the wire, not about the screen in front of them.
 */
fun appLabel(context: Context, packageName: String): String = runCatching {
    val pm = context.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString()
}.getOrElse { packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() } }
