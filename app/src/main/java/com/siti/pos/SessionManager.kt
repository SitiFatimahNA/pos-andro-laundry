package com.siti.pos

import android.content.Context
import android.content.SharedPreferences
import model.ModelAkun

class SessionManager(context: Context) {
    private val pref: SharedPreferences =
        context.getSharedPreferences("pos_session", Context.MODE_PRIVATE)

    fun simpanSession(akun: ModelAkun) {
        pref.edit().apply {
            putString("id", akun.id)
            putString("nama", akun.nama)
            putString("email", akun.email)
            putString("role", akun.role)
            putString("foto", akun.foto)
            putBoolean("is_login", true)
            apply()
        }
    }

    fun getAkunLogin(): ModelAkun {
        return ModelAkun(
            id = pref.getString("id", "") ?: "",
            nama = pref.getString("nama", "") ?: "",
            email = pref.getString("email", "") ?: "",
            role = pref.getString("role", "") ?: "",
            foto = pref.getString("foto", "") ?: ""
        )
    }

    fun isLogin(): Boolean = pref.getBoolean("is_login", false)

    fun logout() {
        val sudahRegister = pref.getBoolean("sudah_register", false)
        pref.edit().clear().apply()
        // Simpan kembali status register setelah clear
        pref.edit().putBoolean("sudah_register", sudahRegister).apply()
    }

    // ← ini harus di dalam class, sebelum kurung tutup
    fun sudahAdaAkun(): Boolean {
        return pref.getBoolean("sudah_register", false)
    }

    fun tandaiSudahRegister() {
        pref.edit().putBoolean("sudah_register", true).apply()
    }

} // ← kurung tutup class di sini