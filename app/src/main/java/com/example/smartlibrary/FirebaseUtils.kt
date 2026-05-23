package com.example.smartlibrary

object FirebaseUtils {

    const val DATABASE_URL =
        "https://smartlibrary-37d61-default-rtdb.asia-southeast1.firebasedatabase.app"

    fun emailToKey(email: String): String {
        return email
            .replace(".", "_dot_")
            .replace("@", "_at_")
    }

    fun getChatId(user1: String, user2: String): String {
        val key1 = emailToKey(user1)
        val key2 = emailToKey(user2)

        return if (key1 < key2) {
            "${key1}_${key2}"
        } else {
            "${key2}_${key1}"
        }
    }
}