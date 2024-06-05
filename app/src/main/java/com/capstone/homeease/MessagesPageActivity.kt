package com.capstone.homeease

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.homeease.databinding.ActivityMessagesPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesPageBinding
    private lateinit var bookingsAdapter: AcceptedBookingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.activity.setOnClickListener {
            val intent = Intent(this, ActivityPage::class.java)
            startActivity(intent)
        }
        binding.payment.setOnClickListener {
            val intent = Intent(this, PaymentPageActivity::class.java)
            startActivity(intent)
        }
        binding.textHome.setOnClickListener {
            val intent = Intent(this, UserDashBoard::class.java)
            startActivity(intent)
        }
        binding.messages.setOnClickListener {
            val intent = Intent(this, MessagesPageActivity::class.java)
            startActivity(intent)
        }
        binding.profile.setOnClickListener {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }

        binding.acceptedBookingsRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchAcceptedBookings()
    }

    private fun fetchAcceptedBookings() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("status", "Accepted")
                .addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        Log.e("AcceptedBookings", "Error listening for accepted bookings changes", exception)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val bookings = snapshots.mapNotNull { document ->
                            document.toObject(Booking::class.java).apply { id = document.id }
                        }
                        bookingsAdapter = AcceptedBookingsAdapter(bookings) { booking ->
                            val intent = Intent(this, ConversationActivity::class.java).apply {
                                putExtra("bookingId", booking.id)
                                putExtra("expertId", booking.expertId)
                                putExtra("expertName", booking.expertName)
                                putExtra("expertImageUrl", booking.expertImageUrl)
                            }
                            startActivity(intent)
                        }
                        binding.acceptedBookingsRecyclerView.adapter = bookingsAdapter
                    }
                }
        } else {
            Log.w("AcceptedBookings", "No user is signed in")
        }
    }

}
