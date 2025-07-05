package pember.latihan.uangku.service
import android.content.Context
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.dao.CategoryDao
import pember.latihan.uangku.model.dao.TransactionDao
import pember.latihan.uangku.model.dao.UserDao
import pember.latihan.uangku.model.Category
import pember.latihan.uangku.model.Transaction
import pember.latihan.uangku.utils.SessionManager

class ExpenseService(private val context: Context) {

    companion object {
        private var daoProviderOverride: (() -> Triple<TransactionDao, CategoryDao, UserDao>)? = null

        fun overrideDaoProvider(provider: () -> Triple<TransactionDao, CategoryDao, UserDao>) {
            daoProviderOverride = provider
        }

        fun resetDaoProvider() {
            daoProviderOverride = null
        }
    }

    private val transactionDao: TransactionDao
    private val categoryDao: CategoryDao
    private val userDao: UserDao
    private val firebaseDb = FirebaseDatabase.getInstance().reference

    init {
        val (tDao, cDao, uDao) = daoProviderOverride?.invoke()
            ?: AppDatabase.getInstance(context).let {
                Triple(it.transactionDao(), it.categoryDao(), it.userDao())
            }
        transactionDao = tDao
        categoryDao = cDao
        userDao = uDao
    }

    suspend fun getExpenseCategories(): List<Category> = withContext(Dispatchers.IO) {
        categoryDao.getByType("expense")
    }

    suspend fun insertExpense(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            val user = userDao.getActiveUser() ?: return@withContext
            val transactionWithUser = transaction.copy(userId = user.id)
            transactionDao.insert(transactionWithUser)

            val updatedUser = user.copy(initialBalance = user.initialBalance - transaction.amount)
            userDao.update(updatedUser)

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.id.toString())
                .update("initial_balance", updatedUser.initialBalance)

            syncExpenseToFirebase(transactionWithUser)
        }
    }

    private fun syncExpenseToFirebase(transaction: Transaction) {
        val userId = transaction.userId.toString()
        val expenseMap = mapOf(
            "category_id" to transaction.categoryId,
            "description" to transaction.description,
            "date" to transaction.date.time,
            "amount" to transaction.amount
        )
        firebaseDb.child("transactions")
            .child(userId)
            .push()
            .setValue(expenseMap)
    }

    suspend fun getCurrentUserId(context: Context): Int {
        val firebaseUid = SessionManager.getInstance(context).getUserId()
            ?: throw IllegalStateException("User belum login")
        val user = AppDatabase.getInstance(context)
            .userDao().getByFirebaseUid(firebaseUid)
            ?: throw IllegalStateException("User lokal tidak ditemukan")
        return user.id
    }
}