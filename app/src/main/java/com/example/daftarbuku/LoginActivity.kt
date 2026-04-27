package com.example.daftarbuku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.daftarbuku.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: FoodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val regEmail = intent.getStringExtra("REGISTERED_EMAIL")
        val regPass = intent.getStringExtra("REGISTERED_PASSWORD")
        if (regEmail != null) binding.etUsername.setText(regEmail)
        if (regPass != null) binding.etPassword.setText(regPass)

        observeViewModel()

        binding.btnLogin.setOnClickListener {
            val input = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username/Email dan Password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(input, password)
        }
    }

    private fun observeViewModel() {
        viewModel.loginStatus.observe(this) { user ->
            Log.d("LOGIN_DEBUG", "User status: $user")
            if (user != null) {
                val sharedPref = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putBoolean("IS_LOGGED_IN", true)
                    putString("USERNAME", user.username)
                    putString("EMAIL", user.email)
                    apply()
                }

                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("USERNAME", user.username)
                    putExtra("EMAIL", user.email)
                }
                startActivity(intent)
                finish()
            } else {
                if (viewModel.loginStatus.value != null) {
                    Toast.makeText(this, "Login Gagal. Cek kembali Email dan Password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
