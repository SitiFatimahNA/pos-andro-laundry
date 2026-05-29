package com.siti.pos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import Helper.DatabaseHelper
import java.io.File
import java.io.FileOutputStream

class ModPegawaiActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var ivFoto: ImageView
    private lateinit var ivEditFoto: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var etNama: TextInputEditText
    private lateinit var etJabatan: TextInputEditText
    private lateinit var etNoHp: TextInputEditText
    private lateinit var btnSimpan: Button
    private lateinit var dbHelper: DatabaseHelper

    private var mode: String = "tambah"
    private var pegawaiId: String = ""
    private var selectedImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            ivFoto.setImageURI(selectedImageUri)
            ivFoto.setPadding(0, 0, 0, 0)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                ivFoto.setImageBitmap(it)
                ivFoto.setPadding(0, 0, 0, 0)
                selectedImageUri = saveBitmapToUri(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_pegawai)

        dbHelper = DatabaseHelper(this)
        initViews()
        checkIntentMode()
        setupListeners()
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        ivFoto = findViewById(R.id.ivFoto)
        ivEditFoto = findViewById(R.id.ivEditFoto)
        tvJudul = findViewById(R.id.tvJudul)
        etNama = findViewById(R.id.etNama)
        etJabatan = findViewById(R.id.etJabatan)
        etNoHp = findViewById(R.id.etNoHp)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun checkIntentMode() {
        mode = intent.getStringExtra("mode") ?: "tambah"
        pegawaiId = intent.getStringExtra("id") ?: ""

        if (mode == "edit") {
            tvJudul.text = "Edit Pegawai"
            btnSimpan.text = "Update"

            etNama.setText(intent.getStringExtra("nama") ?: "")
            etJabatan.setText(intent.getStringExtra("jabatan") ?: "")
            etNoHp.setText(intent.getStringExtra("no_hp") ?: "")

            val fotoPath = intent.getStringExtra("foto") ?: ""
            if (fotoPath.isNotEmpty()) {
                val file = File(fotoPath)
                if (file.exists()) {
                    ivFoto.setImageBitmap(android.graphics.BitmapFactory.decodeFile(fotoPath))
                    ivFoto.setPadding(0, 0, 0, 0)
                    selectedImageUri = Uri.fromFile(file)
                }
            }
        }
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        ivEditFoto.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Pilih Foto")
                .setItems(arrayOf("Kamera", "Galeri")) { _, which ->
                    if (which == 0) {
                        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                    } else {
                        galleryLauncher.launch(
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        )
                    }
                }
                .show()
        }

        btnSimpan.setOnClickListener {
            if (validateInput()) simpanPegawai()
        }
    }

    private fun validateInput(): Boolean {
        val nama = etNama.text.toString().trim()
        val jabatan = etJabatan.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return false
        }
        if (jabatan.isEmpty()) {
            etJabatan.error = "Jabatan tidak boleh kosong"
            return false
        }
        return true
    }

    private fun simpanPegawai() {
        val nama = etNama.text.toString().trim()
        val jabatan = etJabatan.text.toString().trim()
        val noHp = etNoHp.text.toString().trim()
        val fotoPath = copyFotoToInternal()

        if (mode == "edit") {
            val result = dbHelper.updatePegawai(
                id = pegawaiId,
                nama = nama,
                jabatan = jabatan,
                noHp = noHp,
                foto = fotoPath.ifEmpty { intent.getStringExtra("foto") ?: "" }
            )
            if (result > 0) {
                Toast.makeText(this, "Pegawai berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal mengupdate pegawai", Toast.LENGTH_SHORT).show()
            }
        } else {
            val result = dbHelper.insertPegawai(nama, jabatan, noHp, fotoPath)
            if (result > 0) {
                Toast.makeText(this, "Pegawai berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal menyimpan pegawai", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyFotoToInternal(): String {
        selectedImageUri ?: return ""
        return try {
            val inputStream = contentResolver.openInputStream(selectedImageUri!!) ?: return ""
            val folder = File(filesDir, "pegawai_foto")
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
            val folder = File(filesDir, "pegawai_foto")
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
}
