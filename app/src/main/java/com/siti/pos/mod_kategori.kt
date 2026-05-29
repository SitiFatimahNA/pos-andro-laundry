package com.siti.pos

import Helper.DatabaseHelper
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class mod_kategori : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var judul: TextView
    private lateinit var inputNama: TextInputLayout
    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var autoStatus: AutoCompleteTextView
    private lateinit var btnSimpan: Button
    private lateinit var dbHelper: DatabaseHelper

    private var mode: String = "tambah"
    private var kategoriId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_kategori)

        dbHelper = DatabaseHelper(this)
        kategoriId = intent.getStringExtra("kategori_id") ?: ""

        initViews()
        setupDropdownStatus()
        checkIntentMode()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        judul = findViewById(R.id.judul)
        inputNama = findViewById(R.id.inputNama)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        autoStatus = findViewById(R.id.autoStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun setupDropdownStatus() {
        val statusList = listOf("Aktif", "Tidak Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusList)
        autoStatus.setAdapter(adapter)
    }

    private fun checkIntentMode() {
        mode = intent.getStringExtra("mode") ?: "tambah"
        kategoriId = intent.getStringExtra("kategori_id") ?: ""

        if (mode == "edit") {
            judul.text = "Edit Kategori"
            btnSimpan.text = "Update"
            etNamaKategori.setText(intent.getStringExtra("namaKategori") ?: "")
            autoStatus.setText(intent.getStringExtra("statusKategori") ?: "Aktif", false)
        } else {
            judul.text = "Tambah Kategori"
            btnSimpan.text = "Simpan"
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            if (validateInput()) simpanKategori()
        }
    }

    private fun validateInput(): Boolean {
        val nama = etNamaKategori.text.toString().trim()
        val status = autoStatus.text.toString().trim()

        if (nama.isEmpty()) {
            inputNama.error = "Nama kategori tidak boleh kosong"
            return false
        } else {
            inputNama.error = null
        }

        if (status.isEmpty()) {
            Toast.makeText(this, "Pilih status kategori", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun simpanKategori() {
        val nama = etNamaKategori.text.toString().trim()
        val status = autoStatus.text.toString().trim()

        if (mode == "edit") {
            dbHelper.updateKategori(kategoriId, nama, status)
            Toast.makeText(this, "Kategori berhasil diupdate", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.insertKategori(nama, status)
            Toast.makeText(this, "Kategori berhasil disimpan", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}