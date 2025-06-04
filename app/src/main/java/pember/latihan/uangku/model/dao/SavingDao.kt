package pember.latihan.uangku.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pember.latihan.uangku.model.Saving

@Dao
interface SavingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(saving: Saving)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(savings: List<Saving>)

    @Query("SELECT * FROM savings")
    suspend fun getAll(): List<Saving>

    @Query("SELECT * FROM savings WHERE user_id = :userId")
    suspend fun getByUser(userId: Int): List<Saving>

    @Query("SELECT * FROM savings WHERE title = :title LIMIT 1")
    suspend fun getSavingByTitle(title: String): Saving?

    @Query("UPDATE savings SET current_amount = :newAmount WHERE title = :title")
    suspend fun updateCurrentAmountByTitle(title: String, newAmount: Double)

    @Query("DELETE FROM savings")
    suspend fun deleteAll()
}
