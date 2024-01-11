package com.cartravelsdailerapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityForgotPasswordBinding
import com.cartravelsdailerapp.models.SendOTPResponse
import com.cartravelsdailerapp.viewmodels.LoginAndSignUpViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar

class ForgotPassword : AppCompatActivity() {
    lateinit var binding: ActivityForgotPasswordBinding
    lateinit var vm: LoginAndSignUpViewModel
    lateinit var mProgressDialog: ProgressDialog
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences(PrefUtils.CallTravelsSharedPref, MODE_PRIVATE)
        binding.etEmail.setText(sharedPreferences.getString(PrefUtils.UserEmail, ""))
        val myViewModelFactory =
            MyViewModelFactory(this@ForgotPassword.application)

        vm = ViewModelProvider(
            this@ForgotPassword,
            myViewModelFactory
        )[LoginAndSignUpViewModel::class.java]


        binding.btSendOtp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isEmpty()) {
                Snackbar.make(
                    binding.root, resources.getString(R.string.enter_email),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                mProgressDialog = ProgressDialog(this@ForgotPassword)
                mProgressDialog.setTitle("Loading")
                mProgressDialog.show()
                vm.sendOtp(email)
            }
        }

        vm.sendOTPResp.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {

                }

                is BaseResponse.Success -> {
                    val data = it.data as SendOTPResponse
                    mProgressDialog.dismiss()
                    if (data.statusCode == 200 || data.statusCode == 400) {
                        binding.txtMessage.text = data.message
                        val intent = Intent(
                            this,
                            OtpActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    } else {

                        data.message?.let { it1 ->
                            Snackbar.make(
                                binding.root,
                                it1, Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                }

                is BaseResponse.Error -> {
                    // processError(it.msg)
                    it.msg?.let { it1 ->
                        Snackbar.make(
                            binding.root,
                            it1, Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    Log.d(
                        "100->", it
                            .msg.toString()
                    )
                    mProgressDialog.dismiss()
                }
                else -> {
                    //stopLoading()
                    mProgressDialog.dismiss()
                }
            }

        }

    }
}