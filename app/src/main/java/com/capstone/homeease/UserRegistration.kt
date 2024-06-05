package com.capstone.homeease

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class UserRegistration : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)

        // Initialize Firebase Auth, Firestore, and Storage
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        storage = Firebase.storage

        val fullNameEditText = findViewById<EditText>(R.id.fullNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val passwordConfirmEditText = findViewById<EditText>(R.id.passwordConfirmEditText)
        val addressEditText = findViewById<EditText>(R.id.addressEditText)
        val numberEditText = findViewById<EditText>(R.id.numberEditText)
        val continueButton = findViewById<Button>(R.id.registerButton)
        val uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        val profileImageView = findViewById<ImageView>(R.id.profileImageView)

        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        continueButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = passwordConfirmEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val number = numberEditText.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(fullName, email, password, address, number)
            }
        }
        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                imageUri = data.data
                findViewById<ImageView>(R.id.profileImageView).setImageURI(imageUri)
                Log.d("UserRegistration", "Image URI: $imageUri")
            } else {
                Log.e("UserRegistration", "Image selection failed: data or data.data is null")
                Toast.makeText(this, "Error getting selected file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("UserRegistration", "Image selection cancelled or failed")
            Toast.makeText(this, "Error getting selected file", Toast.LENGTH_SHORT).show()
        }
    }

    private val PROFILE_IMAGES_PATH = "profile_images/"

    private fun registerUser(fullName: String, email: String, password: String, address: String, number: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("UserRegistration", "createUserWithEmail:success")
                    val user = auth.currentUser
                    user?.let {
                        if (imageUri != null) {
                            uploadImageToFirebaseStorage(it.uid, fullName, email, address, number)
                        } else {
                            saveUserToFirestore(it.uid, fullName, email, "User", null, address, number)
                        }
                    }
                } else {
                    Log.w("UserRegistration", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Email Already Used.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun uploadImageToFirebaseStorage(userId: String, fullName: String, email: String, address: String, number: String) {
        imageUri?.let { uri ->
            val ref = storage.reference.child("$PROFILE_IMAGES_PATH$userId.jpg")
            val uploadTask = ref.putFile(uri)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveUserToFirestore(userId, fullName, email, "User", downloadUrl.toString(), address, number)
                }
            }.addOnFailureListener { exception ->
                Log.e("UserRegistration", "Failed to upload image to storage", exception)
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                saveUserToFirestore(userId, fullName, email, "User", null, address, number)
            }
        } ?: run {
            Log.e("UserRegistration", "Image URI is null")
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserToFirestore(userId: String, fullName: String, email: String, role: String, imageUrl: String?, address: String, number: String) {
        val user = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "role" to role,
            "imageUrl" to imageUrl,
            "address" to address,
            "number" to number
        )
        firestore.collection("users").document(userId).set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("UserRegistration", "User data saved to Firestore successfully")
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    updateUI(auth.currentUser)
                } else {
                    Log.w("UserRegistration", "Failed to save user data to Firestore", task.exception)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, LoginPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
