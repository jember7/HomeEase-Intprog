package com.capstone.homeease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.homeease.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set images
        binding.imageView1.setImageResource(R.drawable.img)
        binding.imageView2.setImageResource(R.drawable.img_35)
        binding.imageView3.setImageResource(R.drawable.img_36)

        // Set click listeners
        binding.imageView2.setOnClickListener {
            val intent = Intent(this, UserRegistration::class.java)
            startActivity(intent)
        }

        binding.imageView3.setOnClickListener {
            val intent = Intent(this, ExpertRegistration::class.java)
            startActivity(intent)
        }

        binding.login.setOnClickListener {
            val intent = Intent(this, LoginPageActivity::class.java)
            startActivity(intent)
        }

    }
    override fun onBackPressed() {
        // Do nothing, or show a message
        Toast.makeText(this, "Back button is disabled.", Toast.LENGTH_SHORT).show()
    }
}
