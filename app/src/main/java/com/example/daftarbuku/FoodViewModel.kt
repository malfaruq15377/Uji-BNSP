package com.example.daftarbuku

import android.app.Application
import androidx.lifecycle.*
import com.example.daftarbuku.data.local.model.CartItem
import com.example.daftarbuku.data.local.model.HistoryItem
import com.example.daftarbuku.data.local.model.Product
import com.example.daftarbuku.data.local.model.User
import com.example.daftarbuku.data.local.room.AppDatabase
import kotlinx.coroutines.launch

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FoodRepository
    val allProducts: LiveData<List<Product>>
    val cartItems: LiveData<List<CartItem>>
    val allHistory: LiveData<List<HistoryItem>>

    private val _loginStatus = MutableLiveData<User?>()
    val loginStatus: LiveData<User?> = _loginStatus

    private val _registerStatus = MutableLiveData<String?>()
    val registerStatus: LiveData<String?> = _registerStatus

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FoodRepository(db)
        allProducts = repository.allProducts.asLiveData()
        cartItems = repository.cartItems.asLiveData()
        allHistory = repository.allHistory.asLiveData()
    }

    fun refreshProducts() = viewModelScope.launch {
        try {
            repository.refreshProducts()
        } catch (e: Exception) {
        }
    }

    fun addProduct(product: Product) = viewModelScope.launch {
        repository.insertProduct(product)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.deleteProduct(product)
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        val user = repository.getUserByEmail(email)
        if (user != null && user.password == password) {
            _loginStatus.postValue(user)
        } else {
            _loginStatus.postValue(null)
        }
    }

    fun register(user: User) = viewModelScope.launch {
        try {
            if (repository.getUserByEmail(user.email) == null) {
                repository.register(user)
                _registerStatus.postValue("Success")
            } else {
                _registerStatus.postValue("Email already registered")
            }
        } catch (e: Exception) {
            _registerStatus.postValue("Error: ${e.message}")
        }
    }

    fun addToCart(product: Product) = viewModelScope.launch {
        repository.addToCart(product)
    }

    fun removeFromCart(cartItem: CartItem) = viewModelScope.launch {
        repository.removeFromCart(cartItem)
    }

    fun checkout() = viewModelScope.launch {
        repository.checkout()
    }

    fun buyNow(product: Product) = viewModelScope.launch {
        repository.buyNow(product)
    }
}
