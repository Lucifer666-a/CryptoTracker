package com.example.nandosokcerdas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nandosokcerdas.adapter.CryptoAdapter
import com.example.nandosokcerdas.data.remote.CryptoRepository
import com.example.nandosokcerdas.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val cryptoRepository = CryptoRepository()
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Set toolbar sebagai Action Bar resmi. Ini adalah langkah kunci.
        setSupportActionBar(binding.toolbar)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.rvCrypto.layoutManager = LinearLayoutManager(this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchCryptoData()
        }

        fetchCryptoData()
    }

    // 2. Gunakan callback standar onCreateOptionsMenu untuk memuat menu.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    // 3. Gunakan callback standar onOptionsItemSelected untuk menangani klik.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_favorites -> {
                if (firebaseAuth.currentUser != null) {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                } else {
                    Toast.makeText(this, "Silakan login untuk menggunakan fitur favorit", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.menu_logout -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        firebaseAuth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun fetchCryptoData() {
        lifecycleScope.launch {
            binding.tvError.visibility = View.GONE

            if (!binding.swipeRefreshLayout.isRefreshing) {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvCrypto.visibility = View.GONE
            }

            try {
                val response = cryptoRepository.getCryptoList()

                binding.progressBar.visibility = View.GONE
                binding.rvCrypto.visibility = View.VISIBLE
                binding.swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    response.body()?.let {
                        binding.rvCrypto.adapter = CryptoAdapter(it)
                    }
                } else {
                    binding.tvError.visibility = View.VISIBLE
                    binding.rvCrypto.adapter = CryptoAdapter(emptyList())
                    Log.e("HomeActivity", "API call failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.rvCrypto.visibility = View.VISIBLE
                binding.swipeRefreshLayout.isRefreshing = false
                binding.tvError.visibility = View.VISIBLE
                binding.rvCrypto.adapter = CryptoAdapter(emptyList())
                Log.e("HomeActivity", "Exception: ${e.message}")
            }
        }
    }
}
