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

    @Query("SELECT * FROM saving WHERE user_id = :userId")
    suspend fun getByUser(userId: Int): List<Saving>

    @Query("DELETE FROM saving")
    suspend fun deleteAll()
}
