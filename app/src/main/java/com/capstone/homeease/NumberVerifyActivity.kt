package com.capstone.homeease

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class NumberVerifyActivity : AppCompatActivity() {
    private lateinit var phoneNumberEditText: EditText
    private lateinit var countryCodeSpinner: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_verify)

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        countryCodeSpinner = findViewById(R.id.countryCodeSpinner)

        val countryCodeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.country_codes,
            android.R.layout.simple_spinner_item
        )
        countryCodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countryCodeSpinner.adapter = countryCodeAdapter

        val continueButton: Button = findViewById(R.id.continue_button)
        continueButton.setOnClickListener {
            verifyPhoneNumber()
        }
    }

    private fun verifyPhoneNumber() {
        val selectedCountryCode: String = countryCodeSpinner.selectedItem.toString()
        val phoneNumber: String = phoneNumberEditText.text.toString()

        // Your verification logic here

        // For example, you can navigate to the next activity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}