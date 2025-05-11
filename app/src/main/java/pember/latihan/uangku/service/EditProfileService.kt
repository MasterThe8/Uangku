package pember.latihan.uangku.service

import android.content.Context
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.utils.SessionManager

object EditProfileService {

    suspend fun updateProfile(
        context: Context,
        newUsername: String,
        newEmail: String,
        newSaldo: Double,
        currentPassword: String?,
        newPassword: String?,
        confirmPassword: String?
    ): Result<Unit> {
        return try {
            val uid = SessionManager(context).getUserId()
                ?: return Result.failure(Exception("User belum login"))

            val db = AppDatabase.getInstance(context)
            val userDao = db.userDao()
            val firestore = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User tidak ditemukan di FirebaseAuth"))

            // ✅ Update Firestore
            val userUpdate = mapOf(
                "username" to newUsername,
                "email" to newEmail,
                "initial_balance" to newSaldo
            )
            firestore.collection("users").document(uid).set(userUpdate, SetOptions.merge()).await()

            // ✅ Update Room
            withContext(Dispatchers.IO) {
                val localUser = userDao.getActiveUser()
                if (localUser != null) {
                    val updated = localUser.copy(
                        username = newUsername,
                        email = newEmail,
                        initialBalance = newSaldo
                    )
                    userDao.insert(updated)
                }
            }

            // ✅ Update Password jika diisi
            if (!newPassword.isNullOrBlank()) {
                if (newPassword != confirmPassword) {
                    return Result.failure(Exception("Password baru dan konfirmasi tidak cocok"))
                }

                // ⛔ Pastikan current password dimasukkan
                if (currentPassword.isNullOrBlank()) {
                    return Result.failure(Exception("Masukkan password saat ini untuk verifikasi"))
                }

                // ✅ Reauthenticate user terlebih dahulu
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential).await()

                // 🔁 Setelah berhasil, update password
                currentUser.updatePassword(newPassword).await()
            }
            // Log Berhasil
            Log.d("EditProfileService", "Profil berhasil diperbarui")
            Result.success(Unit)
        } catch (e: Exception) {
            // Log Gagal
            Log.e("EditProfileService", "Gagal memperbarui profil", e)
            Result.failure(e)
        }
    }
}
