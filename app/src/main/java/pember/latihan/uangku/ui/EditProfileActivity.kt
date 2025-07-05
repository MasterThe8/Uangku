package pember.latihan.uangku.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import pember.latihan.uangku.R
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.service.EditProfileService
import pember.latihan.uangku.utils.FirebaseSyncHelper
import pember.latihan.uangku.utils.SessionManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etSaldo: EditText
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnBack: ImageView
    private lateinit var btnSave: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etSaldo = findViewById(R.id.etSaldo)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)

        lifecycleScope.launch {
//            val uid = SessionManager.getInstance(this@EditProfileActivity).getUserId()
//            if (uid != null) {
//                FirebaseSyncHelper.syncFromFirebase(this@EditProfileActivity, uid)
//            }

            val user = AppDatabase.getInstance(this@EditProfileActivity)
                .userDao().getActiveUser()

            user?.let {
                etUsername.setText(it.username)
                etEmail.setText(it.email)
                etSaldo.setText(it.initialBalance.toString())
            }

        }

        btnBack.setOnClickListener{
            finish()
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
                    val message = result.exceptionOrNull()?.message ?: "Gagal memperbarui"

                    if (message.startsWith("Perubahan email atau password berhasil") ||
                        message.contains("Verifikasi dikirim ke")) {

                        Toast.makeText(this@EditProfileActivity, message, Toast.LENGTH_LONG).show()
                        SessionManager.getInstance(this@EditProfileActivity).clearSession()
                        val intent = Intent(this@EditProfileActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@EditProfileActivity, message, Toast.LENGTH_LONG).show()
                    }
                }

                Log.d("EditProfile", "Hasil: $result")
            }
        }
    }
}