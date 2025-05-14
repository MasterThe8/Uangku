package pember.latihan.uangku.model.firebase

import pember.latihan.uangku.model.Saving

data class FirebaseSaving(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var target_amount: Double = 0.0,
    var current_amount: Double = 0.0
)

fun FirebaseSaving.toRoomSaving(userId: Int): Saving {
    return Saving(
        userId = userId,
        title = title,
        targetAmount = target_amount,
        currentAmount = current_amount
    )
}

