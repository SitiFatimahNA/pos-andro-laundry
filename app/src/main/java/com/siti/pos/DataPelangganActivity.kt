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
import adapter.PelangganAdapter
import model.ModelPelanggan

class DataPelangganActivity : AppCompatActivity() {

    private lateinit var rvPelanggan: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var ivKembali: ImageView
    private lateinit var ivReload: ImageView
    private lateinit var etSearch: EditText
    private lateinit var adapter: PelangganAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var listSemua: MutableList<ModelPelanggan> = mutableListOf()

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { loadData() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pelanggan)

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
        rvPelanggan = findViewById(R.id.rvPelanggan)
        fabTambah = findViewById(R.id.fabTambah)
        ivKembali = findViewById(R.id.ivKembali)
        ivReload = findViewById(R.id.ivReload)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun setupRecyclerView() {
        adapter = PelangganAdapter(mutableListOf()) { pelanggan ->
            showOptionsDialog(pelanggan)
        }
        rvPelanggan.layoutManager = LinearLayoutManager(this)
        rvPelanggan.setHasFixedSize(true)
        rvPelanggan.adapter = adapter
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        ivReload.setOnClickListener {
            etSearch.setText("")
            loadData()
            Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show()
        }

        fabTambah.setOnClickListener {
            val intent = Intent(this, ModPelangganActivity::class.java)
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
        listSemua = dbHelper.getAllPelanggan().toMutableList()
        adapter.updateData(listSemua)
    }

    private fun filterData(query: String) {
        val filtered = if (query.isEmpty()) listSemua
        else listSemua.filter {
            it.nama.contains(query, ignoreCase = true) ||
                    it.noHp.contains(query, ignoreCase = true) ||
                    it.alamat.contains(query, ignoreCase = true)
        }.toMutableList()
        adapter.updateData(filtered)
    }

    private fun showOptionsDialog(pelanggan: ModelPelanggan) {
        AlertDialog.Builder(this)
            .setTitle(pelanggan.nama)
            .setItems(arrayOf("Edit", "Hapus")) { _, which ->
                when (which) {
                    0 -> bukaEdit(pelanggan)
                    1 -> konfirmasiHapus(pelanggan)
                }
            }
            .show()
    }

    private fun bukaEdit(pelanggan: ModelPelanggan) {
        val intent = Intent(this, ModPelangganActivity::class.java).apply {
            putExtra("mode", "edit")
            putExtra("id", pelanggan.id)
            putExtra("nama", pelanggan.nama)
            putExtra("no_hp", pelanggan.noHp)
            putExtra("alamat", pelanggan.alamat)
        }
        launcher.launch(intent)
    }

    private fun konfirmasiHapus(pelanggan: ModelPelanggan) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pelanggan")
            .setMessage("Hapus '${pelanggan.nama}'?")
            .setPositiveButton("Hapus") { _, _ ->
                dbHelper.deletePelanggan(pelanggan.id)
                loadData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}