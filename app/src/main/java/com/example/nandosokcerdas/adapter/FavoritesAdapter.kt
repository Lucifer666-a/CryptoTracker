package com.example.nandosokcerdas.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nandosokcerdas.DetailActivity
import com.example.nandosokcerdas.data.model.Crypto
import com.example.nandosokcerdas.databinding.ItemCryptoBinding

class FavoritesAdapter(
    private val items: List<Crypto>,
    private val onItemLongClick: (Crypto) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    // Lakukan cast eksplisit dari binding.root (CardView) ke View
    inner class FavoriteViewHolder(val binding: ItemCryptoBinding) :
        RecyclerView.ViewHolder(binding.root as View)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemCryptoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvSymbol.text = item.symbol.uppercase()

        holder.binding.tvPrice.text = ""
        holder.binding.tvChange.text = ""

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.binding.ivLogo)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("EXTRA_CRYPTO", item)
            }
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = items.size
}
