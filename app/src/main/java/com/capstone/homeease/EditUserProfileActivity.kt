package com.capstone.homeease

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditUserProfileActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var saveButton: Button

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_profile)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        addressEditText = findViewById(R.id.addressEditText)
        numberEditText = findViewById(R.id.numberEditText)
        profileImageView = findViewById(R.id.profileImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        saveButton = findViewById(R.id.saveButton)

        uploadImageButton.setOnClickListener {
            openFileChooser()
        }

        saveButton.setOnClickListener {
            saveUserProfile()

        }

        loadUserProfile()
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        fullNameEditText.setText(document.getString("fullName"))
                        emailEditText.setText(document.getString("email"))
                        addressEditText.setText(document.getString("address"))
                        numberEditText.setText(document.getString("number"))

                        val profileImageUrl = document.getString("imageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Load the profile image using Glide
                            Glide.with(this@EditUserProfileActivity)
                                .load(profileImageUrl)
                                .into(profileImageView)
                        } else {
                            // Handle case when profile image URL is null or empty
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("EditUserProfileActivity", "Error getting user profile", exception)
                }
        }
    }

    private fun saveUserProfile() {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val number = numberEditText.text.toString().trim()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val userUpdates: MutableMap<String, Any> = hashMapOf(
                "fullName" to fullName,
                "email" to email,
                "address" to address,
                "number" to number
            )


            firestore.collection("users").document(userId).update(userUpdates)
                .addOnSuccessListener {
                    if (imageUri != null) {
                        uploadProfileImage(userId)
                    } else {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                        // Redirect back to the profile page
                        val intent = Intent(this, ProfilePageActivity::class.java)
                        startActivity(intent)
                        finish() // Optional: Close the current activity
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("EditUserProfileActivity", "Error updating user profile", exception)
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadProfileImage(userId: String) {
        val storageRef = storage.reference.child("profileImages/$userId.jpg")
        val uploadTask = storageRef.putFile(imageUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                firestore.collection("users").document(userId).update("imageUrl", downloadUri.toString())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.w("EditUserProfileActivity", "Error updating profile image URL", exception)
                        Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Failed to upload profile image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
