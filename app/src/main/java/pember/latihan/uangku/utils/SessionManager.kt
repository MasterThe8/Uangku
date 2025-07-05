package pember.latihan.uangku.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManager private constructor(private val prefs: SharedPreferences) {

    companion object {
        private var prefsProvider: (Context) -> SharedPreferences = { context ->
            context.getSharedPreferences("session_pref", Context.MODE_PRIVATE)
        }

        fun overridePrefsProvider(provider: (Context) -> SharedPreferences) {
            prefsProvider = provider
        }

        fun getInstance(context: Context): SessionManager {
            return SessionManager(prefsProvider(context))
        }

        fun resetProvider() {
            prefsProvider = { context ->
                context.getSharedPreferences("session_pref", Context.MODE_PRIVATE)
            }
        }
    }

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
