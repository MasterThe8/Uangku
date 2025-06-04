package pember.latihan.uangku.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.latihan.uangku.R
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.Category
import pember.latihan.uangku.model.dao.TransactionWithCategory
import pember.latihan.uangku.model.seed.CategorySeed
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var spinnerTimeFilter: Spinner
    private lateinit var spinnerCategoryFilter: Spinner
    private val decimalFormat = DecimalFormat("#,###")
    private var allHistory: List<TransactionWithCategory> = emptyList()
    private lateinit var adapter: HistoryAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.listHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        spinnerTimeFilter = findViewById(R.id.spinnerTimeFilter)
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        setupTimeFilterSpinner()
        setupCategoryFilterSpinner()

        loadHistory()
    }

    private fun setupTimeFilterSpinner() {
        // Opsi-opsi filter waktu
        val timeOptions = listOf("Semua", "Hari Ini", "Minggu Ini", "Bulan Ini")
        val timeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            timeOptions
        )
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeFilter.adapter = timeAdapter

        spinnerTimeFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    applyFilters()
                }
                override fun onNothingSelected(parent: AdapterView<*>) { /* tidak perlu */ }
            }
    }

    private fun setupCategoryFilterSpinner() {
        val categoryNames = mutableListOf<String>()
        categoryNames.add("Semua")
        CategorySeed.defaultCategories.forEach { categoryNames.add(it.name) }

        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoryFilter.adapter = categoryAdapter

        spinnerCategoryFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    applyFilters()
                }
                override fun onNothingSelected(parent: AdapterView<*>) { /* tidak perlu */ }
            }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            allHistory = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@HistoryActivity)
                    .transactionDao()
                    .getAllWithCategory()
            }.sortedByDescending { it.transaction.date }
            adapter = HistoryAdapter(allHistory)
            rvHistory.adapter = adapter
            applyFilters()
        }
    }

    private fun applyFilters() {
        val timeSelection = spinnerTimeFilter.selectedItem as String
        val now = Calendar.getInstance()

        val afterTimeFilter = when (timeSelection) {
            "Hari Ini" -> {
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                allHistory.filter {
                    it.transaction.date >= startOfDay
                }
            }
            "Minggu Ini" -> {
                val startOfWeek = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                allHistory.filter {
                    it.transaction.date >= startOfWeek
                }
            }
            "Bulan Ini" -> {
                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                allHistory.filter {
                    it.transaction.date >= startOfMonth
                }
            }
            else -> {
                allHistory
            }
        }

        val categorySelection = spinnerCategoryFilter.selectedItem as String
        val afterCategoryFilter = if (categorySelection == "Semua") {
            afterTimeFilter
        } else {
            afterTimeFilter.filter {
                it.category.name == categorySelection
            }
        }

        val incomeTotal = afterCategoryFilter
            .filter { it.category.type == "income" }
            .sumOf { it.transaction.amount }

        val expenseTotal = afterCategoryFilter
            .filter { it.category.type == "expense" }
            .sumOf { it.transaction.amount }

        val netTotal = incomeTotal - expenseTotal

        val formattedIncome = "Rp" + decimalFormat.format(incomeTotal.toLong())
        val formattedExpense = "Rp" + decimalFormat.format(expenseTotal.toLong())
        val formattedNet = "Rp" + decimalFormat.format(netTotal.toLong())

        findViewById<TextView>(R.id.labelIncome).text = "Pemasukan: $formattedIncome"
        findViewById<TextView>(R.id.labelExpense).text = "Pengeluaran: $formattedExpense"

        val labelTotal = findViewById<TextView>(R.id.labelTotal)
        labelTotal.text = "Total: $formattedNet"
        labelTotal.setTextColor(
            ContextCompat.getColor(
                this,
                if (netTotal >= 0) R.color.income_gaji else R.color.expense_tagihan
            )
        )

        adapter.updateList(afterCategoryFilter)
    }

    private inner class HistoryAdapter(
        private var items: List<TransactionWithCategory>
    ) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_transaction_history, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        fun updateList(newList: List<TransactionWithCategory>) {
            items = newList
            notifyDataSetChanged()
        }

        inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
            private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
            private val tvDescDate: TextView = itemView.findViewById(R.id.tvDescDate)

            fun bind(entry: TransactionWithCategory) {
                val category = entry.category
                val tx = entry.transaction

                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, android.R.color.white)
                )
                tvCategory.text = category.name

                val formatted = decimalFormat.format(tx.amount.toLong())
                val displayAmount = if (category.type == "expense") {
                    "-Rp$formatted"
                } else {
                    "Rp$formatted"
                }
                tvAmount.text = displayAmount

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateText = sdf.format(tx.date)
                tvDescDate.text = "${tx.description} • $dateText"

                if (category.type == "expense") {
                    tvAmount.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.expense)
                    )
                } else {
                    tvAmount.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.income)
                    )
                }
            }
        }
    }
}
