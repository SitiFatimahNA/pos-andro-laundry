package com.siti.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import Helper.DatabaseHelper


class ModPelangganActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var etNama: TextInputEditText
    private lateinit var etNoHp: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var btnSimpan: Button
    private lateinit var dbHelper: DatabaseHelper

    private var mode: String = "tambah"
    private var pelangganId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_pelanggan)

        dbHelper = DatabaseHelper(this)
        initViews()
        checkIntentMode()
        setupListeners()
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        tvJudul = findViewById(R.id.tvJudul)
        etNama = findViewById(R.id.etNama)
        etNoHp = findViewById(R.id.etNoHp)
        etAlamat = findViewById(R.id.etAlamat)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun checkIntentMode() {
        mode = intent.getStringExtra("mode") ?: "tambah"
        pelangganId = intent.getStringExtra("id") ?: ""

        if (mode == "edit") {
            tvJudul.text = "Edit Pelanggan"
            btnSimpan.text = "Update"
            etNama.setText(intent.getStringExtra("nama") ?: "")
            etNoHp.setText(intent.getStringExtra("no_hp") ?: "")
            etAlamat.setText(intent.getStringExtra("alamat") ?: "")
        }
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            if (validateInput()) simpanPelanggan()
        }
    }

    private fun validateInput(): Boolean {
        val nama = etNama.text.toString().trim()
        if (nama.isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return false
        }
        return true
    }

    private fun simpanPelanggan() {
        val nama = etNama.text.toString().trim()
        val noHp = etNoHp.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()

        if (mode == "edit") {
            val result = dbHelper.updatePelanggan(pelangganId, nama, noHp, alamat)
            if (result > 0) {
                Toast.makeText(this, "Pelanggan berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal mengupdate pelanggan", Toast.LENGTH_SHORT).show()
            }
        } else {
            val result = dbHelper.insertPelanggan(nama, noHp, alamat)
            if (result > 0) {
                Toast.makeText(this, "Pelanggan berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal menyimpan pelanggan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}