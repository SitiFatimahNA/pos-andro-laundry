package com.siti.pos

import android.content.Context
import android.content.SharedPreferences
import model.ModelPrinter

class PrinterManager (context: Context) {
    private val pref: SharedPreferences =
        context.getSharedPreferences("printer_setting", Context.MODE_PRIVATE)

    fun simpanSetting(printer: ModelPrinter) {
        pref.edit().apply {
            putString("nama", printer.namaPrinter)
            putString("mac", printer.alamatMac)
            putString("ukuran", printer.ukuranKertas)
            putBoolean("cetak_otomatis", printer.cetakOtomatis)
            apply()
        }
    }

    fun getSetting(): ModelPrinter {
        return ModelPrinter(
            namaPrinter = pref.getString("nama", "") ?: "",
            alamatMac = pref.getString("mac", "") ?: "",
            ukuranKertas = pref.getString("ukuran", "58mm") ?: "58mm",
            cetakOtomatis = pref.getBoolean("cetak_otomatis", true)
        )
    }

    fun sudahDiatur(): Boolean = getSetting().alamatMac.isNotEmpty()
}