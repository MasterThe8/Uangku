package pember.latihan.uangku.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.latihan.uangku.R
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.utils.FirebaseSyncHelper
import pember.latihan.uangku.utils.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnBack: ImageView
    private lateinit var imgAvatar: ImageView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etUsername = findViewById(R.id.et_username)
        etEmail = findViewById(R.id.et_email)
        btnEdit = findViewById(R.id.btn_edit)
        btnBack = findViewById(R.id.btn_back)
        imgAvatar = findViewById(R.id.img_avatar)

        lifecycleScope.launch {
            val user = AppDatabase.getInstance(this@ProfileActivity)
                .userDao().getActiveUser()

            user?.let {
                etUsername.setText(it.username)
                etEmail.setText(it.email)
            }
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            startActivity(intent)
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnLogout = findViewById(R.id.btnLogout)

        btnLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun handleLogout() {
        val context = this
        val uid = SessionManager.getInstance(context).getUserId()

        if (uid != null) {
            FirebaseSyncHelper.syncToFirebase(context, uid) {
                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getInstance(context).clearAllTables()
                    SessionManager.getInstance(context).clearSession()
                    FirebaseAuth.getInstance().signOut()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Logout berhasil", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        } else {
            Toast.makeText(context, "UID tidak ditemukan, logout dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }
}
