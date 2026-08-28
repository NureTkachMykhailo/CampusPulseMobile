package com.mtkach.campuspulse.data

import android.content.Context

data class Session(val userId: Long, val displayName: String, val isSuperuser: Boolean)

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("campuspulse_session", Context.MODE_PRIVATE)

    fun save(session: Session) {
        prefs.edit()
            .putLong("userId", session.userId)
            .putString("displayName", session.displayName)
            .putBoolean("isSuperuser", session.isSuperuser)
            .apply()
    }

    fun load(): Session? {
        val userId = prefs.getLong("userId", -1)
        if (userId < 0) return null
        val name = prefs.getString("displayName", null) ?: return null
        return Session(userId, name, prefs.getBoolean("isSuperuser", false))
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
