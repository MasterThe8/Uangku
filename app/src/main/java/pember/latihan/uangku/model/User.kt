package pember.latihan.uangku.model

import androidx.room.*
import java.util.*

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    @ColumnInfo(name = "initial_balance") val initialBalance: Double,
    @ColumnInfo(name = "created_at") val createdAt: Date,
    @ColumnInfo(name = "updated_at") val updatedAt: Date,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
