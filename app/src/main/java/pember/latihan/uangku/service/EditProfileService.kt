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
import pember.latihan.uangku.utils.FirebaseSyncHelper
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
        val uid = SessionManager.getInstance(context).getUserId()
            ?: return Result.failure(Exception("User belum login"))

        val db = AppDatabase.getInstance(context)
        val userDao = db.userDao()
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
            ?: return Result.failure(Exception("User tidak ditemukan di FirebaseAuth"))

        return try {
            val emailChanged = currentUser.email != newEmail
            val passwordChanged = !newPassword.isNullOrBlank()

            if (emailChanged || passwordChanged) {
                if (currentPassword.isNullOrBlank()) {
                    return Result.failure(Exception("Masukkan password saat ini untuk verifikasi perubahan email/password"))
                }

                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential).await()

                if (emailChanged) {
                    currentUser.verifyBeforeUpdateEmail(newEmail).await()
                    Log.d("EditProfileService", "✅ verifyBeforeUpdateEmail berhasil ke $newEmail")

                    return Result.failure(
                        Exception("Verifikasi dikirim ke $newEmail. Silakan klik link di email tersebut untuk menyelesaikan perubahan.")
                    )
                }

                if (passwordChanged) {
                    if (newPassword != confirmPassword) {
                        return Result.failure(Exception("Password baru dan konfirmasi tidak cocok"))
                    }
                    currentUser.updatePassword(requireNotNull(newPassword)).await()
                }
            }

            val finalEmail = currentUser.email!!

            val userUpdate = mapOf(
                "username" to newUsername,
                "email" to finalEmail,
                "initial_balance" to newSaldo
            )
            firestore.collection("users").document(uid).set(userUpdate, SetOptions.merge()).await()

            withContext(Dispatchers.IO) {
                userDao.getActiveUser()?.let { localUser ->
                    val updatedUser = localUser.copy(
                        username = newUsername,
                        email = finalEmail,
                        initialBalance = newSaldo
                    )
                    userDao.insert(updatedUser)
                }
            }

            withContext(Dispatchers.IO) {
                FirebaseSyncHelper.syncFromFirebase(context, uid)
            }

            if (emailChanged || passwordChanged) {
                SessionManager.getInstance(context).clearSession()
                auth.signOut()

                return Result.failure(Exception("Perubahan email/password berhasil. Silakan login kembali."))
            }

            return Result.success(Unit)

        } catch (e: Exception) {
            Log.e("EditProfileService", "Gagal memperbarui profil", e)
            return Result.failure(e)
        }
    }
}