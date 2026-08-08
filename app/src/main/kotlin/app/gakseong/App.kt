package app.gakseong

import android.app.Application
import app.gakseong.data.Repo
import app.gakseong.quest.tick
import app.gakseong.cloud.Event
import app.gakseong.cloud.attribute
import app.gakseong.cloud.installAppCheck
import app.gakseong.cloud.log
import app.gakseong.cloud.signIn
import app.gakseong.session.FocusService
import app.gakseong.work.SettleWorker
import app.gakseong.work.gather
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repo.init(this)

        // A process that starts with no session running must not leave DND on. §22: the whole reason
        // this is an AutomaticZenRule is that a crash must never strand somebody in silence.
        FocusService.recoverFromCrash(this)

        // §2: settle retroactively on app open, and let the fifteen-minute job cover everything between opens.
        // Neither blocks the first frame; a splash that waits on a UsageStats query is a retention cost.
        // §Referral: only genuine app instances may write. Installed before anything signs in.
        installAppCheck(this)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            // No account, no email, no phone. A refused sign-in leaves the app fully playable: the whole
            // ladder is local, and the cloud only carries guild identity.
            val uid = signIn(this@App)
            if (uid != null) Repo.update { it.copy(uid = uid) }

            // §Referral: Play hands over the referrer once, on first launch. Attempted after sign-in because
            // the pairing is keyed by this install's own UID, and once ever because the rule allows create only.
            val state = Repo.state.value
            if (uid != null && !state.attributionAttempted) {
                val code = attribute(this@App, uid, Repo.today(), state.referredBy.isNotBlank())
                Repo.update { it.copy(attributionAttempted = true, referredBy = code ?: it.referredBy) }
            }
            log(this@App, Event.APP_OPENED)

            // Gathered before the update, because the block DataStore hands back must not suspend: it can be
            // re-run on write contention, and re-running two sensor queries per retry is not free.
            val readings = gather(this@App)
            Repo.remember(readings)
            Repo.update { tick(it, readings, Repo.today(), Repo.endOfDayMs()) }
            // A pasted key takes effect on the next open rather than at the next worker run.
            SettleWorker.runNow(this@App)
        }
        SettleWorker.schedule(this)
    }
}
