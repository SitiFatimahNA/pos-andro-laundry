package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import model.ModelCabang
import com.siti.pos.R


class CabangAdapter (
    private val list: MutableList<ModelCabang>,
    private val onItemClick: (ModelCabang) -> Unit
) : RecyclerView.Adapter<CabangAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cabang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cabang = list[position]

        holder.tvNama.text = cabang.nama
        holder.tvAlamat.text = cabang.alamat.ifEmpty { "-" }
        holder.tvNoTelp.text = cabang.noTelp.ifEmpty { "-" }

        if (cabang.isAktif) {
            holder.chipStatus.text = "Aktif"
            holder.chipStatus.setChipBackgroundColorResource(R.color.green_light)
        } else {
            holder.chipStatus.text = "Nonaktif"
            holder.chipStatus.setChipBackgroundColorResource(R.color.red_light)
        }

        holder.itemView.setOnClickListener { onItemClick(cabang) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ModelCabang>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaCabang)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamat)
        val tvNoTelp: TextView = itemView.findViewById(R.id.tvNoTelp)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
    }
}