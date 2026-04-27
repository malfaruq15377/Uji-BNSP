package com.example.daftarbuku.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.daftarbuku.R
import com.example.daftarbuku.data.local.model.CartItem
import com.example.daftarbuku.data.local.model.HistoryItem
import com.example.daftarbuku.data.local.model.Product
import com.example.daftarbuku.data.local.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Product::class, User::class, CartItem::class, HistoryItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun userDao(): UserDao
    abstract fun cartDao(): CartDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snake_shop_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(private val context: Context) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.productDao())
                    }
                }
            }

            suspend fun populateDatabase(productDao: ProductDao) {
                val packageName = context.packageName
                val dummyProducts = listOf(
                    Product(
                        name = "Coffe",
                        price = 15000,
                        description = "Teman setia buat bangun pagi atau sekadar teman ngobrol sore. Aroma kopinya bikin rileks!",
                        imageUrl = "android.resource://$packageName/${R.drawable.img}"
                    ),
                    Product(
                        name = "Sandwich",
                        price = 27000, 
                        description = "Isian melimpah dalam balutan roti lembut. Praktis, kenyang, dan pastinya lezat.",
                        imageUrl = "android.resource://$packageName/${R.drawable.img_1}"
                    ),
                    Product(
                        name = "Kentang Goreng",
                        price = 30000, 
                        description = "Camilan wajib! Renyah di luar, lembut di dalam. Sekali coba nggak bakal bisa berhenti.",
                        imageUrl = "android.resource://$packageName/${R.drawable.img_2}"
                    ),
                    Product(
                        name = "Spageti",
                        price = 28000, 
                        description = "Pasta al dente dengan siraman saus otentik yang kaya rasa. Cita rasa Italia di setiap garpu.",
                        imageUrl = "android.resource://$packageName/${R.drawable.img_3}"
                    ),
                    Product(
                        name = "Steak",
                        price = 32000, 
                        description = "Juicy, empuk, dan penuh rasa. Disajikan dengan saus rahasia yang bikin nagih!",
                        imageUrl = "android.resource://$packageName/${R.drawable.img_4}"
                    ),
                    Product(
                        name = "Pizza",
                        price = 35000,
                        description = "Adonan tipis nan renyah dengan topping premium dan keju mozzarella yang meleleh sempurna. Cita rasa klasik yang tak tertandingi.",
                        imageUrl = "android.resource://$packageName/${R.drawable.img_7}"
                    )
                )
                
                for (product in dummyProducts) {
                    productDao.insertProduct(product)
                }
            }
        }
    }
}
