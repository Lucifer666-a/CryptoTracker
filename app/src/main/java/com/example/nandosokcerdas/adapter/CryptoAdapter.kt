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

class CryptoAdapter(private val items: List<Crypto>) :
    RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    // Lakukan cast eksplisit dari binding.root (CardView) ke View
    inner class CryptoViewHolder(val binding: ItemCryptoBinding) :
        RecyclerView.ViewHolder(binding.root as View)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val binding = ItemCryptoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CryptoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvSymbol.text = item.symbol.uppercase()
        holder.binding.tvPrice.text = "$${item.price}"

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.binding.ivLogo)

        val color = if (item.change24h >= 0) "#00FF7F" else "#FF4444"
        holder.binding.tvChange.setTextColor(android.graphics.Color.parseColor(color))
        holder.binding.tvChange.text = "${String.format("%.2f", item.change24h)}%"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("EXTRA_CRYPTO", item)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}
