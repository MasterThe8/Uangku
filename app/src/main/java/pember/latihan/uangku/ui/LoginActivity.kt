package pember.latihan.uangku.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.latihan.uangku.R
import pember.latihan.uangku.MainActivity
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.firebase.FirebaseUser
import pember.latihan.uangku.model.firebase.toRoomUser
import pember.latihan.uangku.service.LoginService
import pember.latihan.uangku.utils.FirebaseSyncHelper
import pember.latihan.uangku.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvToRegister: TextView
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvToRegister = findViewById(R.id.tvToRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val identity = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (identity.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            LoginService.login(identity, password, this,
                onSuccess = { firebaseUser: FirebaseUser ->
                    val sessionManager = SessionManager(this)
                    val user = firebaseUser.toRoomUser()

                    val userDao = AppDatabase.getInstance(this).userDao()
                    lifecycleScope.launch {
                        // Insert ke Room dan dapatkan ID-nya
                        val newId = withContext(Dispatchers.IO) {
                            userDao.insertAndGetId(user)
                        }

                        sessionManager.saveUserId(newId.toString())

                        FirebaseSyncHelper.syncFromFirebase(this@LoginActivity, firebaseUser.id)
                        Toast.makeText(this@LoginActivity, "Selamat datang ${firebaseUser.username}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Masukkan email untuk reset password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    showResetDialog("Email reset password berhasil dikirim ke $email")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showResetDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
