package com.example.smartlibrary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteBookDao {

    @Insert
    suspend fun insert(book: FavoriteBook)

    @Query("SELECT * FROM favorite_books")
    suspend fun getAllBooks(): List<FavoriteBook>

    @Query("DELETE FROM favorite_books WHERE title = :title")
    suspend fun deleteByTitle(title: String)
}