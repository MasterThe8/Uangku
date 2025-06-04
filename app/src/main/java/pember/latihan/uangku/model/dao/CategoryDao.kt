package pember.latihan.uangku.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pember.latihan.uangku.model.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(category: List<Category>)

    @Query("SELECT * FROM category")
    suspend fun getAll(): List<Category>

    @Query("SELECT * FROM category WHERE name = :name")
    suspend fun getByName(name: String): Category?

    @Query("SELECT * FROM category WHERE type = :type")
    suspend fun getByType(type: String): List<Category>

    @Query("SELECT * FROM category WHERE name = :name AND type = :type")
    suspend fun getByNameAndType(name: String, type: String): Category?

    @Query("DELETE FROM category")
    suspend fun deleteAll()
}
