package Helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import model.ModelAkun
import produk.Produk
import model.ModelPegawai
import model.ModelCabang
import model.ModelPelanggan
import model.ModelTransaksi
import model.ModelItemTransaksi
import com.siti.pos.ModelKategori

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "pos_db", null, 7) {

    override fun onCreate(db: SQLiteDatabase) {

        // ← tabel kategori jangan sampai hilang
        db.execSQL("""
            CREATE TABLE kategori (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                status TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE produk (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                harga TEXT NOT NULL,
                kategori_id TEXT NOT NULL,
                kategori_nama TEXT NOT NULL,
                stok INTEGER NOT NULL,
                cabang TEXT NOT NULL,
                foto TEXT,
                is_aktif INTEGER DEFAULT 1
            )
        """)

        db.execSQL("""
            CREATE TABLE akun (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                foto TEXT,
                is_aktif INTEGER DEFAULT 1
            )
        """)

        db.execSQL("""
            INSERT INTO akun (nama, email, password, role, is_aktif)
            VALUES ('Admin', 'admin@pos.com', 'admin123', 'Admin', 1)
        """)

        db.execSQL("""
            CREATE TABLE pegawai (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                jabatan TEXT NOT NULL,
                no_hp TEXT,
                foto TEXT,
                is_aktif INTEGER DEFAULT 1
            )
        """)

        db.execSQL("""
            CREATE TABLE cabang (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                alamat TEXT,
                no_telp TEXT,
                is_aktif INTEGER DEFAULT 1
            )
        """)

        db.execSQL("""
            CREATE TABLE pelanggan (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                no_hp TEXT,
                alamat TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nomor_transaksi TEXT NOT NULL,
                tanggal TEXT NOT NULL,
                pelanggan_id TEXT,
                pelanggan_nama TEXT,
                cabang_nama TEXT,
                metode_pembayaran TEXT,
                catatan TEXT,
                total_harga INTEGER DEFAULT 0,
                status TEXT DEFAULT 'Selesai'
            )
        """)

        db.execSQL("""
            CREATE TABLE item_transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                transaksi_id INTEGER NOT NULL,
                produk_id TEXT,
                nama_produk TEXT NOT NULL,
                harga INTEGER NOT NULL,
                jumlah INTEGER NOT NULL,
                subtotal INTEGER NOT NULL
            )
        """)
    }

    // ← posisi onUpgrade sudah benar, di dalam class bukan di luar
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS akun (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nama TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    foto TEXT,
                    is_aktif INTEGER DEFAULT 1
                )
            """)
            db.execSQL("""
                INSERT OR IGNORE INTO akun (nama, email, password, role, is_aktif)
                VALUES ('Admin', 'admin@pos.com', 'admin123', 'Admin', 1)
            """)
            try {
                db.execSQL("ALTER TABLE produk ADD COLUMN is_aktif INTEGER DEFAULT 1")
            } catch (e: Exception) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS produk (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nama TEXT NOT NULL,
                        harga TEXT NOT NULL,
                        kategori_id TEXT NOT NULL,
                        kategori_nama TEXT NOT NULL,
                        stok INTEGER NOT NULL,
                        cabang TEXT NOT NULL,
                        foto TEXT,
                        is_aktif INTEGER DEFAULT 1
                    )
                """)

                db.execSQL("""
                CREATE TABLE IF NOT EXISTS pegawai (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nama TEXT NOT NULL,
                    jabatan TEXT NOT NULL,
                    no_hp TEXT,
                    foto TEXT,
                    is_aktif INTEGER DEFAULT 1
                )
            """)

                db.execSQL("""
            CREATE TABLE cabang (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                alamat TEXT,
                no_telp TEXT,
                is_aktif INTEGER DEFAULT 1
            )
        """)

                db.execSQL("""
                CREATE TABLE IF NOT EXISTS pelanggan (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nama TEXT NOT NULL,
                    no_hp TEXT,
                    alamat TEXT
                )
            """)
                db.execSQL("""
                CREATE TABLE IF NOT EXISTS transaksi (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nomor_transaksi TEXT NOT NULL,
                    tanggal TEXT NOT NULL,
                    pelanggan_id TEXT,
                    pelanggan_nama TEXT,
                    cabang_nama TEXT,
                    metode_pembayaran TEXT,
                    catatan TEXT,
                    total_harga INTEGER DEFAULT 0,
                    status TEXT DEFAULT 'Selesai'
                )
            """)
                db.execSQL("""
                CREATE TABLE IF NOT EXISTS item_transaksi (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaksi_id INTEGER NOT NULL,
                    produk_id TEXT,
                    nama_produk TEXT NOT NULL,
                    harga INTEGER NOT NULL,
                    jumlah INTEGER NOT NULL,
                    subtotal INTEGER NOT NULL
                )
            """)
            }
        }
    }

    // ── KATEGORI ──────────────────────────────────────────

    fun insertKategori(nama: String, status: String): Long {
        val values = ContentValues().apply {
            put("nama", nama)
            put("status", status)
        }
        return writableDatabase.insert("kategori", null, values)
    }

    fun updateKategori(id: String, nama: String, status: String): Int {
        val values = ContentValues().apply {
            put("nama", nama)
            put("status", status)
        }
        return writableDatabase.update("kategori", values, "id=?", arrayOf(id))
    }

    fun deleteKategori(id: String): Int {
        return writableDatabase.delete("kategori", "id=?", arrayOf(id))
    }

    fun getAllKategori(): MutableList<ModelKategori> {
        val list = mutableListOf<ModelKategori>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM kategori", null)
        while (cursor.moveToNext()) {
            list.add(
                ModelKategori(
                    idKategori = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    namaKategori = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    statusKategori = cursor.getString(cursor.getColumnIndexOrThrow("status")) // ← String, bukan Boolean
                )
            )
        }
        cursor.close()
        return list
    }

    // ── PRODUK ────────────────────────────────────────────

    fun insertProduk(
        nama: String,
        harga: String,
        kategoriId: String,
        kategoriNama: String,
        stok: Int,
        cabang: String,
        foto: String
    ): Long {
        val values = ContentValues().apply {
            put("nama", nama)
            put("harga", harga)
            put("kategori_id", kategoriId)
            put("kategori_nama", kategoriNama)
            put("stok", stok)
            put("cabang", cabang)
            put("foto", foto)
        }
        return writableDatabase.insert("produk", null, values)
    }

    fun getAllProduk(): List<Produk> {
        val list = mutableListOf<Produk>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM produk", null)
        while (cursor.moveToNext()) {
            list.add(
                Produk(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    harga = cursor.getString(cursor.getColumnIndexOrThrow("harga")),
                    kategori = cursor.getString(cursor.getColumnIndexOrThrow("kategori_nama")),
                    kategoriId = cursor.getString(cursor.getColumnIndexOrThrow("kategori_id")),
                    stok = cursor.getInt(cursor.getColumnIndexOrThrow("stok")),
                    cabang = cursor.getString(cursor.getColumnIndexOrThrow("cabang")),
                    foto = cursor.getString(cursor.getColumnIndexOrThrow("foto")) ?: "",
                    isAktif = cursor.getInt(cursor.getColumnIndexOrThrow("is_aktif")) == 1
                )
            )
        }
        cursor.close()
        return list
    }

    fun deleteProduk(id: String): Int {
        return writableDatabase.delete("produk", "id=?", arrayOf(id))
    }

    fun updateStatusProduk(id: String, isAktif: Boolean): Int {
        val values = ContentValues().apply {
            put("is_aktif", if (isAktif) 1 else 0)
        }
        return writableDatabase.update("produk", values, "id=?", arrayOf(id))
    }

    fun updateProduk(
        id: String,
        nama: String,
        harga: String,
        kategoriId: String,
        kategoriNama: String,
        stok: Int,
        cabang: String,
        foto: String
    ): Int {
        val values = ContentValues().apply {
            put("nama", nama)
            put("harga", harga)
            put("kategori_id", kategoriId)
            put("kategori_nama", kategoriNama)
            put("stok", stok)
            put("cabang", cabang)
            put("foto", foto)
        }
        return writableDatabase.update("produk", values, "id=?", arrayOf(id))
    }

    // ── AKUN ──────────────────────────────────────────────

    fun loginAkun(email: String, password: String): ModelAkun? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM akun WHERE email=? AND password=? AND is_aktif=1",
            arrayOf(email, password)
        )
        val akun = if (cursor.moveToFirst()) {
            ModelAkun(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                role = cursor.getString(cursor.getColumnIndexOrThrow("role")),
                foto = cursor.getString(cursor.getColumnIndexOrThrow("foto")) ?: "",
                isAktif = cursor.getInt(cursor.getColumnIndexOrThrow("is_aktif")) == 1
            )
        } else null
        cursor.close()
        return akun
    }

    fun updateProfil(id: String, nama: String, email: String, foto: String): Int {
        val values = ContentValues().apply {
            put("nama", nama)
            put("email", email)
            put("foto", foto)
        }
        return writableDatabase.update("akun", values, "id=?", arrayOf(id))
    }

    fun updatePassword(id: String, passwordBaru: String): Int {
        val values = ContentValues().apply {
            put("password", passwordBaru)
        }
        return writableDatabase.update("akun", values, "id=?", arrayOf(id))
    }

    fun getAkunById(id: String): ModelAkun? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM akun WHERE id=?", arrayOf(id)
        )
        val akun = if (cursor.moveToFirst()) {
            ModelAkun(
                id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                role = cursor.getString(cursor.getColumnIndexOrThrow("role")),
                foto = cursor.getString(cursor.getColumnIndexOrThrow("foto")) ?: "",
                isAktif = cursor.getInt(cursor.getColumnIndexOrThrow("is_aktif")) == 1
            )
        } else null
        cursor.close()
        return akun
    }

    fun insertAkun(nama: String, email: String, password: String, role: String): Long {
        return try {
            val values = ContentValues().apply {
                put("nama", nama)
                put("email", email)
                put("password", password)
                put("role", role)
                put("is_aktif", 1)
            }
            writableDatabase.insert("akun", null, values)
        } catch (e: Exception) {
            -1 // email sudah ada (UNIQUE constraint)
        }
    }

    // ── PEGAWAI ───────────────────────────────────────────

    fun insertPegawai(nama: String, jabatan: String, noHp: String, foto: String): Long {
        val values = ContentValues().apply {
            put("nama", nama)
            put("jabatan", jabatan)
            put("no_hp", noHp)
            put("foto", foto)
            put("is_aktif", 1)
        }
        return writableDatabase.insert("pegawai", null, values)
    }

    fun updatePegawai(id: String, nama: String, jabatan: String, noHp: String, foto: String): Int {
        val values = ContentValues().apply {
            put("nama", nama)
            put("jabatan", jabatan)
            put("no_hp", noHp)
            put("foto", foto)
        }
        return writableDatabase.update("pegawai", values, "id=?", arrayOf(id))
    }

    fun updateStatusPegawai(id: String, isAktif: Boolean): Int {
        val values = ContentValues().apply {
            put("is_aktif", if (isAktif) 1 else 0)
        }
        return writableDatabase.update("pegawai", values, "id=?", arrayOf(id))
    }

    fun deletePegawai(id: String): Int {
        return writableDatabase.delete("pegawai", "id=?", arrayOf(id))
    }

    fun getAllPegawai(): List<ModelPegawai> {
        val list = mutableListOf<ModelPegawai>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM pegawai", null)
        while (cursor.moveToNext()) {
            list.add(
                ModelPegawai(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    jabatan = cursor.getString(cursor.getColumnIndexOrThrow("jabatan")),
                    noHp = cursor.getString(cursor.getColumnIndexOrThrow("no_hp")) ?: "",
                    foto = cursor.getString(cursor.getColumnIndexOrThrow("foto")) ?: "",
                    isAktif = cursor.getInt(cursor.getColumnIndexOrThrow("is_aktif")) == 1
                )
            )
        }
        cursor.close()
        return list
    }

    // ── CABANG ────────────────────────────────────────────

    fun insertCabang(nama: String, alamat: String, noTelp: String): Long {
        val values = ContentValues().apply {
            put("nama", nama)
            put("alamat", alamat)
            put("no_telp", noTelp)
            put("is_aktif", 1)
        }
        return writableDatabase.insert("cabang", null, values)
    }

    fun updateCabang(id: String, nama: String, alamat: String, noTelp: String): Int {
        val values = ContentValues().apply {
            put("nama", nama)
            put("alamat", alamat)
            put("no_telp", noTelp)
        }
        return writableDatabase.update("cabang", values, "id=?", arrayOf(id))
    }

    fun updateStatusCabang(id: String, isAktif: Boolean): Int {
        val values = ContentValues().apply {
            put("is_aktif", if (isAktif) 1 else 0)
        }
        return writableDatabase.update("cabang", values, "id=?", arrayOf(id))
    }

    fun deleteCabang(id: String): Int {
        return writableDatabase.delete("cabang", "id=?", arrayOf(id))
    }

    fun getAllCabang(): List<ModelCabang> {
        val list = mutableListOf<ModelCabang>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM cabang", null)
        while (cursor.moveToNext()) {
            list.add(
                ModelCabang(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    alamat = cursor.getString(cursor.getColumnIndexOrThrow("alamat")) ?: "",
                    noTelp = cursor.getString(cursor.getColumnIndexOrThrow("no_telp")) ?: "",
                    isAktif = cursor.getInt(cursor.getColumnIndexOrThrow("is_aktif")) == 1
                )
            )
        }
        cursor.close()
        return list
    }

    fun getAllNamaCabang(): List<String> {
        val list = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery(
            "SELECT nama FROM cabang WHERE is_aktif=1", null
        )
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }

    // ── PELANGGAN ─────────────────────────────────────────

    fun insertPelanggan(nama: String, noHp: String, alamat: String): Long {
        val values = ContentValues().apply {
            put("nama", nama)
            put("no_hp", noHp)
            put("alamat", alamat)
        }
        return writableDatabase.insert("pelanggan", null, values)
    }

    fun getAllPelanggan(): List<ModelPelanggan> {
        val list = mutableListOf<ModelPelanggan>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM pelanggan", null)
        while (cursor.moveToNext()) {
            list.add(
                ModelPelanggan(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    nama = cursor.getString(cursor.getColumnIndexOrThrow("nama")),
                    noHp = cursor.getString(cursor.getColumnIndexOrThrow("no_hp")) ?: "",
                    alamat = cursor.getString(cursor.getColumnIndexOrThrow("alamat")) ?: ""
                )
            )
        }
        cursor.close()
        return list
    }

    // ── TRANSAKSI ─────────────────────────────────────────

    fun insertTransaksi(transaksi: ModelTransaksi): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nomor_transaksi", transaksi.nomorTransaksi)
            put("tanggal", transaksi.tanggal)
            put("pelanggan_id", transaksi.pelangganId)
            put("pelanggan_nama", transaksi.pelangganNama)
            put("cabang_nama", transaksi.cabangNama)
            put("metode_pembayaran", transaksi.metodePembayaran)
            put("catatan", transaksi.catatan)
            put("total_harga", transaksi.totalHarga)
            put("status", transaksi.status)
        }
        val transaksiId = db.insert("transaksi", null, values)

        // Insert item-item transaksi
        transaksi.items.forEach { item ->
            val itemValues = ContentValues().apply {
                put("transaksi_id", transaksiId)
                put("produk_id", item.produkId)
                put("nama_produk", item.namaProduk)
                put("harga", item.harga)
                put("jumlah", item.jumlah)
                put("subtotal", item.subtotal)
            }
            db.insert("item_transaksi", null, itemValues)
        }
        return transaksiId
    }

    fun getAllTransaksi(): List<ModelTransaksi> {
        val list = mutableListOf<ModelTransaksi>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM transaksi ORDER BY id DESC", null
        )
        while (cursor.moveToNext()) {
            val transaksiId = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            list.add(
                ModelTransaksi(
                    id = transaksiId,
                    nomorTransaksi = cursor.getString(cursor.getColumnIndexOrThrow("nomor_transaksi")),
                    tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal")),
                    pelangganId = cursor.getString(cursor.getColumnIndexOrThrow("pelanggan_id")) ?: "",
                    pelangganNama = cursor.getString(cursor.getColumnIndexOrThrow("pelanggan_nama")) ?: "",
                    cabangNama = cursor.getString(cursor.getColumnIndexOrThrow("cabang_nama")) ?: "",
                    metodePembayaran = cursor.getString(cursor.getColumnIndexOrThrow("metode_pembayaran")) ?: "",
                    catatan = cursor.getString(cursor.getColumnIndexOrThrow("catatan")) ?: "",
                    totalHarga = cursor.getLong(cursor.getColumnIndexOrThrow("total_harga")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "Selesai",
                    items = getItemsByTransaksiId(transaksiId)
                )
            )
        }
        cursor.close()
        return list
    }

    fun getItemsByTransaksiId(transaksiId: String): List<ModelItemTransaksi> {
        val list = mutableListOf<ModelItemTransaksi>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM item_transaksi WHERE transaksi_id=?", arrayOf(transaksiId)
        )
        while (cursor.moveToNext()) {
            list.add(
                ModelItemTransaksi(
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    transaksiId = transaksiId,
                    produkId = cursor.getString(cursor.getColumnIndexOrThrow("produk_id")) ?: "",
                    namaProduk = cursor.getString(cursor.getColumnIndexOrThrow("nama_produk")),
                    harga = cursor.getLong(cursor.getColumnIndexOrThrow("harga")),
                    jumlah = cursor.getInt(cursor.getColumnIndexOrThrow("jumlah")),
                    subtotal = cursor.getLong(cursor.getColumnIndexOrThrow("subtotal"))
                )
            )
        }
        cursor.close()
        return list
    }

    fun deleteTransaksi(id: String): Int {
        writableDatabase.delete("item_transaksi", "transaksi_id=?", arrayOf(id))
        return writableDatabase.delete("transaksi", "id=?", arrayOf(id))
    }

    fun generateNomorTransaksi(): String {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM transaksi", null
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0) + 1
        cursor.close()
        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
        val date = dateFormat.format(java.util.Date())
        return "TRX-$date-${count.toString().padStart(4, '0')}"
    }

    fun updatePelanggan(id: String, nama: String, noHp: String, alamat: String): Int {
        val values = ContentValues().apply {
            put("nama", nama)
            put("no_hp", noHp)
            put("alamat", alamat)
        }
        return writableDatabase.update("pelanggan", values, "id=?", arrayOf(id))
    }

    fun deletePelanggan(id: String): Int {
        return writableDatabase.delete("pelanggan", "id=?", arrayOf(id))
    }

    // ── LAPORAN ───────────────────────────────────────────

    fun getTotalPendapatanHarian(tanggal: String): Long {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(total_harga) FROM transaksi WHERE tanggal LIKE ?",
            arrayOf("$tanggal%")
        )
        cursor.moveToFirst()
        val total = cursor.getLong(0)
        cursor.close()
        return total
    }

    fun getTotalPendapatanBulanan(bulan: String): Long {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(total_harga) FROM transaksi WHERE tanggal LIKE ?",
            arrayOf("$bulan%")
        )
        cursor.moveToFirst()
        val total = cursor.getLong(0)
        cursor.close()
        return total
    }

    fun getTransaksiHarian(tanggal: String): List<ModelTransaksi> {
        val list = mutableListOf<ModelTransaksi>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM transaksi WHERE tanggal LIKE ? ORDER BY id DESC",
            arrayOf("$tanggal%")
        )
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            list.add(
                ModelTransaksi(
                    id = id,
                    nomorTransaksi = cursor.getString(cursor.getColumnIndexOrThrow("nomor_transaksi")),
                    tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal")),
                    pelangganNama = cursor.getString(cursor.getColumnIndexOrThrow("pelanggan_nama")) ?: "",
                    cabangNama = cursor.getString(cursor.getColumnIndexOrThrow("cabang_nama")) ?: "",
                    metodePembayaran = cursor.getString(cursor.getColumnIndexOrThrow("metode_pembayaran")) ?: "",
                    totalHarga = cursor.getLong(cursor.getColumnIndexOrThrow("total_harga")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "",
                    items = getItemsByTransaksiId(id)
                )
            )
        }
        cursor.close()
        return list
    }

    fun getTransaksiBulanan(bulan: String): List<ModelTransaksi> {
        val list = mutableListOf<ModelTransaksi>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM transaksi WHERE tanggal LIKE ? ORDER BY id DESC",
            arrayOf("$bulan%")
        )
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            list.add(
                ModelTransaksi(
                    id = id,
                    nomorTransaksi = cursor.getString(cursor.getColumnIndexOrThrow("nomor_transaksi")),
                    tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal")),
                    pelangganNama = cursor.getString(cursor.getColumnIndexOrThrow("pelanggan_nama")) ?: "",
                    cabangNama = cursor.getString(cursor.getColumnIndexOrThrow("cabang_nama")) ?: "",
                    metodePembayaran = cursor.getString(cursor.getColumnIndexOrThrow("metode_pembayaran")) ?: "",
                    totalHarga = cursor.getLong(cursor.getColumnIndexOrThrow("total_harga")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "",
                    items = getItemsByTransaksiId(id)
                )
            )
        }
        cursor.close()
        return list
    }

    fun getProdukTerlaris(limit: Int = 5): List<Pair<String, Int>> {
        val list = mutableListOf<Pair<String, Int>>()
        val cursor = readableDatabase.rawQuery("""
        SELECT nama_produk, SUM(jumlah) as total_terjual
        FROM item_transaksi
        GROUP BY nama_produk
        ORDER BY total_terjual DESC
        LIMIT ?
    """, arrayOf(limit.toString()))
        while (cursor.moveToNext()) {
            list.add(
                Pair(
                    cursor.getString(cursor.getColumnIndexOrThrow("nama_produk")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("total_terjual"))
                )
            )
        }
        cursor.close()
        return list
    }

    fun getPendapatanPerHariDalamBulan(bulan: String): List<Pair<String, Long>> {
        val list = mutableListOf<Pair<String, Long>>()
        val cursor = readableDatabase.rawQuery("""
        SELECT substr(tanggal, 1, 10) as tgl, SUM(total_harga) as total
        FROM transaksi
        WHERE tanggal LIKE ?
        GROUP BY tgl
        ORDER BY tgl ASC
    """, arrayOf("$bulan%"))
        while (cursor.moveToNext()) {
            list.add(
                Pair(
                    cursor.getString(cursor.getColumnIndexOrThrow("tgl")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("total"))
                )
            )
        }
        cursor.close()
        return list
    }

}