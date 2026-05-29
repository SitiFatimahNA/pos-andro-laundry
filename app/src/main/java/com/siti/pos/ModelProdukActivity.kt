package com.siti.pos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import Helper.DatabaseHelper
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream

class ModelProdukActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var ivProductPhoto: ImageView
    private lateinit var btnKamera: CardView
    private lateinit var btnGaleri: CardView
    private lateinit var etNamaProduk: TextInputEditText
    private lateinit var etHarga: TextInputEditText
    private lateinit var etStok: TextInputEditText
    private lateinit var btnPilihKategori: Button
    private lateinit var btnPilihCabang: Button
    private lateinit var cbStokTakTerbatas: CheckBox
    private lateinit var btnSimpan: Button
    private lateinit var tvJudul: TextView
    private lateinit var dbHelper: DatabaseHelper

    private var mode: String = "tambah"
    private var produkId: String = ""
    private var selectedImageUri: Uri? = null
    private var selectedKategoriId: String = ""
    private var selectedKategoriNama: String = ""
    private var selectedCabang: String = ""

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            ivProductPhoto.setImageURI(selectedImageUri)
            ivProductPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                ivProductPhoto.setImageBitmap(it)
                ivProductPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                selectedImageUri = saveBitmapToUri(it)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        when {
            cameraGranted && storageGranted -> showPilihFotoDialog()
            !cameraGranted -> Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            !storageGranted -> Toast.makeText(this, "Izin galeri ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_produk)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)
        initViews()
        setupListeners()
        checkIntentMode()
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        ivProductPhoto = findViewById(R.id.ivProductPhoto)
        btnKamera = findViewById(R.id.btnKamera)
        btnGaleri = findViewById(R.id.btnGaleri)
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etHarga = findViewById(R.id.etHarga)
        etStok = findViewById(R.id.etStok)
        btnPilihKategori = findViewById(R.id.btnPilihKategori)
        btnPilihCabang = findViewById(R.id.btnPilihCabang)
        cbStokTakTerbatas = findViewById(R.id.cbStokTakTerbatas)
        btnSimpan = findViewById(R.id.btnSimpan)
        tvJudul = findViewById(R.id.tvJudul)
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        btnKamera.setOnClickListener { checkPermissionThenOpen("kamera") }
        btnGaleri.setOnClickListener { checkPermissionThenOpen("galeri") }

        cbStokTakTerbatas.setOnCheckedChangeListener { _, isChecked ->
            etStok.isEnabled = !isChecked
            if (isChecked) etStok.setText("")
        }

        btnPilihKategori.setOnClickListener {
            val listKategori = dbHelper.getAllKategori()
            if (listKategori.isEmpty()) {
                Toast.makeText(this, "Belum ada kategori", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val namaList = listKategori.map { it.namaKategori ?: "-" }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Pilih Kategori")
                .setItems(namaList) { _, which ->
                    selectedKategoriId = listKategori[which].idKategori ?: ""
                    selectedKategoriNama = listKategori[which].namaKategori ?: ""
                    btnPilihKategori.text = selectedKategoriNama
                }
                .show()
        }

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

        btnSimpan.setOnClickListener {
            if (validateInput()) simpanProduk()
        }
    }

    private fun validateInput(): Boolean {
        val nama = etNamaProduk.text.toString().trim()
        val harga = etHarga.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaProduk.error = "Nama produk tidak boleh kosong"
            return false
        }
        if (harga.isEmpty()) {
            etHarga.error = "Harga tidak boleh kosong"
            return false
        }
        if (selectedKategoriId.isEmpty()) {
            Toast.makeText(this, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedCabang.isEmpty()) {
            Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!cbStokTakTerbatas.isChecked && etStok.text.toString().trim().isEmpty()) {
            etStok.error = "Stok tidak boleh kosong"
            return false
        }
        return true
    }

    private fun simpanProduk() {
        val nama = etNamaProduk.text.toString().trim()
        val harga = etHarga.text.toString().trim()
        val stok = if (cbStokTakTerbatas.isChecked) -1
        else etStok.text.toString().trim().toIntOrNull() ?: 0
        val fotoPath = copyFotoToInternal()

        if (mode == "edit") {
            val foto = fotoPath.ifEmpty { intent.getStringExtra("foto") ?: "" }
            val result = dbHelper.updateProduk(
                id = produkId,
                nama = nama,
                harga = harga,
                kategoriId = selectedKategoriId,
                kategoriNama = selectedKategoriNama,
                stok = stok,
                cabang = selectedCabang,
                foto = foto
            )
            if (result > 0) {
                Toast.makeText(this, "Produk berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal mengupdate produk", Toast.LENGTH_SHORT).show()
            }
        } else {
            val result = dbHelper.insertProduk(
                nama = nama,
                harga = harga,
                kategoriId = selectedKategoriId,
                kategoriNama = selectedKategoriNama,
                stok = stok,
                cabang = selectedCabang,
                foto = fotoPath
            )
            if (result > 0) {
                Toast.makeText(this, "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal menyimpan produk", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyFotoToInternal(): String {
        selectedImageUri ?: return ""
        return try {
            val inputStream = contentResolver.openInputStream(selectedImageUri!!) ?: return ""
            val folder = File(filesDir, "produk_foto")
            if (!folder.exists()) folder.mkdirs()
            val file = File(folder, "foto_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun saveBitmapToUri(bitmap: Bitmap): Uri? {
        return try {
            val folder = File(filesDir, "produk_foto")
            if (!folder.exists()) folder.mkdirs()
            val file = File(folder, "kamera_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkIntentMode() {
        mode = intent.getStringExtra("mode") ?: "tambah"
        produkId = intent.getStringExtra("produk_id") ?: ""

        if (mode == "edit") {
            tvJudul.text = "Edit Produk"
            btnSimpan.text = "Update Produk"

            etNamaProduk.setText(intent.getStringExtra("nama") ?: "")
            etHarga.setText(intent.getStringExtra("harga") ?: "")
            etStok.setText(intent.getIntExtra("stok", 0).let {
                if (it == -1) { cbStokTakTerbatas.isChecked = true; "" } else it.toString()
            })

            selectedKategoriId = intent.getStringExtra("kategoriId") ?: ""
            selectedKategoriNama = intent.getStringExtra("kategoriNama") ?: ""
            if (selectedKategoriNama.isNotEmpty()) btnPilihKategori.text = selectedKategoriNama

            selectedCabang = intent.getStringExtra("cabang") ?: ""
            if (selectedCabang.isNotEmpty()) btnPilihCabang.text = selectedCabang

            val fotoPath = intent.getStringExtra("foto") ?: ""
            if (fotoPath.isNotEmpty()) {
                val file = File(fotoPath)
                if (file.exists()) {
                    ivProductPhoto.setImageBitmap(BitmapFactory.decodeFile(fotoPath))
                    ivProductPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                    selectedImageUri = Uri.fromFile(file)
                }
            }
        }
    }

    private fun checkPermissionThenOpen(target: String) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            if (target == "kamera") bukaKamera() else bukaGaleri()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun showPilihFotoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Pilih Foto")
            .setItems(arrayOf("Kamera", "Galeri")) { _, which ->
                if (which == 0) bukaKamera() else bukaGaleri()
            }
            .show()
    }

    private fun bukaKamera() {
        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    private fun bukaGaleri() {
        galleryLauncher.launch(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )
    }
}