package pember.latihan.uangku.service

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.User
import pember.latihan.uangku.model.firebase.FirebaseUser
import pember.latihan.uangku.model.firebase.toRoomUser
import pember.latihan.uangku.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.latihan.uangku.utils.FirebaseSyncHelper

object LoginService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val userDao = { context: Context -> AppDatabase.getInstance(context).userDao() }

    fun loginLocal(
        context: Context,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i("LoginService", "Searching user with email: $email")
                val dao = userDao(context)

                // Cari user berdasarkan UID dulu jika ada
                val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                val user = if (firebaseUid != null) {
                    dao.getByFirebaseUid(firebaseUid)
                } else {
                    dao.getUserByEmail(email)
                }

                if (user != null && user.passwordHash == password) {
                    Log.i("LoginService", "User found: ${user.email}")
                    withContext(Dispatchers.Main) {
                        onSuccess(user)
                    }
                } else {
                    onError("User tidak ditemukan atau password salah")
                }
            } catch (e: Exception) {
                Log.e("LoginService", "Error: ${e.message}", e)
                withContext(Dispatchers.Main) { onError("Gagal login lokal: ${e.message}") }
            }
        }
    }
}

