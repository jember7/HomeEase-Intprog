package com.capstone.homeease

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserBookingsActivity : AppCompatActivity() {

    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: OngoingAdapter
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_dash_board)

        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        bookingsAdapter = OngoingAdapter(this,emptyList())
        bookingsRecyclerView.adapter = bookingsAdapter

        fetchUserBookings()
    }

    private fun fetchUserBookings() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val bookings = documents.map { document ->
                        Booking(
                            id = document.id,
                            expertId = document.getString("expertId") ?: "",
                            userId = document.getString("userId") ?: "",
                            status = document.getString("status") ?: "",
                            timestamp = document.getLong("timestamp") ?: 0
                        )
                    }
                    bookingsAdapter.updateBookings(bookings)
                }
                .addOnFailureListener { e ->
                    Log.e("UserBookingsActivity", "Error fetching bookings", e)
                }
        } else {
            Log.e("UserBookingsActivity", "User not authenticated")
        }
    }
}
