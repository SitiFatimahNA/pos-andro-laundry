package com.siti.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.Intent
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import Helper.DatabaseHelper
import model.ModelAkun

class AkunActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView
    private lateinit var ivFotoProfil: ImageView
    private lateinit var ivEditFoto: LinearLayout
    private lateinit var tvNama: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var menuEditProfil: android.view.View
    private lateinit var menuGantiPassword: android.view.View
    private lateinit var btnLogout: LinearLayout
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager
    private lateinit var akunLogin: ModelAkun

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)
        akunLogin = session.getAkunLogin()

        initViews()
        loadProfil()
        setupListeners()
    }

    private fun initViews() {
        ivKembali = findViewById(R.id.ivKembali)
        ivFotoProfil = findViewById(R.id.ivFotoProfil)
        ivEditFoto = findViewById(R.id.ivEditFoto)
        tvNama = findViewById(R.id.tvNama)
        tvEmail = findViewById(R.id.tvEmail)
        tvRole = findViewById(R.id.tvRole)
        menuEditProfil = findViewById(R.id.menuEditProfil)
        menuGantiPassword = findViewById(R.id.menuGantiPassword)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun loadProfil() {
        val akun = dbHelper.getAkunById(akunLogin.id) ?: akunLogin
        tvNama.text = akun.nama
        tvEmail.text = akun.email
        tvRole.text = akun.role

        if (akun.foto.isNotEmpty()) {
            val file = java.io.File(akun.foto)
            if (file.exists()) {
                ivFotoProfil.setImageBitmap(android.graphics.BitmapFactory.decodeFile(akun.foto))
            }
        }
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        menuEditProfil.setOnClickListener { showDialogEditProfil() }

        menuGantiPassword.setOnClickListener { showDialogGantiPassword() }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Keluar")
                .setMessage("Yakin ingin keluar dari akun?")
                .setPositiveButton("Keluar") { _, _ ->
                    session.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun showDialogEditProfil() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profil, null)
        val etNama = view.findViewById<TextInputEditText>(R.id.etNamaBaru)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmailBaru)

        etNama.setText(akunLogin.nama)
        etEmail.setText(akunLogin.email)

        AlertDialog.Builder(this)
            .setTitle("Edit Profil")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                val email = etEmail.text.toString().trim()
                if (nama.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Nama dan email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                dbHelper.updateProfil(akunLogin.id, nama, email, akunLogin.foto)
                session.simpanSession(akunLogin.copy(nama = nama, email = email))
                akunLogin = session.getAkunLogin()
                loadProfil()
                Toast.makeText(this, "Profil berhasil diupdate", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDialogGantiPassword() {
        val view = layoutInflater.inflate(R.layout.dialog_ganti_password, null)
        val etLama = view.findViewById<TextInputEditText>(R.id.etPasswordLama)
        val etBaru = view.findViewById<TextInputEditText>(R.id.etPasswordBaru)
        val etKonfirmasi = view.findViewById<TextInputEditText>(R.id.etKonfirmasiPassword)

        AlertDialog.Builder(this)
            .setTitle("Ganti Password")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val lama = etLama.text.toString().trim()
                val baru = etBaru.text.toString().trim()
                val konfirmasi = etKonfirmasi.text.toString().trim()

                val akun = dbHelper.loginAkun(akunLogin.email, lama)
                if (akun == null) {
                    Toast.makeText(this, "Password lama salah", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (baru != konfirmasi) {
                    Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (baru.length < 6) {
                    Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                dbHelper.updatePassword(akunLogin.id, baru)
                Toast.makeText(this, "Password berhasil diganti", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}