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

        binding.btnAddIncome.setOnClickListener {
            addIncome()
        }
        binding.etDate.isFocusable = false
        binding.etDate.isClickable = true
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
        lifecycleScope.launch {
            incomeService.logAllUsersAndCategories()
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

        val userId = incomeService.getCurrentUserId()
        val transaction = Transaction(
            userId = userId,
            categoryId = selectedCategoryId,
            description = description,
            date = dateParsed,
            amount = nominal
        )

        lifecycleScope.launch {
            incomeService.insertIncome(transaction)
            Toast.makeText(this@IncomeActivity, "Pemasukan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            finish()
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

}
