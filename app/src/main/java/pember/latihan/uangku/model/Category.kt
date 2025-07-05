package pember.latihan.uangku.model

import androidx.room.*
import java.util.*

@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String // "income" atau "expense"
)
