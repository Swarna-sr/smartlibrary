package com.example.smartlibrary

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
    val titleText: TextView = itemView.findViewById(R.id.bookTitle)
    val authorText: TextView = itemView.findViewById(R.id.bookAuthor)
}