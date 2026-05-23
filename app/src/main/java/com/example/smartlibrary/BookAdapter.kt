package com.example.smartlibrary

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class BookAdapter(private val books: List<Item>) :
    RecyclerView.Adapter<BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)

        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        val volumeInfo = book.volumeInfo

        val title = volumeInfo.title ?: "No Title"
        val author = volumeInfo.authors?.joinToString(", ") ?: "Unknown Author"
        val description = volumeInfo.description ?: "No description available"

        val imageUrl = volumeInfo.imageLinks?.thumbnail
            ?.replace("http://", "https://")
            ?: ""

        holder.titleText.text = title
        holder.authorText.text = author

        if (imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.bookImage)
        } else {
            holder.bookImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailActivity::class.java)

            intent.putExtra("title", title)
            intent.putExtra("author", author)
            intent.putExtra("description", description)
            intent.putExtra("imageUrl", imageUrl)

            intent.putExtra("googleRating", volumeInfo.averageRating ?: 0.0)
            intent.putExtra("ratingsCount", volumeInfo.ratingsCount ?: 0)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }
}