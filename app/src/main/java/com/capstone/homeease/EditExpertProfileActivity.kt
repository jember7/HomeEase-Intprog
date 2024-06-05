package com.capstone.homeease

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

class EditExpertProfileActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var saveButton: Button
    private lateinit var dateOfBirthEditText: EditText
    private lateinit var professionSpinner: Spinner
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var selectedProfession: String
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private val professions = listOf(
        "Car Washing", "Home Security", "Laundry",
        "Plumbing", "Electrician", "Home Service"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_expert_profile)

        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        addressEditText = findViewById(R.id.addressEditText)
        numberEditText = findViewById(R.id.numberEditText)
        profileImageView = findViewById(R.id.profileImageView)
        professionSpinner = findViewById(R.id.professionSpinner)
        uploadImageButton = findViewById(R.id.uploadImageButton)
        dateOfBirthEditText = findViewById(R.id.dateOfBirthEditText)
        saveButton = findViewById(R.id.saveButton)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, professions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        professionSpinner.adapter = adapter
        professionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedProfession = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case where nothing is selected
            }
        }
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
                        val dob = document.getString("dateOfBirth")
                        dateOfBirthEditText.setText(dob)

                        // Set profession
                        val profession = document.getString("profession")
                        val position = professions.indexOf(profession)
                        professionSpinner.setSelection(position)
                        val profileImageUrl = document.getString("imageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Load the profile image using Glide
                            Glide.with(this@EditExpertProfileActivity)
                                .load(profileImageUrl)
                                .into(profileImageView)
                        } else {
                            // Handle case when profile image URL is null or empty
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("EditExpertProfileActivity", "Error getting user profile", exception)
                }
        }
    }

    private fun saveUserProfile() {
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val number = numberEditText.text.toString().trim()
        val dateOfBirth = dateOfBirthEditText.text.toString().trim() // Get date of birth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val userUpdates: MutableMap<String, Any> = hashMapOf(
                "fullName" to fullName,
                "email" to email,
                "address" to address,
                "number" to number,
                "profession" to selectedProfession, // Include selected profession
                "dateOfBirth" to dateOfBirth // Include date of birth
            )


            firestore.collection("users").document(userId).update(userUpdates)
                .addOnSuccessListener {
                    if (imageUri != null) {
                        uploadProfileImage(userId)
                    } else {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                        // Redirect back to the profile page
                        val intent = Intent(this, ExpertProfilePage::class.java)
                        startActivity(intent)
                        finish() // Optional: Close the current activity
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("EditExpertProfileActivity", "Error updating user profile", exception)
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
                        Log.w("EditExpertProfileActivity", "Error updating profile image URL", exception)
                        Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Failed to upload profile image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun showDatePickerDialog(view: View) {
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                dateOfBirthEditText.setText("${monthOfYear + 1}/$dayOfMonth/$year")
            }, year, month, day
        )
        datePickerDialog.show()
    }
}

