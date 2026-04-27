package com.example.daftarbuku

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.daftarbuku.data.local.model.Product
import com.example.daftarbuku.databinding.ActivityMainBinding
import com.example.daftarbuku.databinding.DialogAddEditProductBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: FoodViewModel by viewModels()
    private lateinit var foodAdapter: FoodAdapter
    
    private var selectedImageUri: Uri? = null
    private var dialogBinding: DialogAddEditProductBinding? = null

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifikasi diizinkan", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            dialogBinding?.ivPreview?.let { imageView ->
                Glide.with(this).load(it).into(imageView)
            }
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestNotificationPermission()

        val username = intent.getStringExtra("USERNAME") ?: "Pecinta Kuliner"
        val email = intent.getStringExtra("EMAIL") ?: ""
        val password = intent.getStringExtra("PASSWORD") ?: ""

        setupRecyclerView()
        observeViewModel()

        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("USERNAME", username)
                putExtra("EMAIL", email)
                putExtra("PASSWORD", password)
            }
            startActivity(intent)
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.fabAddFood.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val prefs = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
            val isFirstRun = prefs.getBoolean("IS_FIRST_RUN", true)

            if (isFirstRun) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    showNotificationExplanationDialog()
                }
                prefs.edit().putBoolean("IS_FIRST_RUN", false).apply()
            }
        }
    }

    private fun showNotificationExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izinkan Notifikasi?")
            .setMessage("Kami ingin mengirimkan notifikasi terkait pesanan dan promo menarik untuk Anda. Apakah Anda bersedia?")
            .setPositiveButton("Izinkan") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Nanti Saja", null)
            .setCancelable(false)
            .show()
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            onDetailClick = { product ->
                val intent = Intent(this, DetailActivity::class.java).apply {
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
                Toast.makeText(this, "${product.name} ditambah ke keranjang", Toast.LENGTH_SHORT).show()
            },
            onEditClick = { product ->
                showAddEditDialog(product)
            },
            onDeleteClick = { product ->
                showDeleteConfirmation(product)
            }
        )
        binding.rvFood.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = foodAdapter
        }
    }

    private fun showAddEditDialog(product: Product?) {
        dialogBinding = DialogAddEditProductBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this)
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
                Toast.makeText(this, "Harap isi nama dan harga dengan benar", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setOnDismissListener { dialogBinding = null }
        dialog.show()
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Menu")
            .setMessage("Apakah Anda yakin ingin menghapus ${product.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteProduct(product)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.allProducts.observe(this) { products ->
            foodAdapter.submitList(products)
        }
    }
}
