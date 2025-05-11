package pember.latihan.uangku.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pember.latihan.uangku.model.Transaction

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM `transaction` WHERE user_id = :userId")
    suspend fun getByUser(userId: Int): List<Transaction>

    @Query("DELETE FROM `transaction`")
    suspend fun deleteAll()
}
