package com.example.daftarbuku

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.daftarbuku.data.local.model.Product
import com.example.daftarbuku.databinding.ActivityDetailBinding
import com.example.daftarbuku.databinding.DialogAddEditProductBinding
import java.text.NumberFormat
import java.util.*

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: FoodViewModel by viewModels()
    private var currentProduct: Product? = null

    private var selectedImageUri: Uri? = null
    private var dialogBinding: DialogAddEditProductBinding? = null

    companion object {
        private const val CHANNEL_ID = "cart_notification_channel"
        private var notificationIdCounter = 1001
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            currentProduct?.let { sendNotification(it.name) }
        } else {
            Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        val nama = intent.getStringExtra("NAMA") ?: ""
        val harga = intent.getLongExtra("HARGA", 0L)
        val deskripsi = intent.getStringExtra("DESKRIPSI") ?: ""
        val gambar = intent.getStringExtra("GAMBAR") ?: ""

        currentProduct = Product(id = productId, name = nama, price = harga, description = deskripsi, imageUrl = gambar)
        displayProduct(currentProduct!!)

        binding.btnCart.setOnClickListener {
            currentProduct?.let {
                viewModel.addToCart(it)
                sendNotification(it.name)
                showDialogSuccessAddToCart()
            }
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEdit.setOnClickListener {
            currentProduct?.let { showAddEditDialog(it) }
        }

        binding.btnDelete.setOnClickListener {
            currentProduct?.let { showDeleteConfirmation(it) }
        }

        binding.btnBeli.setOnClickListener {
            currentProduct?.let {
                viewModel.buyNow(it)
                showDialogOrderBerhasil()
            }
        }
    }

    private fun displayProduct(product: Product) {
        binding.tvNama.text = product.name
        binding.tvDeskripsi.text = product.description
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        binding.tvHarga.text = formatRupiah.format(product.price)
        Glide.with(this).load(product.imageUrl).placeholder(android.R.drawable.ic_menu_gallery).into(binding.ivMenu)
    }

    private fun showAddEditDialog(product: Product) {
        dialogBinding = DialogAddEditProductBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this)
            .setView(dialogBinding!!.root)
        
        val dialog = builder.create()
        selectedImageUri = null 

        dialogBinding!!.tvTitle.text = "Ubah Menu"
        dialogBinding!!.etName.setText(product.name)
        dialogBinding!!.etPrice.setText(product.price.toString())
        dialogBinding!!.etDescription.setText(product.description)
        if (product.imageUrl.isNotEmpty()) {
            selectedImageUri = Uri.parse(product.imageUrl)
            Glide.with(this).load(selectedImageUri).into(dialogBinding!!.ivPreview)
        }

        dialogBinding!!.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        dialogBinding!!.btnSave.setOnClickListener {
            val name = dialogBinding!!.etName.text.toString()
            val price = dialogBinding!!.etPrice.text.toString().toLongOrNull() ?: 0L
            val description = dialogBinding!!.etDescription.text.toString()
            val imageUrl = selectedImageUri?.toString() ?: product.imageUrl

            if (name.isNotEmpty() && price > 0) {
                val updatedProduct = product.copy(name = name, price = price, description = description, imageUrl = imageUrl)
                viewModel.updateProduct(updatedProduct)
                currentProduct = updatedProduct
                displayProduct(updatedProduct)
                Toast.makeText(this, "Menu berhasil diperbarui", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "${product.name} berhasil dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Keranjang"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = "Notifikasi saat menambah produk ke keranjang"
                enableLights(true)
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(productName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("OPEN_PAGE", "CART")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_keranjang)
            .setContentTitle("Keranjang")
            .setContentText("Produk $productName berhasil ditambahkan ke keranjang")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setFullScreenIntent(pendingIntent, true) 
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)
        try {
            notificationManager.notify(notificationIdCounter++, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showDialogSuccessAddToCart() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_cart_success)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.findViewById<Button>(R.id.btn_go_to_cart).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("OPEN_PAGE", "CART")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
        dialog.findViewById<Button>(R.id.btn_close).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDialogOrderBerhasil() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_order_berhasil)
        dialog.setCancelable(false)
        val btnOk = dialog.findViewById<Button>(R.id.btn_ok)

        btnOk.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("OPEN_PAGE", "HISTORY")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
