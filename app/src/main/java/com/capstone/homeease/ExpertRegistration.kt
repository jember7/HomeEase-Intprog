package com.capstone.homeease

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.homeease.databinding.ActivityExpertRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*

class ExpertRegistration : AppCompatActivity() {

    private lateinit var binding: ActivityExpertRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedProfession: String
    private var imageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpertRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        val professions = listOf(
            "Car Washing", "Home Security", "Laundry",
            "Plumbing", "Electrician", "Home Service"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, professions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.professionSpinner.adapter = adapter

        binding.professionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedProfession = parent.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case where nothing is selected
            }
        }
        binding.uploadImageButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        binding.submitButton.setOnClickListener {
            val fullName = binding.fullNameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val dateOfBirth = binding.dateOfBirthEditText.text.toString().trim()
            val address = binding.addressEditText.text.toString().trim()
            val confirmPassword = binding.passwordConfirmEditText.text.toString().trim()
            val number = binding.numberEditText.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || dateOfBirth.isEmpty() ||address.isEmpty()||number.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
            }else if(password != confirmPassword){
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }else {
                registerExpert(fullName, email, password, dateOfBirth, selectedProfession, address,number)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            findViewById<ImageView>(R.id.profileImageView).setImageURI(imageUri)

        }
    }
    private val PROFILE_IMAGES_PATH = "profile_images/"

    private fun registerExpert(fullName: String, email: String, password: String, dateOfBirth: String, profession: String, address: String,number: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        if (imageUri != null) {
                            uploadImageToFirebaseStorage(it.uid, fullName, email,dateOfBirth,profession,address, number)
                        } else {
                            saveUserToFirestore(it.uid, fullName, email,dateOfBirth,profession ,"Expert", null,address,number)
                        }
                    }
                } else {
                    Log.e("ExpertRegistration", "Registration failed", task.exception)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

    }
    private fun uploadImageToFirebaseStorage(userId: String, fullName: String, email: String, dateOfBirth: String, profession: String,address: String,number: String) {
        imageUri?.let { uri ->
            val ref = storage.reference.child("$PROFILE_IMAGES_PATH$userId.jpg")
            val uploadTask = ref.putFile(uri)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveUserToFirestore(userId, fullName, email, dateOfBirth,profession,"Expert", downloadUrl.toString(),address,number)
                }
            }.addOnFailureListener { exception ->
                Log.e("ExpertRegistration", "Failed to upload image to storage", exception)
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                saveUserToFirestore(userId, fullName, email, dateOfBirth,profession,"Expert", null,address,number)
            }
        }
    }
    private fun saveUserToFirestore(userId: String, fullName: String, email: String, dateOfBirth: String, profession: String,role: String,imageUrl: String?,address: String,number: String) {
        val user = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "dateOfBirth" to dateOfBirth,
            "profession" to profession,
            "role" to "Expert",
            "imageUrl" to imageUrl,
            "address" to address,
            "number" to number
        )
        firestore.collection("users").document(userId).set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ExpertRegistration", "User data saved to Firestore successfully")
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    updateUI(auth.currentUser)
                } else {
                    Log.w("ExpertRegistration", "Failed to save user data to Firestore", task.exception)
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
    // Date picker dialog setup
    fun showDatePickerDialog(view: View) {
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                binding.dateOfBirthEditText.setText("${monthOfYear + 1}/$dayOfMonth/$year")
            }, year, month, day
        )
        datePickerDialog.show()
    }
}
