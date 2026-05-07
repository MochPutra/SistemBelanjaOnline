package com.example.sistembelanjaonline

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Input Views
    private lateinit var etNamaPembeli: EditText
    private lateinit var etNamaProduk: EditText
    private lateinit var etHargaSatuan: EditText
    private lateinit var etJumlahBarang: EditText
    private lateinit var spinnerZona: Spinner

    // Member Toggle
    private lateinit var btnMemberReguler: LinearLayout
    private lateinit var btnMemberGold: LinearLayout
    private lateinit var tvMemberRegulerLabel: TextView
    private lateinit var tvMemberGoldLabel: TextView
    private var selectedMember = "Reguler"

    // Button Views
    private lateinit var btnHitung: Button
    private lateinit var btnReset: Button

    // Output Views
    private lateinit var layoutOutput: LinearLayout
    private lateinit var tvOutNamaPembeli: TextView
    private lateinit var tvOutNamaProduk: TextView
    private lateinit var tvOutHargaSatuan: TextView
    private lateinit var tvOutJumlahBarang: TextView
    private lateinit var tvOutSubtotal: TextView
    private lateinit var tvOutDiskon: TextView
    private lateinit var tvOutOngkir: TextView
    private lateinit var tvOutTotalBayar: TextView
    private lateinit var tvPromoText: TextView

    // Data
    private val ongkirMap = mapOf(
        "Lokal (dalam kota) — Rp5.000" to 5_000,
        "Regional (antar kota/provinsi) — Rp15.000" to 15_000,
        "Nasional (luar pulau) — Rp30.000" to 30_000
    )

    private val diskonMap = mapOf(
        "Reguler" to 0.0,
        "Gold" to 0.10
    )

    private val MINIMAL_FREE_ONGKIR = 500_000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupSpinner()
        setupMemberToggle()
        setupButtons()

        // Default: pilih Reguler
        selectMember("Reguler")
    }

    private fun initViews() {
        etNamaPembeli = findViewById(R.id.etNamaPembeli)
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etHargaSatuan = findViewById(R.id.etHargaSatuan)
        etJumlahBarang = findViewById(R.id.etJumlahBarang)
        spinnerZona = findViewById(R.id.spinnerZona)

        btnMemberReguler = findViewById(R.id.btnMemberReguler)
        btnMemberGold = findViewById(R.id.btnMemberGold)
        tvMemberRegulerLabel = findViewById(R.id.tvMemberRegulerLabel)
        tvMemberGoldLabel = findViewById(R.id.tvMemberGoldLabel)

        btnHitung = findViewById(R.id.btnHitung)
        btnReset = findViewById(R.id.btnReset)

        layoutOutput = findViewById(R.id.layoutOutput)
        tvOutNamaPembeli = findViewById(R.id.tvOutNamaPembeli)
        tvOutNamaProduk = findViewById(R.id.tvOutNamaProduk)
        tvOutHargaSatuan = findViewById(R.id.tvOutHargaSatuan)
        tvOutJumlahBarang = findViewById(R.id.tvOutJumlahBarang)
        tvOutSubtotal = findViewById(R.id.tvOutSubtotal)
        tvOutDiskon = findViewById(R.id.tvOutDiskon)
        tvOutOngkir = findViewById(R.id.tvOutOngkir)
        tvOutTotalBayar = findViewById(R.id.tvOutTotalBayar)
        tvPromoText = findViewById(R.id.tvPromoText)
    }

    private fun setupSpinner() {
        val zonaList = ongkirMap.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, zonaList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerZona.adapter = adapter
    }

    private fun setupMemberToggle() {
        btnMemberReguler.setOnClickListener { selectMember("Reguler") }
        btnMemberGold.setOnClickListener { selectMember("Gold") }
    }

    private fun selectMember(member: String) {
        selectedMember = member

        if (member == "Reguler") {
            // Aktifkan Reguler
            btnMemberReguler.background = ContextCompat.getDrawable(this, R.drawable.bg_member_active)
            tvMemberRegulerLabel.setTextColor(ContextCompat.getColor(this, R.color.navy))

            // Non-aktifkan Gold
            btnMemberGold.background = ContextCompat.getDrawable(this, R.drawable.bg_member_inactive)
            tvMemberGoldLabel.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
        } else {
            // Aktifkan Gold
            btnMemberGold.background = ContextCompat.getDrawable(this, R.drawable.bg_member_gold_active)
            tvMemberGoldLabel.setTextColor(ContextCompat.getColor(this, R.color.gold))

            // Non-aktifkan Reguler
            btnMemberReguler.background = ContextCompat.getDrawable(this, R.drawable.bg_member_inactive)
            tvMemberRegulerLabel.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
        }
    }

    private fun setupButtons() {
        btnHitung.setOnClickListener { hitungTransaksi() }
        btnReset.setOnClickListener { resetForm() }
    }

    private fun hitungTransaksi() {
        val namaPembeli = etNamaPembeli.text.toString().trim()
        val namaProduk = etNamaProduk.text.toString().trim()
        val hargaSatuanStr = etHargaSatuan.text.toString().trim()
        val jumlahBarangStr = etJumlahBarang.text.toString().trim()

        if (namaPembeli.isEmpty()) { etNamaPembeli.error = "Wajib diisi"; etNamaPembeli.requestFocus(); return }
        if (namaProduk.isEmpty()) { etNamaProduk.error = "Wajib diisi"; etNamaProduk.requestFocus(); return }
        if (hargaSatuanStr.isEmpty()) { etHargaSatuan.error = "Wajib diisi"; etHargaSatuan.requestFocus(); return }
        if (jumlahBarangStr.isEmpty()) { etJumlahBarang.error = "Wajib diisi"; etJumlahBarang.requestFocus(); return }

        val hargaSatuan = hargaSatuanStr.toLongOrNull()
        val jumlahBarang = jumlahBarangStr.toIntOrNull()

        if (hargaSatuan == null || hargaSatuan <= 0) { etHargaSatuan.error = "Masukkan harga valid"; etHargaSatuan.requestFocus(); return }
        if (jumlahBarang == null || jumlahBarang <= 0) { etJumlahBarang.error = "Masukkan jumlah valid"; etJumlahBarang.requestFocus(); return }

        val zonaSelected = spinnerZona.selectedItem.toString()
        val ongkirAsli = ongkirMap[zonaSelected]?.toLong() ?: 0L
        val persentaseDiskon = diskonMap[selectedMember] ?: 0.0

        val subtotal = hargaSatuan * jumlahBarang
        val diskon = (subtotal * persentaseDiskon).toLong()
        val subtotalAfterDiskon = subtotal - diskon

        var ongkir = ongkirAsli
        val statusPromo: String

        if (selectedMember == "Gold" && subtotal >= MINIMAL_FREE_ONGKIR) {
            ongkir = 0L
            statusPromo = "Selamat! Free ongkir karena belanja ≥ Rp500.000"
        } else if (selectedMember == "Gold") {
            val sisa = MINIMAL_FREE_ONGKIR - subtotal
            statusPromo = "Tambah Rp${formatRupiah(sisa)} lagi untuk Free Ongkir!"
        } else {
            statusPromo = "Upgrade ke Gold untuk diskon 10% & Free Ongkir"
        }

        val totalBayar = subtotalAfterDiskon + ongkir
        val diskonPersen = (persentaseDiskon * 100).toInt()

        tampilkanOutput(namaPembeli, namaProduk, hargaSatuan, jumlahBarang,
            subtotal, diskon, diskonPersen, ongkir, totalBayar, statusPromo)
    }

    private fun tampilkanOutput(
        namaPembeli: String, namaProduk: String,
        hargaSatuan: Long, jumlahBarang: Int,
        subtotal: Long, diskon: Long, diskonPersen: Int,
        ongkir: Long, totalBayar: Long, statusPromo: String
    ) {
        tvOutNamaPembeli.text = namaPembeli
        tvOutNamaProduk.text = namaProduk
        tvOutHargaSatuan.text = "Rp ${formatRupiah(hargaSatuan)}"
        tvOutJumlahBarang.text = "$jumlahBarang item"
        tvOutSubtotal.text = "Rp ${formatRupiah(subtotal)}"

        tvOutDiskon.text = if (diskon > 0)
            "- Rp ${formatRupiah(diskon)} ($diskonPersen%)"
        else
            "Tidak ada diskon"

        tvOutOngkir.text = if (ongkir == 0L) "GRATIS ★" else "Rp ${formatRupiah(ongkir)}"
        tvOutTotalBayar.text = "Rp ${formatRupiah(totalBayar)}"
        tvPromoText.text = statusPromo

        layoutOutput.visibility = View.VISIBLE

        // Scroll ke hasil
        layoutOutput.post {
            val scrollView = findViewById<ScrollView>(R.id.scrollView)
            scrollView.smoothScrollTo(0, layoutOutput.top)
        }
    }

    private fun resetForm() {
        etNamaPembeli.text.clear()
        etNamaProduk.text.clear()
        etHargaSatuan.text.clear()
        etJumlahBarang.text.clear()
        spinnerZona.setSelection(0)
        selectMember("Reguler")
        layoutOutput.visibility = View.GONE
        etNamaPembeli.requestFocus()
    }

    private fun formatRupiah(amount: Long): String {
        val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return format.format(amount)
    }
}