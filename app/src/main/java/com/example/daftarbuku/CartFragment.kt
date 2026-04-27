package com.example.daftarbuku

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.daftarbuku.data.local.model.CartItem
import com.example.daftarbuku.databinding.FragmentCartBinding
import java.text.NumberFormat
import java.util.*

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.btnCheckout.setOnClickListener {
            if (cartAdapter.itemCount > 0) {
                viewModel.checkout()
                showDialogOrderBerhasill()
            } else {
                Toast.makeText(requireContext(), "Keranjang Anda kosong", Toast.LENGTH_SHORT).show()
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
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun showDeleteConfirmation(cartItem: CartItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus dari Keranjang")
            .setMessage("Hapus ${cartItem.productName}?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.removeFromCart(cartItem)
                Toast.makeText(requireContext(), "Item dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)
            
            var total = 0L
            items.forEach { total += it.productPrice }
            
            val localeID = Locale("in", "ID")
            val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
            binding.tvTotal.text = formatRupiah.format(total)
        }
    }

    private fun showDialogOrderBerhasill() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_order_berhasil)
        dialog.setCancelable(false)
        val btnOk = dialog.findViewById<Button>(R.id.btn_ok)

        btnOk.setOnClickListener {
            dialog.dismiss()
            (activity as? MainActivity)?.binding?.bottomNavigation?.selectedItemId = R.id.navigation_history
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
