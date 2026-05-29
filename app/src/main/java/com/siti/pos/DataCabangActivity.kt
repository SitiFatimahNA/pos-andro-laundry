package com.siti.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import Helper.DatabaseHelper
import adapter.CabangAdapter
import model.ModelCabang

class DataCabangActivity : AppCompatActivity() {

    private lateinit var rvCabang: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var ivKembali: ImageView
    private lateinit var ivReload: ImageView
    private lateinit var etSearch: EditText
    private lateinit var adapter: CabangAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var listSemua: MutableList<ModelCabang> = mutableListOf()

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { loadData() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_cabang)

        dbHelper = DatabaseHelper(this)
        initViews()
        setupRecyclerView()
        setupListeners()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun initViews() {
        rvCabang = findViewById(R.id.rvCabang)
        fabTambah = findViewById(R.id.fabTambah)
        ivKembali = findViewById(R.id.ivKembali)
        ivReload = findViewById(R.id.ivReload)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun setupRecyclerView() {
        adapter = CabangAdapter(mutableListOf()) { cabang ->
            showOptionsDialog(cabang)
        }
        rvCabang.layoutManager = LinearLayoutManager(this)
        rvCabang.setHasFixedSize(true)
        rvCabang.adapter = adapter
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        ivReload.setOnClickListener {
            etSearch.setText("")
            loadData()
            Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show()
        }

        fabTambah.setOnClickListener {
            val intent = Intent(this, ModCabangActivity::class.java)
            intent.putExtra("mode", "tambah")
            launcher.launch(intent)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadData() {
        listSemua = dbHelper.getAllCabang().toMutableList()
        adapter.updateData(listSemua)
    }

    private fun filterData(query: String) {
        val filtered = if (query.isEmpty()) listSemua
        else listSemua.filter {
            it.nama.contains(query, ignoreCase = true) ||
                    it.alamat.contains(query, ignoreCase = true)
        }.toMutableList()
        adapter.updateData(filtered)
    }

    private fun showOptionsDialog(cabang: ModelCabang) {
        val statusLabel = if (cabang.isAktif) "Nonaktifkan" else "Aktifkan"
        AlertDialog.Builder(this)
            .setTitle(cabang.nama)
            .setItems(arrayOf("Edit", "Hapus", statusLabel)) { _, which ->
                when (which) {
                    0 -> bukaEdit(cabang)
                    1 -> konfirmasiHapus(cabang)
                    2 -> toggleStatus(cabang)
                }
            }
            .show()
    }

    private fun bukaEdit(cabang: ModelCabang) {
        val intent = Intent(this, ModCabangActivity::class.java).apply {
            putExtra("mode", "edit")
            putExtra("id", cabang.id)
            putExtra("nama", cabang.nama)
            putExtra("alamat", cabang.alamat)
            putExtra("no_telp", cabang.noTelp)
        }
        launcher.launch(intent)
    }

    private fun konfirmasiHapus(cabang: ModelCabang) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Cabang")
            .setMessage("Hapus '${cabang.nama}'?")
            .setPositiveButton("Hapus") { _, _ ->
                dbHelper.deleteCabang(cabang.id)
                loadData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun toggleStatus(cabang: ModelCabang) {
        dbHelper.updateStatusCabang(cabang.id, !cabang.isAktif)
        loadData()
    }
}