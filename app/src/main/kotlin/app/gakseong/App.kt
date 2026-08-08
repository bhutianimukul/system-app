package app.gakseong

import android.app.Application
import app.gakseong.data.Repo
import app.gakseong.quest.tick
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
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            // Gathered before the update, because the block DataStore hands back must not suspend: it can be
            // re-run on write contention, and re-running two sensor queries per retry is not free.
            val readings = gather(this@App)
            Repo.update { tick(it, readings, Repo.today()) }
        }
        SettleWorker.schedule(this)
    }
}
