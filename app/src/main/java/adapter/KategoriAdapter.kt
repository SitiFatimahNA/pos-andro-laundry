package adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.siti.pos.R
import com.siti.pos.ModelKategori

class KategoriAdapter(
    private var list: MutableList<ModelKategori>,
    private val onItemClick: (ModelKategori) -> Unit,
) : RecyclerView.Adapter<KategoriAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context

        holder.tvKategori.text = item.namaKategori
        holder.chipStatus.text = if (item.statusKategori == "Aktif") "Aktif" else "Nonaktif"

        if (item.statusKategori == "Aktif") {
            holder.chipStatus.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_active_bg))
            holder.chipStatus.setTextColor(
                ContextCompat.getColor(context, R.color.status_active_text))
            holder.chipStatus.chipIcon =
                ContextCompat.getDrawable(context, R.drawable.ic_check)
            holder.chipStatus.chipIconTint =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_active_text))
        } else {
            holder.chipStatus.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_inactive_bg))
            holder.chipStatus.setTextColor(
                ContextCompat.getColor(context, R.color.status_inactive_text))
            holder.chipStatus.chipIcon =
                ContextCompat.getDrawable(context, R.drawable.ic_cancel)
            holder.chipStatus.chipIconTint =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_inactive_text))
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: MutableList<ModelKategori>) {
        list = newList
        notifyDataSetChanged()
    }
}