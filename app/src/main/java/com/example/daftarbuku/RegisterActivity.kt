package com.example.daftarbuku

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.daftarbuku.data.local.model.User
import com.example.daftarbuku.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: FoodViewModel by viewModels()
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifikasi diizinkan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        
        if (isLoggedIn) {
            val username = sharedPref.getString("USERNAME", "")
            val email = sharedPref.getString("EMAIL", "")
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("USERNAME", username)
                putExtra("EMAIL", email)
            }
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestNotificationPermission()

        observeViewModel()

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnCreate.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.tilUsername.error = "Username tidak boleh kosong"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Email tidak valid"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.tilPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }

            val newUser = User(username = username, email = email, password = password)
            viewModel.register(newUser)
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

    private fun observeViewModel() {
        viewModel.registerStatus.observe(this) { status ->
            if (status == "Success") {
                Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    putExtra("REGISTERED_EMAIL", binding.etEmail.text.toString())
                    putExtra("REGISTERED_PASSWORD", binding.etPassword.text.toString())
                }
                startActivity(intent)
                finish()
            } else if (status != null) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
