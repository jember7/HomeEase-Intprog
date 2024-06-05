package com.capstone.homeease

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.homeease.databinding.ActivityPaymentPageBinding // Import the ViewBinding class

class ExpertPaymentPage : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentPageBinding // Declare the ViewBinding variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentPageBinding.inflate(layoutInflater) // Initialize the ViewBinding
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
    }
}
