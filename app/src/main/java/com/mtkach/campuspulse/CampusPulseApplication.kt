package com.mtkach.campuspulse

import android.app.Application
import com.mtkach.campuspulse.data.AppDatabase
import com.mtkach.campuspulse.data.ChronicleRepository
import com.mtkach.campuspulse.data.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CampusPulseApplication : Application() {
    lateinit var repository: ChronicleRepository
        private set
    lateinit var sessionStore: SessionStore
        private set

    private val appScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = ChronicleRepository(db)
        sessionStore = SessionStore(this)

        appScope.launch {
            repository.ensureSeeded()
        }
    }
}
