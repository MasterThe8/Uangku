package pember.latihan.uangku.ui

import android.R
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pember.latihan.uangku.databinding.ActivityIncomeBinding
import pember.latihan.uangku.model.Category
import pember.latihan.uangku.model.Transaction
import pember.latihan.uangku.service.IncomeService
import java.text.SimpleDateFormat
import java.util.Locale
import android.app.DatePickerDialog
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.dao.UserDao
import java.util.Calendar

class IncomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomeBinding
    private lateinit var incomeService: IncomeService
    private lateinit var categoryList: List<Category>
    private var selectedCategoryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeService = IncomeService(applicationContext)

        loadCategories()
        showIncomeTransactions()

        binding.btnAddIncome.setOnClickListener {
            addIncome()
        }

        binding.etDate.isFocusable = false
        binding.etDate.isClickable = true
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            categoryList = incomeService.getIncomeCategories()

            val categoryNames = categoryList.map { it.name }
            val adapter = ArrayAdapter(this@IncomeActivity, R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter

            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedCategoryId = categoryList[position].id
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun addIncome() {
        val nominal = binding.etNominal.text.toString().toDoubleOrNull()
        val description = binding.etDescription.text.toString()
        val date = binding.etDate.text.toString()

        if (nominal == null || description.isBlank() || date.isBlank() || selectedCategoryId == -1) {
            Toast.makeText(this, "Semua field harus diisi dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        val dateParsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)

        // Pindahkan semuanya ke dalam coroutine
        lifecycleScope.launch {
            try {
                val userId = incomeService.getCurrentUserId(this@IncomeActivity)
                Log.d("IncomeActivity", "Current userId saat insert: $userId")

                val transaction = Transaction(
                    userId = userId,
                    categoryId = selectedCategoryId,
                    description = description,
                    date = dateParsed!!,
                    amount = nominal
                )

                incomeService.insertIncome(transaction)
                showIncomeTransactions()
                Toast.makeText(this@IncomeActivity, "Pemasukan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: IllegalStateException) {
                Toast.makeText(this@IncomeActivity, e.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun showIncomeTransactions() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(this@IncomeActivity)

                val userId = incomeService.getCurrentUserId(this@IncomeActivity)

                val incomeTransactions = withContext(Dispatchers.IO) {
                    db.transactionDao().getIncomeTransactions(userId)
                }

                val sb = StringBuilder()
                if (incomeTransactions.isEmpty()) {
                    sb.appendLine("Belum ada pemasukan.")
                } else {
                    incomeTransactions.forEach {
                        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date)
                        sb.appendLine("- ${it.description} | Rp${it.amount.toInt()} | $formattedDate")
                    }
                }

                val allTransactions = withContext(Dispatchers.IO) {
                    db.transactionDao().getAll()
                }
                Log.d("IncomeActivity", "All transactions in DB: $allTransactions")

                val transactions = withContext(Dispatchers.IO) {
                    db.transactionDao().getByUser(userId)
                }
                Log.d("IncomeActivity", "Transactions by userId: $transactions")

                binding.tvIncomeList.text = sb.toString()

            } catch (e: IllegalStateException) {
                Log.e("IncomeActivity", "Error: ${e.message}")
                Toast.makeText(this@IncomeActivity, "User belum login", Toast.LENGTH_LONG).show()
            }
        }
    }
}

