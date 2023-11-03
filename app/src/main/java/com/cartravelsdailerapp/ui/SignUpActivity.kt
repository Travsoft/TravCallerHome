package com.cartravelsdailerapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.PrefUtils.KeyEmail
import com.cartravelsdailerapp.PrefUtils.KeyPhoneNumber
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Sign Up"
        val d = intent.extras
        val email = d?.getString(KeyEmail)
        val phoneNumber = d?.getString(KeyPhoneNumber)
        binding.etEmail.setText(email)
        binding.etSim1.setText(phoneNumber)
        binding.btSignup.setOnClickListener {
            val intent = Intent(
                this,
                Login2Activity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)
        }
    }
}