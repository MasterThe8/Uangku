package pember.latihan.uangku.model

data class FirebaseUser(
    var id: String = "",
    var username: String = "",
    var email: String = "",
    var passwordHash: String = "",
    var initial_balance: Double = 0.0,
    var is_deleted: Boolean = false
)
