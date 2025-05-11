package pember.latihan.uangku.ui

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pember.latihan.uangku.R
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.service.EditProfileService

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etSaldo: EditText
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etSaldo = findViewById(R.id.etSaldo)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSave = findViewById(R.id.btnSave)

        // Load data dari Room
        lifecycleScope.launch {
            val user = AppDatabase.getInstance(this@EditProfileActivity)
                .userDao().getActiveUser()

            user?.let {
                etUsername.setText(it.username)
                etEmail.setText(it.email)
                etSaldo.setText(it.initialBalance.toString())
            }
        }

        btnSave.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val saldo = etSaldo.text.toString().trim().toDoubleOrNull()
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || saldo == null) {
                Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!newPassword.isNullOrBlank() || !confirmPassword.isNullOrBlank()) {
                if (currentPassword.isNullOrBlank()) {
                    Toast.makeText(this, "Masukkan password saat ini", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            lifecycleScope.launch {
                val result = EditProfileService.updateProfile(
                    context = this@EditProfileActivity,
                    newUsername = username,
                    newEmail = email,
                    newSaldo = saldo,
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
                Log.d("EditProfile", "Mulai update profile")
                if (result.isSuccess) {
                    Toast.makeText(this@EditProfileActivity, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditProfileActivity, "Gagal: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
                Log.d("EditProfile", "Hasil: $result")
            }
        }
    }
}
