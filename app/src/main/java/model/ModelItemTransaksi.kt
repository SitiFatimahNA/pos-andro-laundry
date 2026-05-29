package model

data class ModelItemTransaksi(
    val id: String = "",
    val transaksiId: String = "",
    val produkId: String = "",
    val namaProduk: String = "",
    val harga: Long = 0,
    val jumlah: Int = 1,
    val subtotal: Long = 0,
    val foto: String = ""
)
