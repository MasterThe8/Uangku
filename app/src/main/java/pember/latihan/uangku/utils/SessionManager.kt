package pember.latihan.uangku.utils

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session_pref", Context.MODE_PRIVATE)

    fun saveUserId(uid: String) {
        prefs.edit().putString("uid", uid).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("uid", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
