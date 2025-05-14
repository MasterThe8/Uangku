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
            withContext(Dispatchers.IO) {
                FirebaseSyncHelper.syncToFirebase(context, uid)
            }

            val userUpdate = mapOf(
                "username" to newUsername,
                "email" to newEmail,
                "initial_balance" to newSaldo
            )
            firestore.collection("users").document(uid).set(userUpdate, SetOptions.merge()).await()

            withContext(Dispatchers.IO) {
                userDao.getActiveUser()?.let { localUser ->
                    val updatedUser = localUser.copy(
                        username = newUsername,
                        email = newEmail,
                        initialBalance = newSaldo
                    )
                    userDao.insert(updatedUser)
                }
            }

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
                }

                if (passwordChanged) {
                    if (newPassword != confirmPassword) {
                        return Result.failure(Exception("Password baru dan konfirmasi tidak cocok"))
                    }
                    currentUser.updatePassword(requireNotNull(newPassword)).await()
                }

                SessionManager.getInstance(context).clearSession()
                FirebaseAuth.getInstance().signOut()

                withContext(Dispatchers.IO) {
                    FirebaseSyncHelper.syncFromFirebase(context, uid)
                }

                return Result.failure(Exception("Perubahan email atau password berhasil.\nSilakan verifikasi email baru jika diminta, dan login kembali."))
            }

            withContext(Dispatchers.IO) {
                FirebaseSyncHelper.syncFromFirebase(context, uid)
            }

            return Result.success(Unit)

        } catch (e: Exception) {
            Log.e("EditProfileService", "Gagal memperbarui profil", e)
            return Result.failure(e)
        }
    }
}
