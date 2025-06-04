package pember.latihan.uangku.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.Saving
import pember.latihan.uangku.model.dao.SavingDao
import pember.latihan.uangku.model.dao.UserDao
import pember.latihan.uangku.model.firebase.FirebaseSaving
import pember.latihan.uangku.model.firebase.toRoomSaving
import pember.latihan.uangku.utils.SessionManager
import pember.latihan.uangku.utils.fromRoomSaving

class SavingService(private val context: Context) {

    companion object {
        private var daoProviderOverride: (() -> Pair<SavingDao, UserDao>)? = null

        fun overrideDaoProvider(provider: () -> Pair<SavingDao, UserDao>) {
            daoProviderOverride = provider
        }

        fun resetDaoProvider() {
            daoProviderOverride = null
        }
    }

    private val savingDao: SavingDao
    private val userDao: UserDao
    private val firebaseDb = FirebaseDatabase.getInstance().reference

    init {
        val (sDao, uDao) = daoProviderOverride?.invoke()
            ?: AppDatabase.getInstance(context).let {
                Pair(it.savingDao(), it.userDao())
            }

        savingDao = sDao
        userDao = uDao
    }

    suspend fun insertSaving(saving: Saving) {
        withContext(Dispatchers.IO) {
            val user = userDao.getActiveUser() ?: return@withContext
            val savingWithUser = saving.copy(userId = user.id)
            savingDao.insert(savingWithUser)
            syncSavingToFirebase(savingWithUser, user.firebaseUid)
        }
    }

    suspend fun getSavings(): List<Saving> {
        return withContext(Dispatchers.IO) {
            val user = userDao.getActiveUser() ?: return@withContext emptyList()
            savingDao.getByUser(user.id)
        }
    }

    suspend fun updateCurrentAmountByTitle(title: String, newAmount: Double) {
        withContext(Dispatchers.IO) {
            savingDao.updateCurrentAmountByTitle(title, newAmount)

            val user = userDao.getActiveUser()
            if (user != null) {
                val updatedSaving = savingDao.getSavingByTitle(title)
                if (updatedSaving != null) {
                    syncSavingToFirebase(updatedSaving, user.firebaseUid)
                }
            }
        }
    }

    private fun syncSavingToFirebase(saving: Saving, userUid: String) {
        val firebaseSaving = saving.fromRoomSaving(userUid)
        firebaseDb.child("savings")
            .child(userUid)
            .push()
            .setValue(firebaseSaving)
            .addOnSuccessListener {
                Log.d("SavingService", "Saving synced to Firebase")
            }
            .addOnFailureListener {
                Log.e("SavingService", "Failed to sync saving to Firebase", it)
            }
    }

    suspend fun getCurrentUserId(): Int {
        val firebaseUid = SessionManager.getInstance(context).getUserId()
            ?: throw IllegalStateException("User belum login")
        val user = userDao.getByFirebaseUid(firebaseUid)
            ?: throw IllegalStateException("User lokal tidak ditemukan")
        return user.id
    }
}


