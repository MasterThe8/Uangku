package pember.latihan.uangku

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import java.text.DecimalFormat
import kotlinx.coroutines.*
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.ui.*
import pember.latihan.uangku.utils.FirebaseSyncHelper

class MainActivity : AppCompatActivity() {

    private lateinit var tvHelloUser: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnPengeluaranAction: LinearLayout
    private lateinit var btnPendapatanAction: LinearLayout
    private lateinit var btnTabungan: LinearLayout
    private lateinit var tvBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private val numberFormat = DecimalFormat("#,###")

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvHelloUser = findViewById(R.id.tvHelloUser)
        imgProfile = findViewById(R.id.imgProfile)
        btnPengeluaranAction = findViewById(R.id.btnPengeluaranAction)
        btnPendapatanAction = findViewById(R.id.btnPendapatanAction)
        btnTabungan = findViewById(R.id.btnTabungan)
        tvBalance = findViewById(R.id.tvBalance)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)

        imgProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnPengeluaranAction.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }

        btnPendapatanAction.setOnClickListener {
            startActivity(Intent(this, IncomeActivity::class.java))
        }

        btnTabungan.setOnClickListener {
            startActivity(Intent(this, SavingActivity::class.java))
        }

        lifecycleScope.launch {
            FirebaseSyncHelper.syncEmailIfChanged(this@MainActivity)
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
                tvHelloUser.text = "Belum login."
                tvBalance.text = "-"
                tvTotalIncome.text = "-"
                tvTotalExpense.text = "-"
                return@launch
            }

            tvHelloUser.text = "Halo, ${user.username}!"

            val (initialBalance, totalIncome, totalExpense) = withContext(Dispatchers.IO) {
                val income = db.transactionDao().getTotalIncome(user.id) ?: 0.0
                val expense = db.transactionDao().getTotalExpense(user.id) ?: 0.0
                val initBalance = db.userDao().getInitialBalance(user.id) ?: 0.0
                Triple(initBalance, income, expense)
            }

            tvBalance.text = "Rp ${numberFormat.format(initialBalance)}"
            tvTotalIncome.text = "Rp ${numberFormat.format(totalIncome)}"
            tvTotalExpense.text = "Rp ${numberFormat.format(totalExpense)}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
