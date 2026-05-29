package com.siti.pos

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import Helper.DatabaseHelper
import adapter.KategoriAdapter

class data_kategori : AppCompatActivity() {

    private lateinit var rvKategori: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var ivKembali: ImageView
    private lateinit var ivReload: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var adapter: KategoriAdapter
    private lateinit var dbHelper: DatabaseHelper

    private var listSemua: MutableList<ModelKategori> = mutableListOf()

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_kategori)

        dbHelper = DatabaseHelper(this)

        initViews()
        setupRecyclerView()
        setupFab()
        setupSearch()
        setupListeners()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun initViews() {
        rvKategori = findViewById(R.id.rvKategori)
        fabTambah = findViewById(R.id.fabTambah)
        ivKembali = findViewById(R.id.ivkembali)
        ivReload = findViewById(R.id.ivReload)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun setupRecyclerView() {
        adapter = KategoriAdapter(
            mutableListOf(),
            onItemClick = { kategori ->
                showOptionsDialog(kategori)
            }
        )

        rvKategori.layoutManager = LinearLayoutManager(this)
        rvKategori.adapter = adapter
    }

    private fun setupFab() {
        fabTambah.setOnClickListener {
            val intent = Intent(this, mod_kategori::class.java)
            intent.putExtra("mode", "tambah")
            launcher.launch(intent)
        }
    }

    private fun setupListeners() {

        ivKembali.setOnClickListener {
            finish()
        }

        ivReload.setOnClickListener {
            etSearch.setText("")
            loadData()
        }
    }

    private fun setupSearch() {

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadData() {

        listSemua.clear()

        listSemua.addAll(dbHelper.getAllKategori())

        android.util.Log.d(
            "DB_TEST",
            "Jumlah data kategori = ${listSemua.size}"
        )

        adapter.updateData(listSemua)
    }

    private fun filterData(query: String) {

        val filtered = if (query.isEmpty()) {
            listSemua
        } else {
            listSemua.filter {
                it.namaKategori?.contains(
                    query,
                    ignoreCase = true
                ) == true
            }.toMutableList()
        }

        adapter.updateData(filtered)
    }

    private fun showOptionsDialog(kategori: ModelKategori) {

        AlertDialog.Builder(this)
            .setTitle(kategori.namaKategori)
            .setItems(arrayOf("Edit", "Hapus")) { _, which ->

                when (which) {

                    0 -> bukaEdit(kategori)

                    1 -> konfirmasiHapus(kategori)
                }
            }
            .show()
    }

    private fun bukaEdit(kategori: ModelKategori) {

        val intent = Intent(this, mod_kategori::class.java).apply {

            putExtra("mode", "edit")
            putExtra("kategori_id", kategori.idKategori)
            putExtra("namaKategori", kategori.namaKategori)
            putExtra("statusKategori", kategori.statusKategori)
        }

        launcher.launch(intent)
    }

    private fun konfirmasiHapus(kategori: ModelKategori) {

        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Hapus '${kategori.namaKategori}'?")
            .setPositiveButton("Hapus") { _, _ ->

                dbHelper.deleteKategori(
                    kategori.idKategori ?: ""
                )

                loadData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}