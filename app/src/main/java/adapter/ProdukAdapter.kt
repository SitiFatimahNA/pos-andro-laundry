package adapter

import com.siti.pos.R
import produk.Produk
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class ProdukAdapter(
    private val list: MutableList<Produk>,
    private val onItemClick: (Produk) -> Unit  // ← tambah callback
) : RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_produk_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = list[position]

        val foto = produk.foto
        if (foto.isNotEmpty()) {
            val file = java.io.File(foto)
            if (file.exists()) {
                holder.ivProduk.setImageBitmap(android.graphics.BitmapFactory.decodeFile(foto))
            } else {
                holder.ivProduk.setImageResource(R.drawable.ic_camera) // placeholder
            }
        } else {
            holder.ivProduk.setImageResource(R.drawable.ic_camera) // placeholder
        }

        holder.tvNama.text = produk.nama
        holder.tvHarga.text = produk.harga
        holder.tvKategori.text = produk.kategori
        holder.tvStok.text = "Stok: ${if (produk.stok == -1) "∞" else produk.stok.toString()}"
        holder.tvCabang.text = produk.cabang

        // Menyesuaikan logika status aktif/nonaktif pada Chip
        if (produk.isAktif) {
            holder.btnAktif.text = "Aktif"
            holder.btnAktif.setChipIconResource(R.drawable.ic_check)
            holder.btnAktif.setChipBackgroundColorResource(R.color.green_light) // Pastikan warna ini terdaftar di colors.xml
        } else {
            holder.btnAktif.text = "Nonaktif"
            holder.btnAktif.setChipIconResource(R.drawable.ic_cancel)
            holder.btnAktif.setChipBackgroundColorResource(R.color.red_light)  // Pastikan warna ini terdaftar di colors.xml
        }

        holder.itemView.setOnClickListener {
            onItemClick(produk)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Produk>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduk: ImageView = itemView.findViewById(R.id.img_produk)
        val tvNama: TextView = itemView.findViewById(R.id.tv_nama_produk)
        val tvHarga: TextView = itemView.findViewById(R.id.tv_harga_produk)

        // SUDAH DISESUAIKAN: Mengikuti ID XML asli kamu yaitu R.id.tv_Kategori
        val tvKategori: TextView = itemView.findViewById(R.id.tv_Kategori)

        val tvStok: TextView = itemView.findViewById(R.id.tv_stok)
        val tvCabang: TextView = itemView.findViewById(R.id.tv_cabang)
        val btnAktif: Chip = itemView.findViewById(R.id.chip_status)
    }
}