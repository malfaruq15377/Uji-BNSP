package com.example.daftarbuku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.daftarbuku.data.local.model.Product
import com.example.daftarbuku.databinding.ItemFoodBinding
import java.text.NumberFormat
import java.util.*

class FoodAdapter(
    private val onDetailClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, FoodAdapter.FoodViewHolder>(DiffCallback) {

    class FoodViewHolder(private val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            product: Product,
            onDetailClick: (Product) -> Unit,
            onAddToCartClick: (Product) -> Unit,
            onEditClick: (Product) -> Unit,
            onDeleteClick: (Product) -> Unit
        ) {
            binding.tvFoodName.text = product.name
            binding.tvFoodDesc.text = product.description
            
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            binding.tvFoodPrice.text = formatRupiah.format(product.price)

            Glide.with(binding.ivFood.context)
                .load(product.imageUrl)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .into(binding.ivFood)

            binding.root.setOnClickListener { onDetailClick(product) }
            binding.btnAddToCart.setOnClickListener { onAddToCartClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder(
            ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position), onDetailClick, onAddToCartClick, onEditClick, onDeleteClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
    }
}
