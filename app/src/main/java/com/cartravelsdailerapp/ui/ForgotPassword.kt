package com.cartravelsdailerapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cartravelsdailerapp.R

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        supportActionBar?.title = "Forgot Password"
    }
}