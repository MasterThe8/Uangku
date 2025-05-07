package pember.latihan.uangku.service

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pember.latihan.uangku.model.FirebaseUser
import pember.latihan.uangku.utils.SessionManager

object LoginService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun login(
        email: String,
        password: String,
        context: Context,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                SessionManager(context).saveUserId(uid)

                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(FirebaseUser::class.java)
                        if (user != null) onSuccess(user)
                        else onError("Data tidak ditemukan.")
                    }
                    .addOnFailureListener { onError("Gagal ambil data user.") }
            }
            .addOnFailureListener {
                onError(it.message ?: "Login gagal.")
            }
    }
}
