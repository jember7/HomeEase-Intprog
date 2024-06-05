package com.capstone.homeease

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.homeease.databinding.ActivityLoginPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        firestore = Firebase.firestore

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setTitle("Logging In")
            setMessage("Please wait...")
            setCancelable(false)
        }

        // Set click listener for the proceed button
        binding.submitButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password must not be empty", Toast.LENGTH_SHORT).show()
            } else {
                signIn(email, password)
            }
        }

        // Set click listener for Google sign-in button (optional)


        // Set click listener for the sign-up and forgot password texts
        binding.signUpText.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String) {
        progressDialog.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Sign in success, get the signed-in user's information
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        fetchUserRole(it) // Remove source argument
                    }
                } else {
                    // If sign-in fails, display a message to the user
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchUserRole(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "Unknown"
                    redirectToDashboard(role)
                } else {
                    Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onBackPressed() {
        // Do nothing, or show a message
        Toast.makeText(this, "Back button is disabled.", Toast.LENGTH_SHORT).show()
    }
    private fun redirectToDashboard(role: String) {
        when (role) {
            "User" -> {
                startActivity(Intent(this, UserDashBoard::class.java))
                finish()
            }
            "Expert" -> {
                startActivity(Intent(this, ExpertDashBoard::class.java))
                finish()
            }
            else -> {
                Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // This will clear the offline cache on activity destroy (user signs out)
    override fun onDestroy() {
        super.onDestroy()
        firestore.clearPersistence()
    }
}