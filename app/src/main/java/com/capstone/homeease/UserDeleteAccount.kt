package com.capstone.homeease

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.homeease.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class UserDeleteAccountActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityDeleteAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.deleteAccountButton.setOnClickListener {
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showDeleteConfirmationDialog(password)
        }
    }

    private fun verifyPasswordAndDeleteAccount(password: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
        currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                // Password verification successful, proceed to delete account
                checkBookingsAndDeleteAccount(currentUser)
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkBookingsAndDeleteAccount(user: FirebaseUser) {
        // Check for accepted bookings
        firestore.collection("bookings")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("status", "Accepted")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No accepted bookings, proceed to check for pending bookings
                    checkPendingBookingsAndDeleteAccount(user)
                } else {
                    // There are accepted bookings, cannot delete account
                    Toast.makeText(this, "Cannot delete account with accepted bookings", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.w("DeleteAccount", "Error checking bookings", e)
                Toast.makeText(this, "Error checking bookings", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkPendingBookingsAndDeleteAccount(user: FirebaseUser) {
        firestore.collection("bookings")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { documents ->
                // Cancel all pending bookings
                val batch = firestore.batch()
                for (document in documents) {
                    batch.update(document.reference, "status", "Cancelled")
                }
                batch.commit().addOnCompleteListener {
                    deleteAccount(user)
                }
            }
            .addOnFailureListener { e ->
                Log.w("DeleteAccount", "Error checking bookings", e)
                Toast.makeText(this, "Error checking bookings", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAccount(user: FirebaseUser) {
        // Delete user-related data from Firestore
        firestore.collection("users").document(user.uid)
            .delete()
            .addOnSuccessListener {
                // User data deleted successfully
                Toast.makeText(this, "User data deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Failed to delete user data
                Log.e("DeleteAccount", "Error deleting user data: $e")
                Toast.makeText(this, "Error deleting user data", Toast.LENGTH_SHORT).show()
            }

        // Delete the user account
        user.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    // Redirect to the login page or finish activity
                    auth.signOut()
                    // Redirect to the home screen
                    navigateToHomeScreen()
                } else {
                    Toast.makeText(this, "Error deleting account", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showDeleteConfirmationDialog(password: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")

        builder.setPositiveButton("Yes") { _, _ ->
            // User clicked Yes, proceed to delete account
            verifyPasswordAndDeleteAccount(password)
        }

        builder.setNegativeButton("No") { _, _ ->
            // User clicked No, dismiss the dialog
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun navigateToHomeScreen() {
        // Replace HomeActivity::class.java with the actual home screen activity class
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Finish the current activity
    }
}
