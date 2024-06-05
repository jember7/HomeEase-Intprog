package com.capstone.homeease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.homeease.databinding.ActivityExpertMessagesPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExpertMessagesPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpertMessagesPageBinding
    private lateinit var bookingsAdapter: ExpertAcceptedBookingsAdapter // Assuming you have a specific adapter for expert accepted bookings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpertMessagesPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.activity.setOnClickListener {
            val intent = Intent(this, ExpertActivityPage::class.java)
            startActivity(intent)
        }
        binding.payment.setOnClickListener {
            val intent = Intent(this, ExpertPaymentPage::class.java)
            startActivity(intent)
        }
        binding.textHome.setOnClickListener {
            val intent = Intent(this, ExpertDashBoard::class.java)
            startActivity(intent)
        }
        binding.messages.setOnClickListener {
            val intent = Intent(this, ExpertMessagesPageActivity::class.java)
            startActivity(intent)
        }
        binding.profile.setOnClickListener {
            val intent = Intent(this, ExpertProfilePage::class.java)
            startActivity(intent)
        }

        binding.acceptedBookingsRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchAcceptedBookings()
    }

    private fun fetchAcceptedBookings() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("expertId", currentUser.uid) // Assuming expertId is used to filter expert's bookings
                .whereEqualTo("status", "Accepted")
                .get()
                .addOnSuccessListener { documents ->
                    val bookings = documents.mapNotNull { document ->
                        document.toObject(Booking::class.java).apply { id = document.id }
                    }
                    bookingsAdapter = ExpertAcceptedBookingsAdapter(bookings) { booking ->
                        val intent = Intent(this, ExpertConversationActivity::class.java).apply {
                            putExtra("bookingId", booking.id)
                            putExtra("userId", booking.userId)
                            putExtra("userName", booking.userName)

                        }
                        startActivity(intent)
                    }
                    binding.acceptedBookingsRecyclerView.adapter = bookingsAdapter
                }
                .addOnFailureListener { exception ->
                    Log.e("ExpertAcceptedBookings", "Error fetching accepted bookings", exception)
                }
        } else {
            Log.w("ExpertAcceptedBookings", "No user is signed in")
        }
    }
}
