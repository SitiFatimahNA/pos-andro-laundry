package com.siti.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import Helper.DatabaseHelper
import androidx.cardview.widget.CardView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import model.ModelItemTransaksi
import model.ModelTransaksi
import com.siti.pos.databinding.ActivityTransaksiBinding



class TransaksiActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var ivRiwayat: ImageView
    private lateinit var tvNomorTransaksi: TextView
    private lateinit var btnPilihPelanggan: Chip
    private lateinit var btnPilihCabang: Chip
    private lateinit var btnTambahProduk: Chip
    private lateinit var btnTunai: Button
    private lateinit var btnTransfer: Button
    private lateinit var btnQris: Button
    private lateinit var etCatatan: TextInputEditText
    private lateinit var llItemProduk: LinearLayout
    private lateinit var tvKosong: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnBayar: Button
    private lateinit var dbHelper: DatabaseHelper

    private var selectedPelangganId: String = ""
    private var selectedPelangganNama: String = ""
    private var selectedCabang: String = ""
    private var metodePembayaran: String = "Tunai"
    private val itemList: MutableList<ModelItemTransaksi> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)

        dbHelper = DatabaseHelper(this)
        initViews()
        setupListeners()
        tvNomorTransaksi.text = dbHelper.generateNomorTransaksi()
        setMetodePembayaran("Tunai")
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        ivRiwayat = findViewById(R.id.ivRiwayat)
        tvNomorTransaksi = findViewById(R.id.tvNomorTransaksi)
        btnPilihPelanggan = findViewById(R.id.btnPilihPelanggan)
        btnPilihCabang = findViewById(R.id.btnPilihCabang)
        btnTambahProduk = findViewById(R.id.btnTambahProduk)
        btnTunai = findViewById(R.id.btnTunai)
        btnTransfer = findViewById(R.id.btnTransfer)
        btnQris = findViewById(R.id.btnQris)
        etCatatan = findViewById(R.id.etCatatan)
        llItemProduk = findViewById(R.id.llItemProduk)
        tvKosong = findViewById(R.id.tvKosong)
        tvTotal = findViewById(R.id.tvTotal)
        btnBayar = findViewById(R.id.btnBayar)
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        ivRiwayat.setOnClickListener {
            startActivity(Intent(this, RiwayatTransaksiActivity::class.java))
        }

        // Pilih Pelanggan
        btnPilihPelanggan.setOnClickListener {
            val listPelanggan = dbHelper.getAllPelanggan()
            val options = mutableListOf("-- Tanpa Pelanggan --")
            options.addAll(listPelanggan.map { it.nama })

            AlertDialog.Builder(this)
                .setTitle("Pilih Pelanggan")
                .setItems(options.toTypedArray()) { _, which ->
                    if (which == 0) {
                        selectedPelangganId = ""
                        selectedPelangganNama = ""
                        btnPilihPelanggan.text = "Pilih Pelanggan"
                    } else {
                        val pelanggan = listPelanggan[which - 1]
                        selectedPelangganId = pelanggan.id
                        selectedPelangganNama = pelanggan.nama
                        btnPilihPelanggan.text = pelanggan.nama
                    }
                }
                .show()
        }

        // Pilih Cabang
        btnPilihCabang.setOnClickListener {
            val listCabang = dbHelper.getAllNamaCabang()
            if (listCabang.isEmpty()) {
                Toast.makeText(this, "Belum ada cabang", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Pilih Cabang")
                .setItems(listCabang.toTypedArray()) { _, which ->
                    selectedCabang = listCabang[which]
                    btnPilihCabang.text = selectedCabang
                }
                .show()
        }

        // Tambah Produk
        btnTambahProduk.setOnClickListener {
            val listProduk = dbHelper.getAllProduk()
            if (listProduk.isEmpty()) {
                Toast.makeText(this, "Belum ada produk", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val namaList = listProduk.map { "${it.nama} - Rp ${it.harga}" }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Pilih Produk")
                .setItems(namaList) { _, which ->
                    val produk = listProduk[which]
                    val harga = produk.harga.toLongOrNull() ?: 0L
                    val existing = itemList.find { it.produkId == produk.id }
                    if (existing != null) {
                        val idx = itemList.indexOf(existing)
                        val updated = existing.copy(
                            jumlah = existing.jumlah + 1,
                            subtotal = (existing.jumlah + 1) * harga
                        )
                        itemList[idx] = updated
                    } else {
                        itemList.add(
                            ModelItemTransaksi(
                                produkId = produk.id,
                                namaProduk = produk.nama,
                                harga = harga,
                                jumlah = 1,
                                subtotal = harga,
                                foto = produk.foto
                            )
                        )
                    }
                    renderItemProduk()
                }
                .show()
        }

        // Metode Pembayaran
        btnTunai.setOnClickListener { setMetodePembayaran("Tunai") }
        btnTransfer.setOnClickListener { setMetodePembayaran("Transfer") }
        btnQris.setOnClickListener { setMetodePembayaran("QRIS") }

        // Bayar
        btnBayar.setOnClickListener {
            if (validateInput()) showKonfirmasiBayar()
        }
    }

    private fun setMetodePembayaran(metode: String) {
        metodePembayaran = metode

        listOf(btnTunai, btnTransfer, btnQris).forEach { btn ->
            (btn as com.google.android.material.button.MaterialButton).apply {
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.TRANSPARENT
                )
                strokeColor = android.content.res.ColorStateList.valueOf(
                    resources.getColor(R.color.border_default, theme)
                )
            }
        }

        val btnAktif = when (metode) {
            "Tunai"    -> btnTunai
            "Transfer" -> btnTransfer
            "QRIS"     -> btnQris
            else       -> null
        }

        (btnAktif as? com.google.android.material.button.MaterialButton)?.apply {
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.accent_gold_bg, theme)
            )
            strokeColor = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.accent_gold, theme)
            )
        }
    }

    private fun renderItemProduk() {
        llItemProduk.removeAllViews()
        tvKosong.visibility = if (itemList.isEmpty()) View.VISIBLE else View.GONE

        itemList.forEachIndexed { index, item ->
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_transaksi_produk, llItemProduk, false)
            val ivFoto = view.findViewById<ImageView>(R.id.ivFotoProduk)

            if (item.foto.isNotEmpty()) {
                val file = java.io.File(item.foto)

                if (file.exists()) {
                    ivFoto.setImageBitmap(
                        android.graphics.BitmapFactory.decodeFile(item.foto)
                    )
                } else {
                    ivFoto.setImageResource(R.drawable.ic_camera)
                }
            } else {
                ivFoto.setImageResource(R.drawable.ic_camera)
            }

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            view.findViewById<TextView>(R.id.tvNamaProduk).text = item.namaProduk
            view.findViewById<TextView>(R.id.tvHargaProduk).text =
                formatter.format(item.harga).replace("Rp", "Rp ").replace(",00", "")
            view.findViewById<TextView>(R.id.tvJumlah).text = item.jumlah.toString()
            view.findViewById<TextView>(R.id.tvSubtotal).text =
                formatter.format(item.subtotal).replace("Rp", "Rp ").replace(",00", "")

            view.findViewById<ImageView>(R.id.btnTambah).setOnClickListener {
                val updated = item.copy(
                    jumlah = item.jumlah + 1,
                    subtotal = (item.jumlah + 1) * item.harga
                )
                itemList[index] = updated
                renderItemProduk()
            }

            view.findViewById<ImageView>(R.id.btnKurang).setOnClickListener {
                if (item.jumlah > 1) {
                    val updated = item.copy(
                        jumlah = item.jumlah - 1,
                        subtotal = (item.jumlah - 1) * item.harga
                    )
                    itemList[index] = updated
                } else {
                    itemList.removeAt(index)
                }
                renderItemProduk()
            }

            llItemProduk.addView(view)
        }

        // Update total
        val total = itemList.sumOf { it.subtotal }
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        tvTotal.text = formatter.format(total).replace("Rp", "Rp ").replace(",00", "")
    }

    private fun validateInput(): Boolean {
        if (itemList.isEmpty()) {
            Toast.makeText(this, "Pilih minimal satu produk", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedCabang.isEmpty()) {
            Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showKonfirmasiBayar() {
        val total = itemList.sumOf { it.subtotal }
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val totalStr = formatter.format(total).replace("Rp", "Rp ").replace(",00", "")

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembayaran")
            .setMessage("Total: $totalStr\nMetode: $metodePembayaran\n\nProses transaksi?")
            .setPositiveButton("Bayar") { _, _ -> simpanTransaksi() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun simpanTransaksi() {
        val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        val nomorTransaksi = tvNomorTransaksi.text.toString()
        val total = itemList.sumOf { it.subtotal }

        val transaksi = ModelTransaksi(
            nomorTransaksi = nomorTransaksi,
            tanggal = tanggal,
            pelangganId = selectedPelangganId,
            pelangganNama = selectedPelangganNama,
            cabangNama = selectedCabang,
            metodePembayaran = metodePembayaran,
            catatan = etCatatan.text.toString().trim(),
            totalHarga = total,
            status = "Selesai",
            items = itemList.toList()
        )

        val result = dbHelper.insertTransaksi(transaksi)
        if (result > 0) {
            Toast.makeText(this, "Transaksi berhasil!", Toast.LENGTH_SHORT).show()
            // Tampilkan struk
            val intent = Intent(this, StrukActivity::class.java)
            intent.putExtra("transaksi_id", result.toString())
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
        }
    }
}