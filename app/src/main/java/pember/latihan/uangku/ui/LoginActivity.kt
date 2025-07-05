package pember.latihan.uangku.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.latihan.uangku.R
import pember.latihan.uangku.MainActivity
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.User
import pember.latihan.uangku.utils.FirebaseSyncHelper
import pember.latihan.uangku.utils.SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvToRegister: TextView
    private lateinit var tvForgotPassword: TextView
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvToRegister = findViewById(R.id.tvToRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            attemptLogin()
        }
        tvForgotPassword.setOnClickListener {
            sendResetEmail()
        }
        tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val context = this
        val sessionManager = SessionManager.getInstance(context)
        val currentUid = sessionManager.getUserId()

        if (currentUid != null) {
            FirebaseSyncHelper.syncToFirebase(context, currentUid) {
                Log.d("Login", "✅ Data user sebelumnya disinkron ke Firebase.")

                doLogin(email, password)
            }
        } else {
            doLogin(email, password)
        }
    }

    private fun doLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                handleAuthSuccess(result.user?.uid, email, password)
            }
            .addOnFailureListener { ex ->
                Toast.makeText(this, "Login gagal: ${ex.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun handleAuthSuccess(firebaseUid: String?, email: String, password: String) {
        if (firebaseUid == null) {
            Toast.makeText(this, "UID Firebase tidak tersedia", Toast.LENGTH_LONG).show()
            return
        }

        val sessionManager = SessionManager.getInstance(this)
        sessionManager.saveUserId(firebaseUid)

        val newUser = User(
            firebaseUid = firebaseUid,
            email = email,
            username = email.substringBefore("@"),
            passwordHash = "",
            initialBalance = 0.0,
            isDeleted = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            val userDao = AppDatabase.getInstance(this@LoginActivity).userDao()
            val existing = userDao.getUserByEmail(email)
            if (existing == null) {
                userDao.insert(newUser)
                Log.d("LoginActivity", "✅ User baru dimasukkan ke Room.")
            } else {
                Log.d("LoginActivity", "ℹ User sudah ada di Room.")
            }

            FirebaseSyncHelper.syncFromFirebase(this@LoginActivity, firebaseUid)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun sendResetEmail() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Masukkan email untuk reset password", Toast.LENGTH_SHORT).show()
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setTitle("Reset Password")
                    .setMessage("Email reset password berhasil dikirim ke $email")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            .addOnFailureListener { ex ->
                Toast.makeText(this, "Gagal: ${ex.message}", Toast.LENGTH_LONG).show()
            }
    }
}
