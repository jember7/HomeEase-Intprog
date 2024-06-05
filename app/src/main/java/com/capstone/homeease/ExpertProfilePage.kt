package com.capstone.homeease

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.capstone.homeease.databinding.ActivityExpertProfilePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore // Import the ViewBinding class

class ExpertProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityExpertProfilePageBinding // Declare the ViewBinding variable
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpertProfilePageBinding.inflate(layoutInflater) // Initialize the ViewBinding
        setContentView(binding.root)


        // Set click listeners for buttons using the ViewBinding
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
        binding.deleteAccountText.setOnClickListener {
            val intent = Intent(this, DeleteAccountActivity::class.java)
            startActivity(intent)
        }
        binding.logoutText.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut() // Sign out the current user

            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
            startActivity(intent)
            finish() // Finish current activity to prevent user from navigating back
        }
        binding.editProfile.setOnClickListener{
            val intent = Intent(this,EditExpertProfileActivity::class.java)
            startActivity(intent)
        }
        binding.manageLinkedBankText.setOnClickListener{
            val intent = Intent(this,ExpertPaymentPage::class.java)
            startActivity(intent)
        }
        binding.changePassword.setOnClickListener {
            val intent = Intent(this,ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        binding.bookingHistoryText.setOnClickListener{
            val intent = Intent(this,ExpertActivityPage::class.java)
            startActivity(intent)
        }
        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get the current user
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User is signed in, fetch user data from Firestore
            fetchUserData(currentUser)
        } else {
            // No user is signed in, handle this case accordingly
            Log.w("ProfilePageActivity", "No user is signed in")
        }
    }

    private fun fetchUserData(user: FirebaseUser) {
        val userId = user.uid

        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.d("ProfilePageActivity", "Listener failed", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val username = snapshot.getString("fullName")
                    val profileImageUrl = snapshot.getString("imageUrl")
                    val number = snapshot.getString("number")

                    val usernameTextView = findViewById<TextView>(R.id.usernameText)
                    usernameTextView.text = username ?: "Username"

                    val numberTextView = findViewById<TextView>(R.id.numberText)
                    numberTextView.text = number ?: "Number"

                    val profileImageView = findViewById<ImageView>(R.id.profilePicture)
                    profileImageUrl?.let {
                        Glide.with(this)
                            .load(it)
                            .placeholder(R.drawable.img_35) // Optional: A default image while loading
                            .error(R.drawable.img_35) // Optional: A default image if there's an error
                            .into(profileImageView)
                    }
                } else {
                    Log.d("ProfilePageActivity", "No such document")
                }
            }
    }

}

