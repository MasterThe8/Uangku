package pember.latihan.uangku.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import pember.latihan.uangku.R
import pember.latihan.uangku.MainActivity
import pember.latihan.uangku.service.RegisterService
import pember.latihan.uangku.utils.DataResetHelper
import pember.latihan.uangku.utils.SessionManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etSaldo: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etSaldo = findViewById(R.id.etSaldo)
        btnRegister = findViewById(R.id.btnRegister)
        tvToLogin = findViewById(R.id.tvToLogin)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val saldo = etSaldo.text.toString().trim().toDoubleOrNull()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || saldo == null) {
                Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password dan konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sessionManager = SessionManager.getInstance(this)
            val currentUserId = sessionManager.getUserId()

            if (currentUserId != null) {
                DataResetHelper.clearLocalDataWithSync(this, currentUserId) {
                    sessionManager.clearSession()
                    registerUser(email, password, username, saldo)
                }
            } else {
                registerUser(email, password, username, saldo)
            }
        }

        tvToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(email: String, password: String, username: String, saldo: Double) {
        RegisterService.register(email, password, username, saldo, this,
            onSuccess = {
                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            },
            onError = {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        )
    }
}
