package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import model.ModelPelanggan
import com.siti.pos.R

class PelangganAdapter(
    private val list: MutableList<ModelPelanggan>,
    private val onItemClick: (ModelPelanggan) -> Unit
) : RecyclerView.Adapter<PelangganAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pelanggan = list[position]

        // Inisial dari nama
        holder.tvInisial.text = pelanggan.nama
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }

        holder.tvNama.text = pelanggan.nama
        holder.tvNoHp.text = pelanggan.noHp.ifEmpty { "-" }
        holder.tvAlamat.text = pelanggan.alamat.ifEmpty { "-" }

        holder.itemView.setOnClickListener { onItemClick(pelanggan) }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<ModelPelanggan>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInisial: TextView = itemView.findViewById(R.id.tvInisial)
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPelanggan)
        val tvNoHp: TextView = itemView.findViewById(R.id.tvNoHpPelanggan)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatPelanggan)
    }
}