package com.example.nandosokcerdas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nandosokcerdas.adapter.FavoritesAdapter
import com.example.nandosokcerdas.data.model.Crypto
import com.example.nandosokcerdas.databinding.ActivityFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var favoritesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvFavorites.layoutManager = LinearLayoutManager(this)
        listenForFavoriteChanges()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hentikan listener saat activity dihancurkan untuk mencegah memory leak
        favoritesListener?.remove()
    }

    private fun listenForFavoriteChanges() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            binding.tvNoFavorites.visibility = View.VISIBLE
            binding.rvFavorites.visibility = View.GONE
            return
        }

        val query = firestore.collection("users").document(userId).collection("favorites")
        
        favoritesListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                binding.tvNoFavorites.text = "Gagal memuat favorit."
                binding.tvNoFavorites.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                binding.tvNoFavorites.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
                return@addSnapshotListener
            }

            val favoriteList = snapshot.documents.map { doc ->
                Crypto(
                    id = doc.getString("id") ?: "",
                    name = doc.getString("name") ?: "",
                    symbol = doc.getString("symbol") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    price = 0.0,
                    change24h = 0.0
                )
            }

            binding.tvNoFavorites.visibility = View.GONE
            binding.rvFavorites.visibility = View.VISIBLE
            binding.rvFavorites.adapter = FavoritesAdapter(favoriteList) { crypto ->
                showDeleteConfirmationDialog(crypto)
            }
        }
    }

    private fun showDeleteConfirmationDialog(crypto: Crypto) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Favorit")
            .setMessage("Anda yakin ingin menghapus ${crypto.name} dari daftar favorit?")
            .setPositiveButton("Hapus") { _, _ -> deleteFavorite(crypto) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteFavorite(crypto: Crypto) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("favorites").document(crypto.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${crypto.name} dihapus dari favorit", Toast.LENGTH_SHORT).show()
                // Kita tidak perlu memanggil fetchFavorites() lagi, karena listener akan otomatis mendeteksi perubahan
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus favorit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
