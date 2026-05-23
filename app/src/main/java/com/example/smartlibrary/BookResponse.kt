package com.example.smartlibrary

data class BookResponse(
    val items: List<Item> = emptyList()
)