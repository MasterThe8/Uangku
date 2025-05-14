package pember.latihan.uangku

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.ui.LoginActivity
import pember.latihan.uangku.ui.RegisterActivity
import pember.latihan.uangku.ui.EditProfileActivity
import pember.latihan.uangku.ui.IncomeActivity
import pember.latihan.uangku.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var tvUserInfo: TextView
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvUserInfo = findViewById(R.id.tvUserInfo)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnIncome).setOnClickListener {
            startActivity(Intent(this, IncomeActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        showUserData()
    }

    private fun showUserData() {
        scope.launch {
            val db = AppDatabase.getInstance(this@MainActivity)
            val user = withContext(Dispatchers.IO) {
                db.userDao().getActiveUser()
            }

            if (user == null) {
                tvUserInfo.text = "Belum login."
                return@launch
            }

            val sb = StringBuilder()
            sb.appendLine("👤 ${user.username}")
            sb.appendLine("📧 ${user.email}")
            sb.appendLine("💰 Rp${user.initialBalance.toInt()}")

            tvUserInfo.text = sb.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

