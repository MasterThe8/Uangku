package pember.latihan.uangku.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import pember.latihan.uangku.R
import pember.latihan.uangku.data.AppDatabase
import pember.latihan.uangku.databinding.ActivitySavingBinding
import pember.latihan.uangku.model.Saving
import pember.latihan.uangku.service.SavingService
import pember.latihan.uangku.utils.CompleteAdapter
import pember.latihan.uangku.utils.SavingAdapter
import java.text.DecimalFormat

class SavingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySavingBinding
    private lateinit var savingService: SavingService
    private lateinit var inProgressAdapter: SavingAdapter
    private lateinit var completeAdapter: CompleteAdapter
    private var inProgressList: List<Saving> = emptyList()
    private var completeList: List<Saving> = emptyList()
    private val numberFormat = DecimalFormat("###,###,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savingService = SavingService(applicationContext)

        loadAndSetupAdapters()

        binding.addSavingBtn.setOnClickListener {
            showAddSavingDialog()
        }

        binding.tabInProgress.setOnClickListener {
            showInProgressTab()
        }
        binding.tabComplete.setOnClickListener {
            showCompleteTab()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadAndSetupAdapters() {
        lifecycleScope.launch {
            val allSavings: List<Saving> = savingService.getSavings()

            inProgressList = allSavings.filter { it.currentAmount < it.targetAmount }
            completeList   = allSavings.filter { it.currentAmount >= it.targetAmount }

            inProgressAdapter = SavingAdapter(inProgressList) { selected ->
                showEditDialog(selected)
            }
            completeAdapter = CompleteAdapter(completeList) { saving ->
                Toast.makeText(this@SavingActivity, "Tabungan sudah tercapai dan tidak bisa diubah", Toast.LENGTH_SHORT).show()
            }

            binding.rvSavingList.layoutManager = LinearLayoutManager(this@SavingActivity)

            showInProgressTab()
        }
    }

    private fun showInProgressTab() {
        binding.rvSavingList.adapter = inProgressAdapter

        val totalTargetInProgress: Double = inProgressList.sumOf { it.targetAmount }
        val formattedTotal = numberFormat.format(totalTargetInProgress)
        binding.tvTotalTarget.text = "Rp$formattedTotal"
        binding.tvTotalTargetText.visibility = View.VISIBLE
        binding.tvTotalTarget.visibility = View.VISIBLE
        activateTab(binding.tabInProgress, binding.tabComplete)
    }

    private fun showCompleteTab() {
        binding.rvSavingList.adapter = completeAdapter
        binding.tvTotalTargetText.visibility = View.GONE
        binding.tvTotalTarget.visibility = View.GONE
        activateTab(binding.tabComplete, binding.tabInProgress)
    }

    private fun activateTab(tabActive: TextView, tabInactive: TextView) {
        tabActive.apply {
            setTextColor(Color.parseColor("#2A74D2"))
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.underline_blue)
        }
        tabInactive.apply {
            setTextColor(Color.parseColor("#6BA1A3"))
            setTypeface(null, Typeface.NORMAL)
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun showAddSavingDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_saving, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.etTitle)
        val targetAmountEditText = dialogView.findViewById<EditText>(R.id.etTargetAmount)
        val currentAmountEditText = dialogView.findViewById<EditText>(R.id.etCurrentAmount)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tambahkan Tabungan")
            .setView(dialogView)
            .setPositiveButton("Tambah") { _, _ ->
                val title = titleEditText.text.toString()
                val targetAmount = targetAmountEditText.text.toString().toDoubleOrNull()
                val currentAmount = currentAmountEditText.text.toString().toDoubleOrNull()

                if (title.isBlank() || targetAmount == null || currentAmount == null) {
                    Toast.makeText(this, "Harap isi semua field dengan benar", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                addNewSaving(title, targetAmount, currentAmount)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun addNewSaving(title: String, targetAmount: Double, currentAmount: Double) {
        lifecycleScope.launch {
            try {
                val userId = savingService.getCurrentUserId()
                val saving = Saving(
                    userId = userId,
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount
                )
                savingService.insertSaving(saving)
                Toast.makeText(this@SavingActivity, "Tabungan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                // Reload ulang daftar dan adapter setelah nambah
                loadAndSetupAdapters()
            } catch (e: Exception) {
                Toast.makeText(this@SavingActivity, "Gagal menambahkan tabungan", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showEditDialog(saving: Saving) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Tabungan: ${saving.title}")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Jumlah tabungan yang ditambah/dikurangi"

        builder.setView(input)
        builder.setPositiveButton("Tambah") { _, _ ->
            val amount = input.text.toString().toDoubleOrNull()
            if (amount != null) {
                updateSavingAmount(saving, amount)
            }
        }
        builder.setNegativeButton("Kurang") { _, _ ->
            val amount = input.text.toString().toDoubleOrNull()
            if (amount != null) {
                updateSavingAmount(saving, -amount)
            }
        }
        builder.setNeutralButton("Batal", null)
        builder.show()
    }

    private fun updateSavingAmount(saving: Saving, delta: Double) {
        val newAmount = (saving.currentAmount + delta).coerceAtLeast(0.0)
        lifecycleScope.launch {
            savingService.updateCurrentAmountByTitle(saving.title, newAmount)
            Toast.makeText(this@SavingActivity, "Tabungan diupdate", Toast.LENGTH_SHORT).show()
            loadAndSetupAdapters()
        }
    }
}