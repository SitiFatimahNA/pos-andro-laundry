package model

data class ModelAkun(

    val id: String = "",
    val nama: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "",
    val foto: String = "",
    val isAktif: Boolean = true
)
