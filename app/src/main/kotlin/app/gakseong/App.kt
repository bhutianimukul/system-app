package app.gakseong

import android.app.Application
import app.gakseong.data.Repo

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repo.init(this)
    }
}
