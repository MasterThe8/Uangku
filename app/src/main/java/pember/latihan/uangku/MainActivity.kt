package pember.latihan.uangku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import pember.latihan.uangku.ui.LoginActivity
import pember.latihan.uangku.ui.RegisterActivity
import pember.latihan.uangku.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var tvUserInfo: TextView

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

        // Ambil UID dari session
        val uid = SessionManager(this).getUserId()
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val name = doc.getString("username") ?: "N/A"
                        val email = doc.getString("email") ?: "N/A"
                        val saldo = doc.getDouble("initial_balance") ?: 0.0
                        tvUserInfo.text = "👤 $name\n📧 $email\n💰 Rp${saldo.toInt()}"
                    }
                }
                .addOnFailureListener {
                    tvUserInfo.text = "Gagal mengambil data user."
                }
        } else {
            tvUserInfo.text = "Belum login."
        }
    }
}
