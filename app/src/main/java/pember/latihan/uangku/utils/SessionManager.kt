package pember.latihan.uangku.utils

import android.content.Context
import android.util.Log

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session_pref", Context.MODE_PRIVATE)

    fun saveUserId(uid: String) {
        Log.d("SessionManager", "Saving UID: $uid")
        prefs.edit().putString("uid", uid).apply()
    }

    fun getUserId(): String? {
        val uid = prefs.getString("uid", null)
        Log.d("SessionManager", "Retrieved UID: $uid")
        return uid
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
