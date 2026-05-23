package com.example.smartlibrary

data class SharedBook(
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val senderEmail: String = "",
    val receiverEmail: String = "",
    val timestamp: Long = 0L
)