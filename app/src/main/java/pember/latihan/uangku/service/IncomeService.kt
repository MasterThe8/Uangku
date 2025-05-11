package pember.latihan.uangku.service

import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.Category
import pember.latihan.uangku.model.Transaction
import pember.latihan.uangku.utils.SessionManager

class IncomeService(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val firebaseDb = FirebaseDatabase.getInstance().reference

    suspend fun getIncomeCategories(): List<Category> {
        return withContext(Dispatchers.IO) {
            categoryDao.getCategoriesByType("income")
        }
    }

    suspend fun insertIncome(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.insert(transaction) // Simpan ke Room
        }

        syncIncomeToFirebase(transaction)
    }

    private fun syncIncomeToFirebase(transaction: Transaction) {
        val userId = transaction.userId.toString()

        val incomeMap = mapOf(
            "category_id" to transaction.categoryId,
            "description" to transaction.description,
            "date" to transaction.date.time, // simpan sebagai timestamp
            "amount" to transaction.amount
        )

        firebaseDb.child("transactions")
            .child(userId)
            .push()
            .setValue(incomeMap)
            .addOnSuccessListener {
                Log.d("IncomeService", "Income synced to Firebase")
            }
            .addOnFailureListener {
                Log.e("IncomeService", "Failed to sync income to Firebase", it)
            }
    }

    suspend fun logAllUsersAndCategories() {
        withContext(Dispatchers.IO) {
            val users = db.userDao().getAll()
            val categories = db.categoryDao().getAll()

            Log.d("RoomDebug", "Users in DB:")
            users.forEach {
                Log.d("RoomDebug", "User ID: ${it.id}, Username: ${it.username}, Email: ${it.email}")
            }

            Log.d("RoomDebug", "Categories in DB:")
            categories.forEach {
                Log.d("RoomDebug", "Category ID: ${it.id}, Name: ${it.name}, Type: ${it.type}")
            }
        }
    }

    fun getCurrentUserId(): Int {
        val sessionManager = SessionManager(context)
        val userId = sessionManager.getUserId()
        return userId?.toIntOrNull() ?: throw IllegalStateException("User belum login")
    }
}
