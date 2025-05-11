package pember.latihan.uangku.model.firebase

import pember.latihan.uangku.model.Transaction
import java.util.Date

data class FirebaseTransaction(
    var id: String = "",
    var userId: String = "",
    var categoryName: String = "", // tambahkan
    var type: String = "",         // income / expense
    var description: String = "",
    var date: Long = 0L,
    var amount: Double = 0.0
)


fun FirebaseTransaction.toRoomTransaction(localUserId: Int, localCategoryId: Int): Transaction {
    return Transaction(
        userId = localUserId,
        categoryId = localCategoryId,
        description = description,
        date = Date(date),
        amount = amount
    )
}

