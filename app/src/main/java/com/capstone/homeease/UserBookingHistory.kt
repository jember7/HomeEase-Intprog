package com.capstone.homeease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.homeease.databinding.ActivityUserBookingHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserBookingHistory : AppCompatActivity() {

    private lateinit var ongoingRecyclerView: RecyclerView
    private lateinit var ongoingAdapter: BookingsAdapter

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        val activityImageView = findViewById<ImageView>(R.id.activity)
        val paymentImageView = findViewById<ImageView>(R.id.payment)
        val textHomeImageView = findViewById<ImageView>(R.id.textHome)
        val messagesImageView = findViewById<ImageView>(R.id.messages)
        val profileImageView = findViewById<ImageView>(R.id.profile)

        // Set click listeners for image views
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


        ongoingRecyclerView.layoutManager = LinearLayoutManager(this)


        ongoingAdapter = BookingsAdapter(this, mutableListOf())

        ongoingRecyclerView.adapter = ongoingAdapter


        fetchBookings()
    }

    private fun fetchBookings() {
        val userId = auth.currentUser?.uid
        FirebaseFirestore.getInstance().collection("bookings")
            .whereEqualTo("userId", userId)
            .whereIn("status", listOf("Completed", "Cancelled", "Declined")) // Use whereIn to filter by multiple statuses
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Handle failure
                    Log.e("UserBookingsActivity", "Error getting bookings", exception)
                    return@addSnapshotListener
                }

                val bookings = mutableListOf<Booking>()
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val id = document.id
                        val expertName = document.getString("expertName") ?: ""
                        val expertAddress = document.getString("expertAddress") ?: ""
                        val expertImageUrl = document.getString("expertImageUrl") ?: ""
                        val status = document.getString("status") ?: ""
                        val timestamp = document.getLong("timestamp") ?: 0
                        // Create a Booking object and add it to the list
                        val booking = Booking(id, "", expertName, "", "", status, timestamp,"","",expertAddress,expertImageUrl)
                        bookings.add(booking)
                    }
                }
                // Update the adapter with the retrieved bookings
                ongoingAdapter.setNewBookings(bookings)
            }
    }


}
