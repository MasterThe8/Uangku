package pember.latihan.uangku.utils

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.firebase.FirebaseSaving
import pember.latihan.uangku.model.firebase.FirebaseTransaction
import pember.latihan.uangku.model.firebase.FirebaseUser
import pember.latihan.uangku.model.firebase.toRoomSaving
import pember.latihan.uangku.model.firebase.toRoomTransaction
import pember.latihan.uangku.model.firebase.toRoomUser
import pember.latihan.uangku.data.CategorySeed

object FirebaseSyncHelper {

    suspend fun syncFromFirebase(context: Context, uid: String) {
        val dbFs = Firebase.firestore
        val appDb = AppDatabase.getInstance(context)

        // 1) Fetch and insert the User, and get its true Room ID
        val fbUser = dbFs.collection("users").document(uid)
            .get().await()
            .toObject(FirebaseUser::class.java)
            ?: return

        val localUser = fbUser.toRoomUser()
        val localUserId = appDb.userDao().insertAndGetId(localUser).toInt()

        // 2) Sync categories and build a name→ID map
        val categories = CategorySeed.defaultCategories
        categories.forEach { appDb.categoryDao().insert(it) }
        val categoryList = appDb.categoryDao().getAll()
        val categoryMap = categoryList.associateBy { it.name to it.type }

        // 3) Sync savings
        val fbSavings = dbFs.collection("savings")
            .whereEqualTo("userId", uid)
            .get().await()
            .toObjects(FirebaseSaving::class.java)
        appDb.savingDao().deleteAll()
        fbSavings
            .map { it.toRoomSaving(localUserId) }
            .forEach { appDb.savingDao().insert(it) }

        // 4) Sync transactions
        val fbTrans = dbFs.collection("transactions")
            .whereEqualTo("userId", uid)
            .get().await()
            .toObjects(FirebaseTransaction::class.java)

        appDb.transactionDao().deleteAll()
        fbTrans.mapNotNull { ft ->
            val key = ft.categoryName.trim() to ft.type.trim()
            val cat = categoryMap[key]
            if (cat == null) {
                Log.w("FirebaseSync", "Skipping txn with unknown category: $key")
                null
            } else {
                ft.toRoomTransaction(localUserId, cat.id)
            }
        }.forEach { appDb.transactionDao().insert(it) }
    }
}
