package com.example.smartlibrary

data class ChatMessage(
    val senderEmail: String = "",
    val receiverEmail: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)