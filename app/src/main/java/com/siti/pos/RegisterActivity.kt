package com.siti.pos

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import Helper.DatabaseHelper
import androidx.cardview.widget.CardView

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNama: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etKonfirmasi: TextInputEditText
    private lateinit var btnDaftar: CardView
    private lateinit var tvSudahPunyaAkun: TextView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        if (session.isLogin()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etKonfirmasi = findViewById(R.id.etKonfirmasi)
        btnDaftar = findViewById(R.id.btnDaftar)
        tvSudahPunyaAkun = findViewById(R.id.tvSudahPunyaAkun)
    }

    private fun setupListeners() {
        btnDaftar.setOnClickListener {
            if (validateInput()) daftarAkun()
        }

        tvSudahPunyaAkun.setOnClickListener {
            if (session.sudahAdaAkun()) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Belum ada akun, silakan daftar dulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(): Boolean {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val konfirmasi = etKonfirmasi.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return false
        }
        if (email.isEmpty()) {
            etEmail.error = "Email tidak boleh kosong"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password tidak boleh kosong"
            return false
        }
        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            return false
        }
        if (konfirmasi != password) {
            etKonfirmasi.error = "Konfirmasi password tidak cocok"
            return false
        }
        return true
    }

    private fun daftarAkun() {
        val nama = etNama.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val result = dbHelper.insertAkun(nama, email, password, "Admin")
        if (result > 0) {
            session.tandaiSudahRegister()
            Toast.makeText(this, "Akun berhasil dibuat, silakan login", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
        }
    }
}