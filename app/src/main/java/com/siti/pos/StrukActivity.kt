package com.siti.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.LayoutInflater
import android.widget.*
import androidx.cardview.widget.CardView
import Helper.DatabaseHelper
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class StrukActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var tvCabangStruk: TextView
    private lateinit var tvNomorStruk: TextView
    private lateinit var tvTanggalStruk: TextView
    private lateinit var tvPelangganStruk: TextView
    private lateinit var tvMetodeStruk: TextView
    private lateinit var llItemStruk: LinearLayout
    private lateinit var tvTotalStruk: TextView
    private lateinit var btnCetak: Button
    private lateinit var btnSelesai: Button
    private lateinit var cardStruk: CardView
    private lateinit var dbHelper: DatabaseHelper

    private val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_struk)

        dbHelper = DatabaseHelper(this)
        initViews()

        val transaksiId = intent.getStringExtra("transaksi_id") ?: ""
        loadStruk(transaksiId)
        setupListeners()
        cekCetakOtomatis()
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        tvCabangStruk = findViewById(R.id.tvCabangStruk)
        tvNomorStruk = findViewById(R.id.tvNomorStruk)
        tvTanggalStruk = findViewById(R.id.tvTanggalStruk)
        tvPelangganStruk = findViewById(R.id.tvPelangganStruk)
        tvMetodeStruk = findViewById(R.id.tvMetodeStruk)
        llItemStruk = findViewById(R.id.llItemStruk)
        tvTotalStruk = findViewById(R.id.tvTotalStruk)
        btnCetak = findViewById(R.id.btnCetak)
        btnSelesai = findViewById(R.id.btnSelesai)
        cardStruk = findViewById(R.id.cardStruk)
    }

    private fun loadStruk(transaksiId: String) {
        val items = dbHelper.getItemsByTransaksiId(transaksiId)
        val transaksiList = dbHelper.getAllTransaksi()
        val transaksi = transaksiList.find { it.id == transaksiId } ?: return

        tvCabangStruk.text = transaksi.cabangNama.ifEmpty { "Cabang Utama" }
        tvNomorStruk.text = transaksi.nomorTransaksi
        tvTanggalStruk.text = transaksi.tanggal
        tvPelangganStruk.text = transaksi.pelangganNama.ifEmpty { "Umum" }
        tvMetodeStruk.text = transaksi.metodePembayaran

        llItemStruk.removeAllViews()
        items.forEach { item ->
            val view = LayoutInflater.from(this)
                .inflate(android.R.layout.simple_list_item_2, llItemStruk, false)

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }
            }

            val tvItem = TextView(this).apply {
                text = "${item.namaProduk} x${item.jumlah}"
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvItemSubtotal = TextView(this).apply {
                text = formatter.format(item.subtotal).replace("Rp", "Rp ").replace(",00", "")
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
                gravity = android.view.Gravity.END
            }

            row.addView(tvItem)
            row.addView(tvItemSubtotal)
            llItemStruk.addView(row)
        }

        tvTotalStruk.text = formatter.format(transaksi.totalHarga)
            .replace("Rp", "Rp ").replace(",00", "")
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        btnCetak.setOnClickListener {
            val printerManager = PrinterManager(this)
            if (!printerManager.sudahDiatur()) {
                Toast.makeText(this, "Atur printer dulu di menu Printer", Toast.LENGTH_SHORT).show()
                // Arahkan ke PrinterActivity
                startActivity(Intent(this, PrinterActivity::class.java))
                return@setOnClickListener
            }
            cetakViaBluetooth()
        }

        btnSelesai.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
    }

    private fun cetakStruk() {
        val transaksiId = intent.getStringExtra("transaksi_id") ?: return
        val transaksiList = dbHelper.getAllTransaksi()
        val transaksi = transaksiList.find { it.id == transaksiId } ?: return
        val items = dbHelper.getItemsByTransaksiId(transaksiId)

        val itemsHtml = items.joinToString("") { item ->
            "<tr><td>${item.namaProduk} x${item.jumlah}</td>" +
                    "<td align='right'>${formatter.format(item.subtotal).replace("Rp", "Rp ").replace(",00", "")}</td></tr>"
        }

        val html = """
        <html><body style='font-family:monospace;padding:16px'>
        <h2 style='text-align:center'>POS Laundry</h2>
        <p style='text-align:center'>${transaksi.cabangNama}</p>
        <hr/>
        <p>No: ${transaksi.nomorTransaksi}</p>
        <p>Tanggal: ${transaksi.tanggal}</p>
        <p>Pelanggan: ${transaksi.pelangganNama.ifEmpty { "Umum" }}</p>
        <p>Pembayaran: ${transaksi.metodePembayaran}</p>
        <hr/>
        <table width='100%'>$itemsHtml</table>
        <hr/>
        <h3>Total: ${formatter.format(transaksi.totalHarga).replace("Rp", "Rp ").replace(",00", "")}</h3>
        <p style='text-align:center'>Terima Kasih!</p>
        </body></html>
    """.trimIndent()

        val webView = android.webkit.WebView(this)
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                val printManager = getSystemService(PRINT_SERVICE) as android.print.PrintManager
                // WebView yang menyediakan PrintDocumentAdapter — bukan abstract
                val printAdapter = webView.createPrintDocumentAdapter("Struk Transaksi")
                printManager.print("Struk Transaksi", printAdapter, null)
            }
        }
    }
    // Tambah di onCreate StrukActivity setelah loadStruk()
    private fun cekCetakOtomatis() {
        val printerManager = PrinterManager(this)
        if (printerManager.getSetting().cetakOtomatis &&
            printerManager.sudahDiatur()) {
            cetakViaBluetooth()
        }
    }

    private fun cetakViaBluetooth() {
        val setting = PrinterManager(this).getSetting()
        val transaksiId = intent.getStringExtra("transaksi_id") ?: return
        val transaksiList = dbHelper.getAllTransaksi()
        val transaksi = transaksiList.find { it.id == transaksiId } ?: return
        val items = dbHelper.getItemsByTransaksiId(transaksiId)
        val lebarKertas = if (setting.ukuranKertas == "58mm") 32 else 48
        val garis = "-".repeat(lebarKertas)

        Thread {
            try {
                val btManager = getSystemService(BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
                val btAdapter = btManager.adapter
                val device = btAdapter.getRemoteDevice(setting.alamatMac)
                val socket = device.createRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                )
                socket.connect()

                val teks = buildString {
                    appendLine("\u001B\u0061\u0001")
                    appendLine("POS Laundry")
                    appendLine(transaksi.cabangNama)
                    appendLine("\u001B\u0061\u0000")
                    appendLine(garis)
                    appendLine("No : ${transaksi.nomorTransaksi}")
                    appendLine("Tgl: ${transaksi.tanggal}")
                    appendLine("Plg: ${transaksi.pelangganNama.ifEmpty { "Umum" }}")
                    appendLine("Byr: ${transaksi.metodePembayaran}")
                    appendLine(garis)
                    items.forEach { item ->
                        appendLine("${item.namaProduk} x${item.jumlah}")
                        appendLine("  Rp ${item.subtotal}")
                    }
                    appendLine(garis)
                    appendLine("TOTAL: Rp ${transaksi.totalHarga}")
                    appendLine(garis)
                    appendLine("\u001B\u0061\u0001")
                    appendLine("Terima Kasih!")
                    appendLine("\n\n\n")
                }

                socket.outputStream.write(teks.toByteArray(Charsets.UTF_8))
                socket.outputStream.flush()
                socket.close()

                runOnUiThread {
                    Toast.makeText(this, "Struk dicetak!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Gagal cetak: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}