package pember.latihan.uangku.model

import androidx.room.*
import java.util.*

@Entity(
    tableName = "saving",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["user_id"])
    ],
    indices = [Index("user_id")]
)
data class Saving(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    val title: String,
    @ColumnInfo(name = "target_amount") val targetAmount: Double,
    @ColumnInfo(name = "current_amount") val currentAmount: Double
)
