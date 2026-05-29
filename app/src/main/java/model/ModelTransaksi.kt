package model

data class ModelTransaksi(
    val id: String = "",
    val nomorTransaksi: String = "",
    val tanggal: String = "",
    val pelangganId: String = "",
    val pelangganNama: String = "",
    val cabangNama: String = "",
    val metodePembayaran: String = "",
    val catatan: String = "",
    val totalHarga: Long = 0,
    val status: String = "Selesai",
    val items: List<ModelItemTransaksi> = emptyList()
)
