package com.capstone.homeease
import com.google.firebase.Timestamp
import java.util.Date

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Any = Timestamp.now(),  // Use Any to accept both Long and Timestamp
    val bookingId: String = ""
) {
    fun getTimestampAsDate(): Date {
        return when (timestamp) {
            is Timestamp -> timestamp.toDate()
            is Long -> Date(timestamp)
            else -> Date()
        }
    }
}
