package com.capstone.homeease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AvailableExpertsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var expertsAdapter: ExpertsAdapter
    private lateinit var experts: MutableList<Expert>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_experts)

        // Find ImageViews by their IDs
        val activityImageView = findViewById<ImageView>(R.id.activity)
        val paymentImageView = findViewById<ImageView>(R.id.payment)
        val textHomeImageView = findViewById<ImageView>(R.id.textHome)
        val messagesImageView = findViewById<ImageView>(R.id.messages)
        val profileImageView = findViewById<ImageView>(R.id.profile)

        // Set click listeners for ImageViews
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

        // Initialize Firestore and RecyclerView
        firestore = FirebaseFirestore.getInstance()
        experts = mutableListOf()
        expertsAdapter = ExpertsAdapter(this, experts) // Pass the context

        val expertsRecyclerView = findViewById<RecyclerView>(R.id.expertsRecyclerView)
        expertsRecyclerView.layoutManager = LinearLayoutManager(this)
        expertsRecyclerView.adapter = expertsAdapter

        val service = intent.getStringExtra("service")
        val titleTextView = findViewById<TextView>(R.id.title)
        titleTextView.text = service
        fetchExperts(service)
    }

    private fun fetchExperts(profession: String?) {
        firestore.collection("users")
            .whereEqualTo("role", "Expert")
            .whereEqualTo("profession", profession)
            .get()
            .addOnSuccessListener { documents ->
                experts.clear()
                for (document in documents) {
                    val expert = document.toObject(Expert::class.java).apply {
                        id = document.id // Set the id field to the document ID
                        imageUrl = document.getString("imageUrl") ?: ""
                    }
                    experts.add(expert)
                }
                expertsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("AvailableExpertsActivity", "Error getting documents: ", exception)
                Toast.makeText(this, "Failed to fetch experts", Toast.LENGTH_SHORT).show()
            }
    }

}
