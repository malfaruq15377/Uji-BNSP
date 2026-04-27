package com.example.daftarbuku

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.daftarbuku.data.local.model.Product
import com.example.daftarbuku.databinding.DialogAddEditProductBinding
import com.example.daftarbuku.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()
    private lateinit var foodAdapter: FoodAdapter
    
    private var selectedImageUri: Uri? = null
    private var dialogBinding: DialogAddEditProductBinding? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            dialogBinding?.ivPreview?.let { imageView ->
                Glide.with(this).load(it).into(imageView)
            }
            try {
                requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.fabAddFood.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            onDetailClick = { product ->
                val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra("PRODUCT_ID", product.id)
                    putExtra("NAMA", product.name)
                    putExtra("HARGA", product.price)
                    putExtra("DESKRIPSI", product.description)
                    putExtra("GAMBAR", product.imageUrl)
                }
                startActivity(intent)
            },
            onAddToCartClick = { product ->
                viewModel.addToCart(product)
                Toast.makeText(requireContext(), "${product.name} ditambah ke keranjang", Toast.LENGTH_SHORT).show()
            },
            onEditClick = { product ->
                showAddEditDialog(product)
            },
            onDeleteClick = { product ->
                showDeleteConfirmation(product)
            }
        )
        binding.rvFood.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = foodAdapter
        }
    }

    private fun showAddEditDialog(product: Product?) {
        dialogBinding = DialogAddEditProductBinding.inflate(LayoutInflater.from(requireContext()))
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogBinding!!.root)
        
        val dialog = builder.create()
        selectedImageUri = null 

        if (product != null) {
            dialogBinding!!.tvTitle.text = "Ubah Menu"
            dialogBinding!!.etName.setText(product.name)
            dialogBinding!!.etPrice.setText(product.price.toString())
            dialogBinding!!.etDescription.setText(product.description)
            if (product.imageUrl.isNotEmpty()) {
                selectedImageUri = Uri.parse(product.imageUrl)
                Glide.with(this).load(selectedImageUri).into(dialogBinding!!.ivPreview)
            }
        }

        dialogBinding!!.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        dialogBinding!!.btnSave.setOnClickListener {
            val name = dialogBinding!!.etName.text.toString()
            val price = dialogBinding!!.etPrice.text.toString().toLongOrNull() ?: 0L
            val description = dialogBinding!!.etDescription.text.toString()
            val imageUrl = selectedImageUri?.toString() ?: product?.imageUrl ?: ""

            if (name.isNotEmpty() && price > 0) {
                if (product == null) {
                    val newProduct = Product(name = name, price = price, description = description, imageUrl = imageUrl)
                    viewModel.addProduct(newProduct)
                } else {
                    val updatedProduct = product.copy(name = name, price = price, description = description, imageUrl = imageUrl)
                    viewModel.updateProduct(updatedProduct)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Harap isi nama dan harga dengan benar", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setOnDismissListener { dialogBinding = null }
        dialog.show()
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Menu")
            .setMessage("Apakah Anda yakin ingin menghapus ${product.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteProduct(product)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.allProducts.observe(viewLifecycleOwner) { products ->
            foodAdapter.submitList(products)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
