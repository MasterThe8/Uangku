package pember.latihan.uangku.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pember.latihan.uangku.R
import pember.latihan.uangku.ui.LoginActivity
import pember.latihan.uangku.MainActivity
import pember.latihan.uangku.utils.CategorySyncHelper
import pember.latihan.uangku.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            CategorySyncHelper.syncCategories(this@SplashActivity)

            Handler(Looper.getMainLooper()).postDelayed({
                val sessionManager = SessionManager.getInstance(this@SplashActivity)
                val uid = sessionManager.getUserId()

                val nextIntent = if (uid != null) {
                    Intent(this@SplashActivity, MainActivity::class.java)
                } else {
                    Intent(this@SplashActivity, LoginActivity::class.java)
                }

                startActivity(nextIntent)
                finish()
            }, 2000)
        }
    }
}