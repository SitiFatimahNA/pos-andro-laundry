package adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import model.ModelPegawai
import com.siti.pos.R
import java.io.File

class PegawaiAdapter(
    private val list: MutableList<ModelPegawai>,
    private val onItemClick: (ModelPegawai) -> Unit
) : RecyclerView.Adapter<PegawaiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pegawai = list[position]

        holder.tvNama.text = pegawai.nama
        holder.tvJabatan.text = pegawai.jabatan
        holder.tvNoHp.text = pegawai.noHp.ifEmpty { "-" }

        // Foto
        if (pegawai.foto.isNotEmpty()) {
            val file = File(pegawai.foto)
            if (file.exists()) {
                holder.ivFoto.setImageBitmap(BitmapFactory.decodeFile(pegawai.foto))
                holder.ivFoto.setPadding(0, 0, 0, 0)
            }
        }

        // Status
        if (pegawai.isAktif) {
            holder.chipStatus.text = "Aktif"
            holder.chipStatus.setChipBackgroundColorResource(R.color.green_light)
        } else {
            holder.chipStatus.text = "Nonaktif"
            holder.chipStatus.setChipBackgroundColorResource(R.color.red_light)
        }

        holder.itemView.setOnClickListener { onItemClick(pegawai) }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ModelPegawai>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFoto: ImageView = itemView.findViewById(R.id.ivFotoPegawai)
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaPegawai)
        val tvJabatan: TextView = itemView.findViewById(R.id.tvJabatan)
        val tvNoHp: TextView = itemView.findViewById(R.id.tvNoHp)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
    }
}