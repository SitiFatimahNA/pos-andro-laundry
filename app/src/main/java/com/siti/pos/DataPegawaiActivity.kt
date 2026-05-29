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
import adapter.PegawaiAdapter
import model.ModelPegawai

class DataPegawaiActivity : AppCompatActivity() {

    private lateinit var rvPegawai: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var ivKembali: ImageView
    private lateinit var ivReload: ImageView
    private lateinit var etSearch: EditText
    private lateinit var adapter: PegawaiAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var listSemua: MutableList<ModelPegawai> = mutableListOf()

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { loadData() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_pegawai)

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
        rvPegawai = findViewById(R.id.rvPegawai)
        fabTambah = findViewById(R.id.fabTambah)
        ivKembali = findViewById(R.id.ivKembali)
        ivReload = findViewById(R.id.ivReload)
        etSearch = findViewById(R.id.etSearch)
    }

    private fun setupRecyclerView() {
        adapter = PegawaiAdapter(mutableListOf()) { pegawai ->
            showOptionsDialog(pegawai)
        }
        rvPegawai.layoutManager = LinearLayoutManager(this)
        rvPegawai.setHasFixedSize(true)
        rvPegawai.adapter = adapter
    }

    private fun setupListeners() {
        ivKembali.setOnClickListener { finish() }

        ivReload.setOnClickListener {
            etSearch.setText("")
            loadData()
            Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show()
        }

        fabTambah.setOnClickListener {
            val intent = Intent(this, ModPegawaiActivity::class.java)
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
        listSemua = dbHelper.getAllPegawai().toMutableList()
        adapter.updateData(listSemua)
    }

    private fun filterData(query: String) {
        val filtered = if (query.isEmpty()) listSemua
        else listSemua.filter {
            it.nama.contains(query, ignoreCase = true) ||
                    it.jabatan.contains(query, ignoreCase = true)
        }.toMutableList()
        adapter.updateData(filtered)
    }

    private fun showOptionsDialog(pegawai: ModelPegawai) {
        val statusLabel = if (pegawai.isAktif) "Nonaktifkan" else "Aktifkan"
        AlertDialog.Builder(this)
            .setTitle(pegawai.nama)
            .setItems(arrayOf("Edit", "Hapus", statusLabel)) { _, which ->
                when (which) {
                    0 -> bukaEdit(pegawai)
                    1 -> konfirmasiHapus(pegawai)
                    2 -> toggleStatus(pegawai)
                }
            }
            .show()
    }

    private fun bukaEdit(pegawai: ModelPegawai) {
        val intent = Intent(this, ModPegawaiActivity::class.java).apply {
            putExtra("mode", "edit")
            putExtra("id", pegawai.id)
            putExtra("nama", pegawai.nama)
            putExtra("jabatan", pegawai.jabatan)
            putExtra("no_hp", pegawai.noHp)
            putExtra("foto", pegawai.foto)
        }
        launcher.launch(intent)
    }

    private fun konfirmasiHapus(pegawai: ModelPegawai) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pegawai")
            .setMessage("Hapus '${pegawai.nama}'?")
            .setPositiveButton("Hapus") { _, _ ->
                dbHelper.deletePegawai(pegawai.id)
                loadData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun toggleStatus(pegawai: ModelPegawai) {
        dbHelper.updateStatusPegawai(pegawai.id, !pegawai.isAktif)
        loadData()
    }
}