package pember.latihan.uangku.model.dao

import androidx.room.Embedded
import androidx.room.Relation
import pember.latihan.uangku.model.Category
import pember.latihan.uangku.model.Transaction

data class TransactionWithCategory(
    @Embedded val transaction: Transaction,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category
)
