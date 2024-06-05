package com.capstone.homeease

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView // Import ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.homeease.databinding.ActivityExpertDashBoardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ExpertDashBoard : AppCompatActivity() {
    private lateinit var binding: ActivityExpertDashBoardBinding // Declare the ViewBinding variable
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: ExpertBookingsAdapter
    private lateinit var OngoingAdapter: OngoingAdapter
    private lateinit var ongoingBookingsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_dash_board)

        // Find ImageViews by their IDs
        val activityImageView = findViewById<ImageView>(R.id.activity)
        val paymentImageView = findViewById<ImageView>(R.id.payment)
        val textHomeImageView = findViewById<ImageView>(R.id.textHome)
        val messagesImageView = findViewById<ImageView>(R.id.messages)
        val profileImageView = findViewById<ImageView>(R.id.profile)

        // Set click listeners for ImageViews
        activityImageView.setOnClickListener {
            val intent = Intent(this, ExpertActivityPage::class.java)
            startActivity(intent)
        }
        paymentImageView.setOnClickListener {
            val intent = Intent(this, ExpertPaymentPage::class.java)
            startActivity(intent)
        }
        textHomeImageView.setOnClickListener {
            val intent = Intent(this, ExpertDashBoard::class.java)
            startActivity(intent)
        }
        messagesImageView.setOnClickListener {
            val intent = Intent(this, ExpertMessagesPageActivity::class.java)
            startActivity(intent)
        }
        profileImageView.setOnClickListener {
            val intent = Intent(this, ExpertProfilePage::class.java)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        ongoingBookingsRecyclerView = findViewById(R.id.ongoingBookingsRecyclerView)
        bookingsAdapter = ExpertBookingsAdapter(this, emptyList())

        bookingsRecyclerView.adapter = bookingsAdapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        ongoingBookingsRecyclerView.layoutManager = layoutManager
        OngoingAdapter = OngoingAdapter(this, emptyList())
        ongoingBookingsRecyclerView.adapter = OngoingAdapter// Get the current user
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is signed in, fetch user data from Firestore
            fetchBookings()
            fetchOngoingBookings()
            fetchUserData(currentUser)
        } else {
            // No user is signed in, handle this case accordingly
            Log.w("ExpertDashBoard", "No user is signed in")
        }
    }
    private fun fetchBookings() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val expertId = user.uid

            // Listen for real-time updates to bookings with the status "Pending"
            firestore.collection("bookings")
                .whereEqualTo("expertId", expertId)
                .whereEqualTo("status", "Pending")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("ExpertDashboard", "Error listening for booking changes: $e")
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val bookings = mutableListOf<Booking>()
                        for (document in snapshots) {
                            val id = document.id
                            val userId = document.getString("userId") ?: ""
                            val status = document.getString("status") ?: ""
                            val timestamp = document.getLong("timestamp") ?: 0
                            val expertName = document.getString("expertName") ?: ""
                            val userName = document.getString("userName") ?: ""
                            val note = document.getString("note") ?: ""
                            val rate = document.getString("rate") ?: ""
                            val userAddress = document.getString("userAddress") ?: ""
                            val booking = Booking(id, expertId, expertName, userId, userName, status, timestamp, note, rate,"","",userAddress)
                            bookings.add(booking)
                        }
                        bookingsAdapter.updateBookings(bookings)
                    }
                }
        }
    }
    override fun onBackPressed() {
        // Do nothing, or show a message
        Toast.makeText(this, "Back button is disabled.", Toast.LENGTH_SHORT).show()
    }
    private fun fetchOngoingBookings() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val expertId = user.uid

            // Listen for real-time updates to bookings with the status "Accepted"
            firestore.collection("bookings")
                .whereEqualTo("expertId", expertId)
                .whereEqualTo("status", "Accepted")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("ExpertDashboard", "Error listening for ongoing booking changes: $e")
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val ongoingBookings = mutableListOf<Booking>()
                        for (document in snapshots) {
                            val id = document.id
                            val userId = document.getString("userId") ?: ""
                            val status = document.getString("status") ?: ""
                            val timestamp = document.getLong("timestamp") ?: 0
                            val expertName = document.getString("expertName") ?: ""
                            val userName = document.getString("userName") ?: ""
                            val note = document.getString("note") ?: ""
                            val rate = document.getString("rate") ?: ""
                            val userAddress = document.getString("userAddress") ?: ""
                            val booking = Booking(id, expertId, expertName, userId, userName, status, timestamp, note, rate,"","", userAddress)
                            ongoingBookings.add(booking)
                        }
                        OngoingAdapter.updateBookings(ongoingBookings)
                    }
                }
        }
    }

    private fun fetchUserData(user: FirebaseUser) {
        val userId = user.uid

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("fullName")
                    val profileImageUrl = document.getString("imageUrl")
                    val number = document.getString("number")
                    val usernameTextView = findViewById<TextView>(R.id.usernameText)
                    usernameTextView.text = username ?: "Username"
                    val numberTextView = findViewById<TextView>(R.id.numberText)
                    numberTextView.text = number ?: "Add Number"
                    val profileImageView = findViewById<ImageView>(R.id.profilePicture)
                    profileImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .placeholder(R.drawable.img_35) // Optional: A default image while loading
                            .error(R.drawable.img_35) // Optional: A default image if there's an error
                            .into(profileImageView)
                    }
                } else {
                    Log.d("ExpertDashBoard", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ExpertDashBoard", "get failed with ", exception)
            }
    }

}
