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
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT * FROM category")
    suspend fun getAll(): List<Category>

    @Query("SELECT id FROM category WHERE name = :name LIMIT 1")
    suspend fun getIdByName(name: String): Int?

    @Query("SELECT * FROM category WHERE type = :type")
    suspend fun getCategoriesByType(type: String): List<Category>

    @Query("SELECT id FROM category WHERE name = :name LIMIT 1")
    suspend fun getCategoryIdByName(name: String): Int
}
