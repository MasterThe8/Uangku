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
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
