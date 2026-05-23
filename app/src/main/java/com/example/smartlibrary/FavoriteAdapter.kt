package com.example.smartlibrary

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class FavoriteAdapter(private val books: List<FavoriteBook>) :
    RecyclerView.Adapter<FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val book = books[position]

        holder.title.text = "⭐ ${book.rating} - ${book.title}"

        if (book.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(book.imageUrl.replace("http://", "https://"))
                .into(holder.image)
        } else {
            holder.image.setImageResource(android.R.color.darker_gray)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookDetailActivity::class.java)

            intent.putExtra("title", book.title)
            intent.putExtra("author", book.author)
            intent.putExtra("description", book.description)
            intent.putExtra("imageUrl", book.imageUrl)
            intent.putExtra("rating", book.rating)
            intent.putExtra("review", book.review)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = books.size
}