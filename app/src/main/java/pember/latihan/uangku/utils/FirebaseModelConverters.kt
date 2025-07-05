package pember.latihan.uangku.utils

import pember.latihan.uangku.model.*
import pember.latihan.uangku.model.firebase.*

fun User.fromRoomUser(firebaseId: String): FirebaseUser {
    return FirebaseUser(
        id = firebaseId,
        username = username,
        email = email,
        passwordHash = "",
        initial_balance = initialBalance,
        is_deleted = isDeleted
    )
}

fun Saving.fromRoomSaving(userUid: String): FirebaseSaving {
    return FirebaseSaving(
        id = "",
        userId = userUid,
        title = title,
        target_amount = targetAmount,
        current_amount = currentAmount
    )
}

fun Transaction.fromRoomTransaction(categoryName: String, categoryType: String, userUid: String): FirebaseTransaction {
    return FirebaseTransaction(
        id = "",
        userId = userUid,
        categoryName = categoryName,
        type = categoryType,
        description = description,
        date = date.time,
        amount = amount
    )
}
