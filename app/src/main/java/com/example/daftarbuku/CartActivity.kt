package com.example.daftarbuku

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.daftarbuku.data.local.model.CartItem
import com.example.daftarbuku.databinding.ActivityCartBinding
import java.text.NumberFormat
import java.util.*

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private val viewModel: FoodViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCheckout.setOnClickListener {
            if (cartAdapter.itemCount > 0) {
                viewModel.checkout()
                showDialogOrderBerhasill()
            } else {
                Toast.makeText(this, "Keranjang Anda kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onDeleteClick = { cartItem ->
                showDeleteConfirmation(cartItem)
            }
        )
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun showDeleteConfirmation(cartItem: CartItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus dari Keranjang")
            .setMessage("Hapus ${cartItem.productName}?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.removeFromCart(cartItem)
                Toast.makeText(this, "Item dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(this) { items ->
            cartAdapter.submitList(items)
            
            var total = 0L
            items.forEach { total += it.productPrice }
            
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            binding.tvTotal.text = formatRupiah.format(total)
        }
    }

    private fun showDialogOrderBerhasill() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_order_berhasil)
        dialog.setCancelable(false)
        val btnOk = dialog.findViewById<Button>(R.id.btn_ok)

        btnOk.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
