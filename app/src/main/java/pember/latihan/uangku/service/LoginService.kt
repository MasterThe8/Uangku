package pember.latihan.uangku.service

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pember.latihan.uangku.model.firebase.FirebaseUser
import pember.latihan.uangku.utils.SessionManager

object LoginService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(
        identity: String, // bisa username atau email
        password: String,
        context: Context,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(identity).matches()

        // Jika email langsung login
        if (isEmail) {
            signInWithEmail(identity, password, context, onSuccess, onError)
        } else {
            // Jika username, cari email dulu dari Firestore
            FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("username", identity)
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        onError("Username tidak ditemukan")
                    } else {
                        val doc = result.documents.first()
                        val email = doc.getString("email")
                        if (email != null) {
                            signInWithEmail(email, password, context, onSuccess, onError)
                        } else {
                            onError("Email tidak ditemukan")
                        }
                    }
                }
                .addOnFailureListener {
                    onError("Gagal mencari user: ${it.message}")
                }
        }
    }

    private fun signInWithEmail(
        email: String,
        password: String,
        context: Context,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                SessionManager(context).saveUserId(uid)

                FirebaseFirestore.getInstance().collection("users")
                    .document(uid).get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(FirebaseUser::class.java)
                        if (user != null) onSuccess(user)
                        else onError("Data tidak ditemukan.")
                    }
                    .addOnFailureListener {
                        onError("Gagal ambil data user.")
                    }
            }
            .addOnFailureListener {
                onError(it.message ?: "Login gagal.")
            }
    }
}
