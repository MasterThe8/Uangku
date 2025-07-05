package pember.latihan.uangku.model

import androidx.room.*

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firebaseUid: String,
    val username: String = "",
    val email: String = "",
    @ColumnInfo(name = "password_hash") val passwordHash: String = "",
    @ColumnInfo(name = "initial_balance") val initialBalance: Double = 0.0,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
