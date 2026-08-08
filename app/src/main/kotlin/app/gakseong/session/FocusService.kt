package app.gakseong.session

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import app.gakseong.MainActivity
import app.gakseong.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The focus session, as a foreground service.
 *
 * This is the one foreground service in the app and it is bounded by the session. §2's ban on a persistent
 * watcher is about *tracking*: a service that lives for forty-five minutes with a visible notification is a
 * different thing from one that runs forever to count screen time.
 */
class FocusService : Service() {

    private var loop: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { finish(broken = false); return START_NOT_STICKY }
            ACTION_ABANDON -> { finish(broken = true); return START_NOT_STICKY }
        }

        val minutes = intent?.getIntExtra(EXTRA_MINUTES, 45) ?: 45
        val now = System.currentTimeMillis()
        val endAt = now + minutes * 60_000L
        _state.value = FocusState(startedAtMs = now, lengthMinutes = minutes)

        // §22: fully optional and off by default. A refused grant degrades to a normal session rather than
        // blocking it, so nothing branches on the result.
        enableDnd(this, endAt)

        startForeground(NOTIFICATION_ID, notification(minutes * 60_000L))
        loop = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(1_000)
                val s = _state.value ?: break
                when (val status = status(s, System.currentTimeMillis())) {
                    is Status.Complete -> { finish(broken = false); break }
                    Status.Broken -> { finish(broken = true); break }
                    is Status.Running -> notify(notification(status.remainingMs))
                    is Status.Grace -> notify(notification(status.remainingMs, graceMs = status.graceLeftMs))
                }
            }
        }
        return START_NOT_STICKY
    }

    /** The user left for something that is not the dialer. Called by the Activity, which knows it lost focus. */
    private fun away(packageName: String?) {
        _state.value = _state.value?.let { step(it, System.currentTimeMillis(), packageName) }
    }

    private fun finish(broken: Boolean) {
        loop?.cancel()
        _state.value = _state.value?.copy(broken = broken)
        // Hand the phone back before anything else, unconditionally.
        //
        // This used to be gated on a `dndOn` flag held in a companion object. That flag resets when the process
        // dies, so after a crash the app forgot it had silenced the phone and never turned it back. Verified on
        // device: force-stop mid-session left zen_mode=1 with the rule still STATE_TRUE. disableDnd is safe to
        // call when DND was never on, so there is nothing to remember.
        disableDnd(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        if (!broken) _state.value = null
    }

    override fun onDestroy() {
        loop?.cancel()
        disableDnd(this)
        super.onDestroy()
    }

    private fun notify(n: Notification) =
        getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, n)

    private fun notification(remainingMs: Long, graceMs: Long? = null): Notification {
        val minutes = (remainingMs / 60_000).coerceAtLeast(0)
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).putExtra("screen", "focus"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_app)
            .setContentTitle(
                if (graceMs != null) "Return within ${graceMs / 1000}s" else "Focus session",
            )
            .setContentText(
                if (graceMs != null) "The session is still yours" else "$minutes minutes remain",
            )
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(
                Notification.Action.Builder(
                    null, "End session",
                    PendingIntent.getService(
                        this, 1, Intent(this, FocusService::class.java).setAction(ACTION_ABANDON),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    ),
                ).build(),
            )
            .build()
    }

    companion object {
        const val ACTION_STOP = "stop"
        const val ACTION_ABANDON = "abandon"
        const val EXTRA_MINUTES = "minutes"
        private const val CHANNEL = "focus"
        private const val NOTIFICATION_ID = 1

        private val _state = MutableStateFlow<FocusState?>(null)

        /** Null when no session is running. The Focus screen and the `FocusSession` verifier both read this. */
        val state: StateFlow<FocusState?> = _state.asStateFlow()

        /**
         * Clear a rule left behind by a process that died mid-session.
         *
         * A fresh process with no session running has no business leaving DND on, and the user has no way to
         * know what silenced their phone. Called from `App.onCreate`, which is the first moment the app can
         * possibly notice.
         */
        fun recoverFromCrash(context: Context) {
            if (_state.value == null) disableDnd(context)
        }

        fun start(context: Context, minutes: Int) {
            channel(context)
            val intent = Intent(context, FocusService::class.java).putExtra(EXTRA_MINUTES, minutes)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun stop(context: Context, broken: Boolean) {
            context.startService(
                Intent(context, FocusService::class.java)
                    .setAction(if (broken) ACTION_ABANDON else ACTION_STOP),
            )
        }

        /** Called when the Activity loses focus, so the service does not have to poll UsageStats to notice. */
        fun leftApp(packageName: String?) {
            _state.value = _state.value?.let { step(it, System.currentTimeMillis(), packageName) }
        }

        fun returned() {
            _state.value = _state.value?.let { step(it, System.currentTimeMillis(), null) }
        }

        private fun channel(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "Focus session", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Shows how long is left in a session"
                    setShowBadge(false)
                },
            )
        }
    }
}
