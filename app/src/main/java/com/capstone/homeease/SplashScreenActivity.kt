package com.capstone.homeease

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import android.content.SharedPreferences


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_DISPLAY_LENGTH = 3000 // Splash screen delay time in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val migrationDone = sharedPreferences.getBoolean("migration_done", false)

        if (!migrationDone) {
            ExpertConversationActivity.migrateTimestamps()
            sharedPreferences.edit().putBoolean("migration_done", true).apply()
        }
        FirebaseApp.initializeApp(this)
        Handler().postDelayed({
            val mainIntent = Intent(this@SplashScreenActivity, HomeActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }

}

