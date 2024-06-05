package com.capstone.homeease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityPage : AppCompatActivity() {

    private lateinit var ongoingRecyclerView: RecyclerView
    private lateinit var pendingRecyclerView: RecyclerView
    private lateinit var ongoingAdapter: BookingsAdapter
    private lateinit var pendingAdapter: BookingsAdapter
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page)
        val activityImageView = findViewById<ImageView>(R.id.activity)
        val paymentImageView = findViewById<ImageView>(R.id.payment)
        val textHomeImageView = findViewById<ImageView>(R.id.textHome)
        val messagesImageView = findViewById<ImageView>(R.id.messages)
        val profileImageView = findViewById<ImageView>(R.id.profile)
        val history = findViewById<Button>(R.id.historyButton)
        // Set click listeners for image views
        history.setOnClickListener {
            val intent = Intent(this, UserBookingHistory::class.java)
            startActivity(intent)
        }
        activityImageView.setOnClickListener {
            val intent = Intent(this, ActivityPage::class.java)
            startActivity(intent)
        }
        paymentImageView.setOnClickListener {
            val intent = Intent(this, PaymentPageActivity::class.java)
            startActivity(intent)
        }
        textHomeImageView.setOnClickListener {
            val intent = Intent(this, UserDashBoard::class.java)
            startActivity(intent)
        }
        messagesImageView.setOnClickListener {
            val intent = Intent(this, MessagesPageActivity::class.java)
            startActivity(intent)
        }
        profileImageView.setOnClickListener {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }
        ongoingRecyclerView = findViewById(R.id.ongoingRecyclerView)
        pendingRecyclerView = findViewById(R.id.pendingRecyclerView)

        ongoingRecyclerView.layoutManager = LinearLayoutManager(this)
        pendingRecyclerView.layoutManager = LinearLayoutManager(this)

        ongoingAdapter = BookingsAdapter(this, mutableListOf())
        pendingAdapter = BookingsAdapter(this, mutableListOf())
        ongoingRecyclerView.adapter = ongoingAdapter
        pendingRecyclerView.adapter = pendingAdapter

        fetchBookings()
    }

    private fun fetchBookings() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("BookingsActivity", "Error listening for booking changes: $e")
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val ongoingBookings = mutableListOf<Booking>()
                        val pendingBookings = mutableListOf<Booking>()

                        for (document in snapshots) {
                            val booking = document.toObject(Booking::class.java)
                            booking.id = document.id
                            booking.expertAddress = document.getString("expertAddress") ?: ""
                            booking.expertImageUrl = document.getString("expertImageUrl")?:""
                            if (booking.status == "Accepted") {
                                ongoingBookings.add(booking)
                            } else if (booking.status == "Pending") {
                                pendingBookings.add(booking)
                            }
                        }

                        ongoingAdapter.updateBookings(ongoingBookings)
                        pendingAdapter.updateBookings(pendingBookings)
                    }
                }
        } else {
            // Handle the case where the user is not authenticated
            Log.w("BookingsActivity", "No user is signed in")
        }
    }

}
