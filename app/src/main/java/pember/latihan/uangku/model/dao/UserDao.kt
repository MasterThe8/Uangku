package pember.latihan.uangku.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pember.latihan.uangku.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndGetId(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE is_deleted = 0 LIMIT 1")
    suspend fun getActiveUser(): User?

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    suspend fun getByFirebaseUid(uid: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
