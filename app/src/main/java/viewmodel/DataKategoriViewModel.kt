package viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.siti.pos.ModelKategori

class DataKategoriViewModel : ViewModel() {

    val kategoriList = MutableLiveData<ArrayList<ModelKategori>>()
    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    private var originalKategoriList = ArrayList<ModelKategori>()

    fun getData(dbHelper: Helper.DatabaseHelper) {
        isLoading.value = true
        val list = ArrayList(dbHelper.getAllKategori())
        originalKategoriList.clear()
        originalKategoriList.addAll(list)
        kategoriList.value = list
        isSearchEmpty.value = list.isEmpty()
        isLoading.value = false
    }

    fun filterList(query: String?) {
        if (query.isNullOrEmpty()) {
            kategoriList.value = originalKategoriList
            isSearchEmpty.value = false
        } else {
            val filteredList = originalKategoriList.filter {
                it.namaKategori?.lowercase()?.contains(query.lowercase()) == true
            }
            kategoriList.value = ArrayList(filteredList)
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }
}