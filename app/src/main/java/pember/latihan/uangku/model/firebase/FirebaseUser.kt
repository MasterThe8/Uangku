package pember.latihan.uangku.model.firebase

import pember.latihan.uangku.model.User

data class FirebaseUser(
    var id: String = "",
    var username: String = "",
    var email: String = "",
    var passwordHash: String = "",
    var initial_balance: Double = 0.0,
    var is_deleted: Boolean = false
)

fun FirebaseUser.toRoomUser(): User {
    return User(
        id = 0,
        firebaseUid = this.id,
        username = this.username,
        email = this.email,
        passwordHash = "",
        initialBalance = this.initial_balance,
        isDeleted = false
    )
}




