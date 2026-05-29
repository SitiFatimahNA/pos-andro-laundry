package com.siti.pos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import Helper.DatabaseHelper


class ModCabangActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var etNama: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var etNoTelp: TextInputEditText
    private lateinit var btnSimpan: Button
    private lateinit var dbHelper: DatabaseHelper

    private var mode: String = "tambah"
    private var cabangId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_cabang)

        dbHelper = DatabaseHelper(this)
        initViews()
        checkIntentMode()
        setupListeners()
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        tvJudul = findViewById(R.id.tvJudul)
        etNama = findViewById(R.id.etNama)
        etAlamat = findViewById(R.id.etAlamat)
        etNoTelp = findViewById(R.id.etNoTelp)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun checkIntentMode() {
        mode = intent.getStringExtra("mode") ?: "tambah"
        cabangId = intent.getStringExtra("id") ?: ""

        if (mode == "edit") {
            tvJudul.text = "Edit Cabang"
            btnSimpan.text = "Update"
            etNama.setText(intent.getStringExtra("nama") ?: "")
            etAlamat.setText(intent.getStringExtra("alamat") ?: "")
            etNoTelp.setText(intent.getStringExtra("no_telp") ?: "")
        }
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            if (validateInput()) simpanCabang()
        }
    }

    private fun validateInput(): Boolean {
        val nama = etNama.text.toString().trim()
        if (nama.isEmpty()) {
            etNama.error = "Nama cabang tidak boleh kosong"
            return false
        }
        return true
    }

    private fun simpanCabang() {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val noTelp = etNoTelp.text.toString().trim()

        if (mode == "edit") {
            val result = dbHelper.updateCabang(cabangId, nama, alamat, noTelp)
            if (result > 0) {
                Toast.makeText(this, "Cabang berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal mengupdate cabang", Toast.LENGTH_SHORT).show()
            }
        } else {
            val result = dbHelper.insertCabang(nama, alamat, noTelp)
            if (result > 0) {
                Toast.makeText(this, "Cabang berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal menyimpan cabang", Toast.LENGTH_SHORT).show()
            }
        }
    }
}