package pember.latihan.uangku.service

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pember.latihan.uangku.model.FirebaseUser
import pember.latihan.uangku.utils.SessionManager

object RegisterService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun register(
        email: String,
        password: String,
        username: String,
        saldo: Double,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = FirebaseUser(
                    id = uid,
                    username = username,
                    email = email,
                    passwordHash = "",
                    initial_balance = saldo,
                    is_deleted = false
                )

                db.collection("users").document(uid).set(user)
                    .addOnSuccessListener {
                        SessionManager(context).saveUserId(uid)
                        onSuccess()
                    }
                    .addOnFailureListener { onError("Gagal simpan user di Firestore.") }
            }
            .addOnFailureListener {
                onError(it.message ?: "Registrasi gagal.")
            }
    }
}
