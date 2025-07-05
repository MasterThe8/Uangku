package pember.latihan.uangku.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pember.latihan.uangku.R
import pember.latihan.uangku.databinding.ItemSavingBinding
import pember.latihan.uangku.model.Saving

import java.text.DecimalFormat

class SavingAdapter(
    private val savings: List<Saving>,
    private val onEditClick: (Saving) -> Unit
) : RecyclerView.Adapter<SavingAdapter.SavingViewHolder>() {

    private val numberFormat = DecimalFormat("###,###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingViewHolder {
        val binding = ItemSavingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavingViewHolder, position: Int) {
        val saving = savings[position]
        holder.bind(saving)
    }

    override fun getItemCount(): Int = savings.size

    inner class SavingViewHolder(private val binding: ItemSavingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(saving: Saving) {
            val remaining = saving.targetAmount - saving.currentAmount

            val formattedTargetAmount = numberFormat.format(saving.targetAmount)
            val formattedCurrentAmount = numberFormat.format(saving.currentAmount)
            val formattedRemainingAmount = numberFormat.format(remaining)

            binding.tvTitle.text = saving.title
            binding.tvTargetAmount.text = "Rp$formattedTargetAmount"
            binding.tvCurrentAmount.text = "Rp$formattedCurrentAmount"
            binding.tvRemainingAmount.text = "Rp$formattedRemainingAmount"

            val progress = (saving.currentAmount / saving.targetAmount * 100).toInt()
            binding.progressBar.progress = progress

            binding.root.setOnClickListener { onEditClick(saving) }
        }
    }
}