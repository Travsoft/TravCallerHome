package com.cartravelsdailerapp.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityLoginBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
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
        sharedPreferences = getSharedPreferences(PrefUtils.CallTravelsSharedPref, MODE_PRIVATE)
        setContentView(binding.root)
        if (sharedPreferences.getBoolean(PrefUtils.IsLogin, false)) {
            vm.getCallLogsHistory()
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),

                )
        }

        binding.btLogin.setOnClickListener {
            email = binding.etEmail.text.toString()
            mobileNo = binding.etMobile.text.toString()
            if (TextUtils.isEmpty(email)) {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.EnterEmailid),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (TextUtils.isEmpty(mobileNo)) {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.mobile_number),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (!binding.checkTermandcondition.isChecked) {
                Snackbar.make(
                    binding.root,
                    resources.getString(R.string.CheckTermsAndConditions),
                    Snackbar.LENGTH_SHORT
                ).show()

            } else {
                vm.getAllCallLogsHistory()
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),

                    )
                val edit = sharedPreferences.edit()
                edit.putBoolean(PrefUtils.IsLogin, true)
                edit.apply()
            }
        }
        binding.txtSignUp.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SignUpActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),

                )
        }
        binding.txtForgotpassword.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ForgotPassword::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),

                )
        }
        binding.txtSkip.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            )
        }


    }
}