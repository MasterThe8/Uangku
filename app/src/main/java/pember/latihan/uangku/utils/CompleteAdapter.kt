package pember.latihan.uangku.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pember.latihan.uangku.databinding.ItemSavingCompleteBinding
import pember.latihan.uangku.model.Saving
import java.text.DecimalFormat

class CompleteAdapter(
    private val completeSavings: List<Saving>,
    private val onItemClick: (Saving) -> Unit
) : RecyclerView.Adapter<CompleteAdapter.CompleteViewHolder>() {

    private val numberFormat = DecimalFormat("###,###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompleteViewHolder {
        val binding = ItemSavingCompleteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CompleteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompleteViewHolder, position: Int) {
        val saving = completeSavings[position]
        holder.bind(saving)
    }

    override fun getItemCount(): Int = completeSavings.size

    inner class CompleteViewHolder(
        private val binding: ItemSavingCompleteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(saving: Saving) {
            // Hanya perlu menampilkan title + targetAmount
            val formattedTarget = numberFormat.format(saving.targetAmount)
            binding.tvTitleComplete.text = saving.title
            binding.tvTargetAmountComplete.text = "Rp$formattedTarget"

            // Klik baris complete juga bisa dibuka dialog edit jika diinginkan
            binding.root.setOnClickListener { onItemClick(saving) }
        }
    }
}