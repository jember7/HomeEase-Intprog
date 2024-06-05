package com.capstone.homeease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.capstone.homeease.databinding.ActivityUserDashBoardBinding // Import the ViewBinding class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserDashBoard : AppCompatActivity() {

    private lateinit var binding: ActivityUserDashBoardBinding // Declare the ViewBinding variable

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        // Load and update the address
        loadUserProfile()
        binding = ActivityUserDashBoardBinding.inflate(layoutInflater) // Initialize the ViewBinding
        setContentView(binding.root)

        // Set click listeners for buttons using the ViewBinding
        binding.washing.setOnClickListener {
            val i = Intent(this, AvailableExpertsActivity::class.java );
            i.putExtra("service", "Car Washing")
            startActivity(i)
        }
        binding.security.setOnClickListener {
            val i = Intent(this, AvailableExpertsActivity::class.java );
            i.putExtra("service", "Home Security")
            startActivity(i)
        }
        binding.homeservice.setOnClickListener {
            val i = Intent(this, AvailableExpertsActivity::class.java );
            i.putExtra("service", "Home Service")
            startActivity(i)
        }
        binding.laundry.setOnClickListener {
            val i = Intent(this, AvailableExpertsActivity::class.java );
            i.putExtra("service", "Laundry")
            startActivity(i)
        }
        binding.plumber.setOnClickListener {
            val i = Intent(this, AvailableExpertsActivity::class.java );
            i.putExtra("service", "Plumbing")
            startActivity(i)
        }

        binding.renting.setOnClickListener {
            val i = Intent(this, AvailableExpertsActivity::class.java );
            i.putExtra("service", "Electrician")
            startActivity(i)
        }
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

    }
    override fun onBackPressed() {
        super.onBackPressed()
        // Show a toast message when back button is pressed
        Toast.makeText(this, "Back button is pressed.", Toast.LENGTH_SHORT).show()
    }
    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {

                        val address = document.getString("address")
                        binding.address.text = address


                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("EditUserProfileActivity", "Error getting user profile", exception)
                }
        }
    }


}
