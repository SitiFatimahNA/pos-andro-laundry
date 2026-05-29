package com.siti.pos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import Helper.DatabaseHelper
import androidx.cardview.widget.CardView

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: CardView
    private lateinit var tvBuatAkun: TextView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        if (session.isLogin()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        if (!session.sudahAdaAkun()) {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvBuatAkun = findViewById(R.id.tvBuatAkun)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val akun = dbHelper.loginAkun(email, password)
            if (akun != null) {
                session.simpanSession(akun)
                Toast.makeText(this, "Selamat datang, ${akun.nama}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
            }
        }

        tvBuatAkun.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}