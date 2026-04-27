package com.example.daftarbuku

import com.example.daftarbuku.data.local.model.CartItem
import com.example.daftarbuku.data.local.model.HistoryItem
import com.example.daftarbuku.data.local.model.Product
import com.example.daftarbuku.data.local.model.User
import com.example.daftarbuku.data.local.room.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class FoodRepository(private val db: AppDatabase) {
    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()

    suspend fun insertProduct(product: Product) = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: Product) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(product: Product) = db.productDao().deleteProduct(product)

    suspend fun refreshProducts() {
        val initialData = listOf(
            Product(
                name = "Nasi Goreng Spesial",
                price = 25000,
                description = "Nasi goreng dengan telur, ayam, dan kerupuk.",
                imageUrl = "https://img.kurio.network/7n-Z_Z-ZpQ-0-ZpQ/800/600/nasi-goreng.jpg"
            ),
            Product(
                name = "Mie Ayam Jamur",
                price = 18000,
                description = "Mie ayam dengan topping jamur merang segar.",
                imageUrl = "https://img-global.cpcdn.com/recipes/5e8d8d3d5f3a7c3a/680x482cq70/mie-ayam-jamur-foto-resep-utama.jpg"
            ),
            Product(
                name = "Sate Ayam Madura",
                price = 30000,
                description = "10 tusuk sate ayam dengan bumbu kacang kental.",
                imageUrl = "https://awsimages.detik.net.id/community/media/visual/2022/03/16/resep-sate-ayam-madura_43.jpeg"
            ),
            Product(
                name = "Ayam Bakar Taliwang",
                price = 35000,
                description = "Ayam bakar khas Lombok dengan sambal pedas.",
                imageUrl = "https://asset.kompas.com/crops/O3vG7p0D0r2w-r-LpXfVv9nKqE0=/0x0:1000x667/750x500/data/photo/2020/12/29/5feba54c3a3c9.jpg"
            )
        )
        db.productDao().deleteAllProducts()
        db.productDao().insertProducts(initialData)
    }

    suspend fun getUserByEmail(email: String): User? = db.userDao().getUserByEmail(email)
    suspend fun register(user: User) = db.userDao().registerUser(user)

    val cartItems: Flow<List<CartItem>> = db.cartDao().getCartItems()
    suspend fun addToCart(product: Product) {
        val cartItem = CartItem(
            productId = product.id,
            productName = product.name,
            productPrice = product.price,
            productImageUrl = product.imageUrl
        )
        db.cartDao().addToCart(cartItem)
    }
    suspend fun updateCart(cartItem: CartItem) = db.cartDao().updateCartItem(cartItem)
    suspend fun removeFromCart(cartItem: CartItem) = db.cartDao().deleteCartItem(cartItem)
    
    suspend fun checkout() {
        val items = cartItems.first()
        if (items.isNotEmpty()) {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val currentDate = sdf.format(Date())
            
            val historyItems = items.map {
                HistoryItem(
                    productName = it.productName,
                    productPrice = it.productPrice,
                    purchaseDate = currentDate,
                    productImageUrl = it.productImageUrl
                )
            }
            db.historyDao().insertAllHistory(historyItems)
            db.cartDao().clearCart()
        }
    }

    val allHistory: Flow<List<HistoryItem>> = db.historyDao().getAllHistory()
}
