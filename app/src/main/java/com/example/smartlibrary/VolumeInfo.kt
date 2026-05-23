package com.example.smartlibrary

data class VolumeInfo(
    val title: String? = null,
    val authors: List<String>? = null,
    val description: String? = null,
    val imageLinks: ImageLinks? = null,
    val averageRating: Double? = null,
    val ratingsCount: Int? = null
)