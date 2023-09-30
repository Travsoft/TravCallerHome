package com.cartravelsdailerapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.DataBinderMapperImpl
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityLoginBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    lateinit var vm: MainActivityViewModel

    private lateinit var binding: ActivityLoginBinding
    var email: String? = null
    var mobileNo: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val myViewModelFactory =
            MyViewModelFactory(this@LoginActivity.application)
        vm = ViewModelProvider(
            this@LoginActivity,
            myViewModelFactory
        )[MainActivityViewModel::class.java]
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm.callLogs.observe(this) {
            DatabaseBuilder.getInstance(this).CallHistoryDao().insertAll(it)
        }
        binding.btLogin.setOnClickListener {
            email = binding.etEmail.text.toString()
            mobileNo = binding.etMobile.text.toString()
            if (TextUtils.isEmpty(email)) {
                Snackbar.make(binding.root, "Enter Email id", Snackbar.LENGTH_SHORT).show()
            } else if (TextUtils.isEmpty(mobileNo)) {
                Snackbar.make(binding.root, "Enter Mobile Number", Snackbar.LENGTH_SHORT).show()
            } else if (!binding.checkTermandcondition.isChecked) {
                Snackbar.make(binding.root, "Check Terms and Conditions", Snackbar.LENGTH_SHORT)
                    .show()

            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        binding.txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        binding.txtForgotpassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }
        binding.txtSkip.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


    }
}