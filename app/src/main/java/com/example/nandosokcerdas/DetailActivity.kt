package com.example.nandosokcerdas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.nandosokcerdas.data.model.Crypto
import com.example.nandosokcerdas.data.remote.CryptoRepository
import com.example.nandosokcerdas.databinding.ActivityDetailBinding
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val cryptoRepository = CryptoRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val crypto = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("EXTRA_CRYPTO", Crypto::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Crypto>("EXTRA_CRYPTO")
        }

        crypto?.let { cryptoItem ->
            displayCryptoDetails(cryptoItem)
            fetchMarketChartData(cryptoItem)
            checkIfFavorite(cryptoItem)

            binding.btnFavorite.setOnClickListener {
                if (isFavorite) {
                    removeFavorite(cryptoItem)
                } else {
                    addFavorite(cryptoItem)
                }
            }
        }
    }

    private fun displayCryptoDetails(crypto: Crypto) {
        binding.tvDetailName.text = crypto.name
        binding.tvDetailSymbol.text = crypto.symbol.uppercase()
        binding.tvDetailPrice.text = "$${String.format("%,.2f", crypto.price)}"
        binding.tvDetailChange.text = "${String.format("%.2f", crypto.change24h)}%"

        Glide.with(this).load(crypto.imageUrl).into(binding.ivDetailLogo)

        val color = if (crypto.change24h >= 0) "#00FF7F" else "#FF4444"
        binding.tvDetailChange.setTextColor(Color.parseColor(color))
    }

    private fun fetchMarketChartData(crypto: Crypto) {
        lifecycleScope.launch {
            try {
                val response = cryptoRepository.getMarketChart(crypto.id)
                if (response.isSuccessful) {
                    response.body()?.prices?.let { prices ->
                        val entries = prices.map { Entry(it[0].toFloat(), it[1].toFloat()) }
                        setupLineChart(entries, crypto)
                    }
                } else {
                    Log.e("DetailActivity", "API call failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("DetailActivity", "Exception: ${e.message}")
            }
        }
    }

    private fun setupLineChart(entries: List<Entry>, crypto: Crypto) {
        val isPositiveChange = crypto.change24h >= 0
        val chartColor = if (isPositiveChange) Color.parseColor("#00FF7F") else Color.parseColor("#FF4444")
        val fillDrawable = if (isPositiveChange) {
            ContextCompat.getDrawable(this, R.drawable.chart_gradient_green)
        } else {
            ContextCompat.getDrawable(this, R.drawable.chart_gradient_red)
        }

        val dataSet = LineDataSet(entries, crypto.name).apply {
            color = chartColor
            valueTextColor = Color.WHITE
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
            setDrawFilled(true)
            this.fillDrawable = fillDrawable
            highLightColor = Color.WHITE
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false

            xAxis.isEnabled = true
            xAxis.textColor = Color.WHITE
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = DateAxisValueFormatter()
            xAxis.setDrawGridLines(false)
            xAxis.labelCount = 4

            axisLeft.isEnabled = true
            axisLeft.textColor = Color.WHITE
            axisLeft.setDrawAxisLine(false)
            axisLeft.setDrawGridLines(false)

            axisRight.isEnabled = false
            legend.isEnabled = false

            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)

            val marker = CustomMarkerView(this@DetailActivity, R.layout.chart_marker_view)
            this.marker = marker

            invalidate()
        }
    }

    // ... (Fungsi favorite tidak berubah)
    private fun checkIfFavorite(crypto: Crypto) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("favorites").document(crypto.id).get().addOnSuccessListener { document ->
                isFavorite = document.exists()
                updateFavoriteButtonUI()
            }
    }
    private fun updateFavoriteButtonUI() {
        if (isFavorite) {
            binding.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            binding.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }
    private fun addFavorite(crypto: Crypto) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Silakan login untuk menggunakan fitur favorit.", Toast.LENGTH_SHORT).show()
            return
        }
        val favoriteCoin = hashMapOf("id" to crypto.id, "name" to crypto.name, "symbol" to crypto.symbol, "imageUrl" to crypto.imageUrl)
        firestore.collection("users").document(userId).collection("favorites").document(crypto.id).set(favoriteCoin).addOnSuccessListener { 
                Toast.makeText(this, "${crypto.name} ditambahkan ke favorit!", Toast.LENGTH_SHORT).show()
                isFavorite = true
                updateFavoriteButtonUI()
            }.addOnFailureListener { e -> Toast.makeText(this, "Gagal menambahkan favorit: ${e.message}", Toast.LENGTH_SHORT).show() }
    }
    private fun removeFavorite(crypto: Crypto) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("favorites").document(crypto.id).delete().addOnSuccessListener { Toast.makeText(this, "${crypto.name} dihapus dari favorit", Toast.LENGTH_SHORT).show(); isFavorite = false; updateFavoriteButtonUI() }.addOnFailureListener { e -> Toast.makeText(this, "Gagal menghapus favorit: ${e.message}", Toast.LENGTH_SHORT).show() }
    }
}

class DateAxisValueFormatter : ValueFormatter() {
    private val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
        return sdf.format(Date(value.toLong()))
    }
}

// Perbarui CustomMarkerView untuk menampilkan tanggal
@SuppressLint("ViewConstructor") // Suppress warning
class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvPrice: TextView = findViewById(R.id.tvPrice)
    private val tvDate: TextView = findViewById(R.id.tvDate)
    private val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            tvPrice.text = "$${String.format("%,.2f", it.y)}"
            tvDate.text = sdf.format(Date(it.x.toLong()))
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}
