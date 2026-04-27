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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.daftarbuku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifikasi diizinkan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAndRequestNotificationPermission()
        setupBottomNavigation()

        handleIntent(intent)

        if (savedInstanceState == null && intent.getStringExtra("OPEN_PAGE") == null) {
            replaceFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.navigation_home
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val page = intent?.getStringExtra("OPEN_PAGE")
        when (page) {
            "CART" -> {
                binding.bottomNavigation.selectedItemId = R.id.navigation_cart
                replaceFragment(CartFragment())
            }
            "HISTORY" -> {
                binding.bottomNavigation.selectedItemId = R.id.navigation_history
                replaceFragment(HistoryFragment())
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_cart -> {
                    replaceFragment(CartFragment())
                    true
                }
                R.id.navigation_history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
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
}
