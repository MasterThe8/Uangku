package pember.latihan.uangku.utils

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.Category
import pember.latihan.uangku.model.firebase.FirebaseSaving
import pember.latihan.uangku.model.firebase.FirebaseTransaction
import pember.latihan.uangku.model.firebase.FirebaseUser
import pember.latihan.uangku.model.firebase.toRoomSaving
import pember.latihan.uangku.model.firebase.toRoomTransaction
import pember.latihan.uangku.model.firebase.toRoomUser

object FirebaseSyncHelper {

    suspend fun syncFromFirebase(context: Context, uid: String) {
        val db = FirebaseFirestore.getInstance()
        val appDb = AppDatabase.getInstance(context)

        try {
            val doc = db.collection("users").document(uid).get().await()
            val firebaseUser = doc.toObject(FirebaseUser::class.java)
            if (firebaseUser != null) {
                Log.d("FirebaseSync", "🧩 Deleting existing local user data...")
                appDb.userDao().deleteAll()

                Log.d("FirebaseSync", "🧩 Inserting user...")
                val userId = appDb.userDao().insertAndGetId(firebaseUser.toRoomUser()).toInt()
                Log.d("FirebaseSync", "✅ User inserted with ID = $userId")

                val syncedCategories = CategorySyncHelper.syncCategories(context)
                val categoryMap = syncedCategories.associateBy { it.name to it.type }
                Log.d("FirebaseSync", "✅ Categories synced: ${syncedCategories.size}")

                syncSavings(db, appDb, uid, userId)
                syncTransactions(db, appDb, uid, userId, categoryMap)
            } else {
                Log.e("FirebaseSync", "❌ FirebaseUser is null")
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "❌ Sync failed: ${e.message}", e)
        }
    }

    fun syncToFirebase(context: Context, uid: String, onComplete: () -> Unit = {}) {
        val db = FirebaseFirestore.getInstance()
        val appDb = AppDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = appDb.userDao().getActiveUser()
                val savings = appDb.savingDao().getAll()
                val transactions = appDb.transactionDao().getAllWithCategory()

                user?.let {
                    val firebaseUser = it.fromRoomUser(uid)
                    db.collection("users").document(uid).set(firebaseUser).await()
                    Log.d("FirebaseSync", "⬆ User synced.")
                }

                val savingsRef = db.collection("savings")
                savingsRef.whereEqualTo("userId", uid).get().await().documents.forEach { it.reference.delete() }
                savings.forEach {
                    savingsRef.add(it.fromRoomSaving(uid))
                }
                Log.d("FirebaseSync", "⬆ Savings synced: ${savings.size}")

                val trxRef = db.collection("transactions")
                trxRef.whereEqualTo("userId", uid).get().await().documents.forEach { it.reference.delete() }
                transactions.forEach {
                    val trx = it.transaction.fromRoomTransaction(it.category.name, it.category.type, uid)
                    trxRef.add(trx)
                }
                Log.d("FirebaseSync", "⬆ Transactions synced: ${transactions.size}")

                withContext(Dispatchers.Main) {
                    onComplete()
                }

            } catch (e: Exception) {
                Log.e("FirebaseSync", "❌ Failed to sync to Firebase: ${e.message}", e)
            }
        }
    }

    private suspend fun syncSavings(
        db: FirebaseFirestore,
        appDb: AppDatabase,
        uid: String,
        userId: Int
    ) {
        val savingsSnapshot = db.collection("savings").whereEqualTo("userId", uid).get().await()
        val list = savingsSnapshot.toObjects(FirebaseSaving::class.java).map {
            it.toRoomSaving(userId)
        }

        appDb.savingDao().deleteAll()
        Log.d("FirebaseSync", "✅ Inserting ${list.size} savings...")
        list.forEach { appDb.savingDao().insert(it) }
    }

    private suspend fun syncTransactions(
        db: FirebaseFirestore,
        appDb: AppDatabase,
        uid: String,
        userId: Int,
        categoryMap: Map<Pair<String, String>, Category>
    ) {
        val transSnapshot = db.collection("transactions").whereEqualTo("userId", uid).get().await()
        val list = transSnapshot.toObjects(FirebaseTransaction::class.java)

        appDb.transactionDao().deleteAll()
        Log.d("FirebaseSync", "✅ ${list.size} transactions fetched.")

        val mapped = list.mapNotNull {
            val categoryName = it.categoryName?.trim()
            val categoryType = it.type?.trim()
            val matchedCategory = categoryMap[categoryName to categoryType]
            if (matchedCategory == null) {
                Log.w("FirebaseSync", "⚠ Skipping transaction: $categoryName ($categoryType) not found.")
                return@mapNotNull null
            }

            val trx = it.toRoomTransaction(userId, matchedCategory.id)
            Log.d("FirebaseSync", "➡ Transaction: ${trx.description}, categoryId=${trx.categoryId}, userId=${trx.userId}")
            trx
        }

        mapped.forEach { appDb.transactionDao().insert(it) }
        Log.d("FirebaseSync", "✅ Transactions inserted: ${mapped.size}")
    }
}
