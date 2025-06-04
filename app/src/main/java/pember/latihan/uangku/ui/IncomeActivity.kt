package pember.latihan.uangku.ui

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import java.text.DecimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pember.latihan.uangku.R
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.model.Category
import pember.latihan.uangku.model.Transaction
import pember.latihan.uangku.model.dao.TransactionWithCategory
import pember.latihan.uangku.service.IncomeService
import java.text.SimpleDateFormat
import java.util.*

class IncomeActivity : AppCompatActivity() {
    private lateinit var btnAddIncome: Button
    private lateinit var llCategoryContainer: LinearLayout
    private lateinit var incomeService: IncomeService
    private lateinit var tvTotalIncome: TextView
    private var categoryList: List<Category> = emptyList()
    private var selectedUserId: Int = -1
    private val numberFormat = DecimalFormat("###,###,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)

        btnAddIncome = findViewById(R.id.btnAddIncome)
        llCategoryContainer = findViewById(R.id.llCategoryContainer)
        incomeService = IncomeService(applicationContext)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)

        lifecycleScope.launch {
            selectedUserId = incomeService.getCurrentUserId(this@IncomeActivity)
            loadAndDisplayCategories()
            showIncomeTransactions()
        }

        btnAddIncome.setOnClickListener {
            showAddIncomeDialog()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.iconHistory).setOnClickListener{
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private suspend fun fetchCategoriesFromService(): List<Category> {
        return withContext(Dispatchers.IO) {
            incomeService.getIncomeCategories()
        }
    }

    private suspend fun fetchIncomeTransactionsForUser(): List<Transaction> {
        return withContext(Dispatchers.IO) {
            AppDatabase
                .getInstance(this@IncomeActivity)
                .transactionDao()
                .getIncomeTransactions(selectedUserId)
        }
    }

    private fun loadAndDisplayCategories() {
        lifecycleScope.launch {
            categoryList = fetchCategoriesFromService()
            val allIncomeTrans = fetchIncomeTransactionsForUser()

            llCategoryContainer.removeAllViews()

            for (category in categoryList) {
                val transForCat = allIncomeTrans.filter { it.categoryId == category.id }
                val totalAmount = transForCat.sumOf { it.amount }
                val lastDateString = if (transForCat.isNotEmpty()) {
                    val latestDate: Date = transForCat.maxByOrNull { it.date }!!.date
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(latestDate)
                } else {
                    "-"
                }

                val categoryCard = createCategoryCard(category.name, totalAmount, lastDateString)
                llCategoryContainer.addView(categoryCard)
            }
        }
    }

    private fun createCategoryCard(
        categoryName: String,
        totalAmount: Double,
        lastDateText: String
    ): LinearLayout {
        val dpToPx = { dp: Float -> (dp * resources.displayMetrics.density).toInt() }
        val marginBottomPx = dpToPx(16f)
        val paddingPx = dpToPx(10f)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@IncomeActivity, R.color.income_gaji))
            elevation = dpToPx(2f).toFloat()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = marginBottomPx
            }
        }

        val tvTitle = TextView(this).apply {
            text = categoryName
            setTextColor(ContextCompat.getColor(this@IncomeActivity, R.color.black))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val tvTotal = TextView(this).apply {
            text = "Rp ${numberFormat.format(totalAmount)}"
            setTextColor(ContextCompat.getColor(this@IncomeActivity, R.color.black))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val tvDate = TextView(this).apply {
            text = lastDateText
            setTextColor(ContextCompat.getColor(this@IncomeActivity, R.color.black))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        card.addView(tvTitle)
        card.addView(tvTotal)
        card.addView(tvDate)

        return card
    }

    private fun showAddIncomeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_income, null)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val etNominal = dialogView.findViewById<EditText>(R.id.etNominal)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etDate = dialogView.findViewById<EditText>(R.id.etDate)

        val categoryNames = categoryList.map { it.name }
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryNames
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerCategory.adapter = spinnerAdapter

        var chosenCategoryId: Int = if (categoryList.isNotEmpty()) categoryList[0].id else -1
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                chosenCategoryId = categoryList[position].id
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    etDate.setText(formatted)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val alert = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Tambah Pemasukan")
            .setPositiveButton("SIMPAN", null)
            .setNegativeButton("BATAL") { dialog, _ -> dialog.dismiss() }
            .create()

        alert.setOnShowListener {
            val btnSimpan = alert.getButton(DialogInterface.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val strNominal = etNominal.text.toString().trim()
                val strDesc = etDescription.text.toString().trim()
                val strDate = etDate.text.toString().trim()

                if (strNominal.isEmpty() || strDesc.isEmpty() || strDate.isEmpty() || chosenCategoryId == -1) {
                    Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val nominalValue = strNominal.toDoubleOrNull()
                if (nominalValue == null || nominalValue <= 0.0) {
                    Toast.makeText(this, "Nominal tidak valid!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val parsedDate = sdf.parse(strDate) ?: Date()

                    val newTx = Transaction(
                        userId = selectedUserId,
                        categoryId = chosenCategoryId,
                        description = strDesc,
                        date = parsedDate,
                        amount = nominalValue
                    )

                    incomeService.insertIncome(newTx)

                    loadAndDisplayCategories()
                    showIncomeTransactions()

                    Toast.makeText(this@IncomeActivity, "Pemasukan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    alert.dismiss()
                }
            }
        }

        alert.show()
    }

    private fun showIncomeTransactions() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(this@IncomeActivity)
                val userId = selectedUserId

                val totalIncome = withContext(Dispatchers.IO) {
                    db.transactionDao().getTotalIncome(userId) ?: 0.0
                }
                tvTotalIncome.text = "Rp ${numberFormat.format(totalIncome)}"

                val incomeTransactions = withContext(Dispatchers.IO) {
                    db.transactionDao().getIncomeTransactionsWithCategory(userId)
                }

                llCategoryContainer.removeAllViews()

                if (incomeTransactions.isEmpty()) {
                    val emptyView = TextView(this@IncomeActivity).apply {
                        text = "Belum ada pemasukan."
                        textSize = 16f
                        setPadding(10, 10, 10, 10)
                    }
                    llCategoryContainer.addView(emptyView)

                    setupPieChart(
                        values = listOf(1f),
                        colors = listOf(ComposeColor.LightGray)
                    )
                    return@launch
                }

                val groupedByCategory: Map<Int, List<TransactionWithCategory>> =
                    incomeTransactions.groupBy { it.category.id }

                val valuesList = mutableListOf<Float>()
                val colorsList = mutableListOf<ComposeColor>()

                groupedByCategory.forEach { (_, listTx) ->
                    val category = listTx[0].category
                    val categoryName = category.name

                    val totalPerCategory: Double = listTx.sumOf { it.transaction.amount }

                    val latestDate: Date =
                        listTx.maxByOrNull { it.transaction.date }!!.transaction.date
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(latestDate)

                    val itemView = layoutInflater.inflate(
                        R.layout.item_transaction,
                        llCategoryContainer,
                        false
                    )

                    val tvCategory = itemView.findViewById<TextView>(R.id.tvCategory)
                    val tvAmount = itemView.findViewById<TextView>(R.id.tvAmount)
                    val tvDate = itemView.findViewById<TextView>(R.id.tvDate)

                    tvCategory.setStrokeText(
                        text = categoryName,
                        strokeColor = Color.BLACK,
                        strokeWidth = 6f,
                        fillColor = Color.WHITE
                    )
                    tvAmount.setStrokeText(
                        text = "Rp${numberFormat.format(totalPerCategory)}",
                        strokeColor = Color.BLACK,
                        strokeWidth = 6f,
                        fillColor = Color.YELLOW
                    )
                    tvDate.text = formattedDate

                    val colorResId = when (categoryName.lowercase().replace(" ", "_")) {
                        "uang_kaget"           -> R.color.income_uang_kaget
                        "gaji"                 -> R.color.income_gaji
                        "hadiah"               -> R.color.income_hadiah
                        "uang_saku"            -> R.color.income_uang_saku
                        "pendapatan_lainnya"   -> R.color.income_pendapatan_lainnya
                        else                   -> R.color.black
                    }
                    val bgDrawable = ContextCompat.getDrawable(this@IncomeActivity, R.drawable.bg_item_transaction)
                            as? android.graphics.drawable.GradientDrawable
                    val bgColorInt = ContextCompat.getColor(this@IncomeActivity, colorResId)
                    bgDrawable?.setColor(bgColorInt)
                    itemView.background = bgDrawable

                    llCategoryContainer.addView(itemView)

                    valuesList.add(totalPerCategory.toFloat())

                    val composeColor = when (categoryName.lowercase().replace(" ", "_")) {
                        "uang_kaget"           -> ComposeColor(0xFFFFD54F)
                        "gaji"                 -> ComposeColor(0xFF4CAF50)
                        "hadiah"               -> ComposeColor(0xFFBA68C8)
                        "uang_saku"            -> ComposeColor(0xFF7986CB)
                        "pendapatan_lainnya"   -> ComposeColor(0xFFB85486)
                        else                   -> ComposeColor.LightGray
                    }
                    colorsList.add(composeColor)
                }

                setupPieChart(valuesList, colorsList)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@IncomeActivity,
                    "Terjadi kesalahan: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupPieChart(values: List<Float>, colors: List<ComposeColor>) {
        findViewById<ComposeView>(R.id.composePieChart).setContent {
            PieChart(
                values = values,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                labelTextSize = 12.sp
            )
        }
    }

    private fun TextView.setStrokeText(
        text: String,
        strokeColor: Int,
        strokeWidth: Float,
        fillColor: Int
    ) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        val strokeSpan = object : CharacterStyle() {
            override fun updateDrawState(ds: TextPaint) {
                ds.style = Paint.Style.STROKE
                ds.strokeWidth = strokeWidth
                ds.color = strokeColor
                ds.isAntiAlias = true
            }
        }
        val fillSpan = object : CharacterStyle() {
            override fun updateDrawState(ds: TextPaint) {
                ds.style = Paint.Style.FILL
                ds.color = fillColor
                ds.isAntiAlias = true
            }
        }

        val strokeString = SpannableString(text).apply {
            setSpan(strokeSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val fillString = SpannableString(text).apply {
            setSpan(fillSpan, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        setText(strokeString, TextView.BufferType.SPANNABLE)
        post {
            setText(fillString, TextView.BufferType.SPANNABLE)
        }
    }

    @Composable
    fun PieChart(
        values: List<Float>,
        colors: List<ComposeColor>,
        modifier: Modifier = Modifier,
        labelTextSize: TextUnit = 14.sp
    ) {
        val total = values.sum().coerceAtLeast(1f)
        val sweepAngles = values.map { it / total * 360f }

        Canvas(modifier = modifier) {
            var startAngle = -90f
            sweepAngles.forEachIndexed { index, sweep ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }
    }
}
