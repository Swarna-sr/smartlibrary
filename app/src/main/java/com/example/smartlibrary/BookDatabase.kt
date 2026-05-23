package com.example.smartlibrary

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteBook::class, User::class],
    version = 3,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {

    abstract fun favoriteBookDao(): FavoriteBookDao

    // 🔥 ADD THIS
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(context: Context): BookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "book_database"
                )
                    // ⚠️ TEMPORARY (for development only)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}