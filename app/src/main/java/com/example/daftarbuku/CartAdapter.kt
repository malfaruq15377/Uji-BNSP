package com.example.daftarbuku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.daftarbuku.data.local.model.CartItem
import com.example.daftarbuku.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private val onDeleteClick: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback) {

    class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem, onDeleteClick: (CartItem) -> Unit) {
            binding.tvFoodName.text = cartItem.productName
            binding.tvFoodDesc.text = "Klik ikon sampah untuk menghapus"
            
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            binding.tvFoodPrice.text = formatRupiah.format(cartItem.productPrice)

            Glide.with(binding.ivFood.context)
                .load(cartItem.productImageUrl)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .into(binding.ivFood)

            binding.btnDelete.setOnClickListener {
                onDeleteClick(cartItem)
            }

            binding.btnAddToCart.visibility = android.view.View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        return CartViewHolder(
            ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean = oldItem == newItem
    }
}
