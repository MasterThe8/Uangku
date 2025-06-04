package pember.latihan.uangku.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pember.latihan.uangku.model.Transaction
import pember.latihan.uangku.model.dao.TransactionWithCategory

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<Transaction>

    @Query("SELECT * FROM transactions t INNER JOIN category c ON t.category_id = c.id")
    suspend fun getAllWithCategory(): List<TransactionWithCategory>

    @Query("SELECT * FROM transactions WHERE user_id = :userId")
    suspend fun getByUser(userId: Int): List<Transaction>

    @Query("""SELECT t.* FROM transactions t INNER JOIN category c ON t.category_id = c.id WHERE t.user_id = :userId AND c.type = 'income'""")
    suspend fun getIncomeTransactions(userId: Int): List<Transaction>

    @Query("""SELECT t.* FROM transactions t INNER JOIN category c ON t.category_id = c.id WHERE t.user_id = :userId AND c.type = 'expense'""")
    suspend fun getExpenseTransactions(userId: Int): List<Transaction>

    @Query("""SELECT SUM(t.amount) FROM transactions t INNER JOIN category c ON t.category_id = c.id WHERE t.user_id = :userId AND c.type = 'income'""")
    suspend fun getTotalIncome(userId: Int): Double?

    @Query("""SELECT SUM(t.amount) FROM transactions t INNER JOIN category c ON t.category_id = c.id WHERE t.user_id = :userId AND c.type = 'expense'""")
    suspend fun getTotalExpense(userId: Int): Double?

    @Query("""SELECT SUM(amount) FROM transactions t INNER JOIN category c ON t.category_id = c.id WHERE t.user_id = :userId AND c.id = :categoryId""")
    suspend fun getSumAmountByCategory(userId: Int, categoryId: Int): Double?

    @Query("SELECT * FROM transactions t INNER JOIN category c ON t.category_id = c.id WHERE t.user_id = :userId AND c.type = 'income' ORDER BY date DESC")
    suspend fun getIncomeTransactionsSorted(userId: String): List<Transaction>

    @Query("""SELECT * FROM transactions t INNER JOIN category c ON t.category_id = c.id WHERE t.user_id = :userId AND c.type = 'income' ORDER BY t.date DESC""")
    suspend fun getIncomeTransactionsWithCategory(userId: Int): List<TransactionWithCategory>

    @Query("""SELECT * FROM transactions t INNER JOIN category c ON t.category_id = c.id WHERE t.user_id = :userId AND c.type = 'expense' ORDER BY t.date DESC""")
    suspend fun getExpenseTransactionsWithCategory(userId: Int): List<TransactionWithCategory>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
