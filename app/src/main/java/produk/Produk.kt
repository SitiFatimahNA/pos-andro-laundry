package produk

data class Produk(
    val nama: String,
    val harga: String,
    val kategori: String,
    val stok: Int,
    val cabang: String,
    val isAktif: Boolean,
    // Tambah field baru
    val id: String = "",
    val kategoriId: String = "",
    val foto: String = ""
)