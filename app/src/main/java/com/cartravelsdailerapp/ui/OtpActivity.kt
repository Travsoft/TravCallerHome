package com.cartravelsdailerapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityForgotPasswordBinding
import com.cartravelsdailerapp.databinding.ActivityOtpBinding
import com.cartravelsdailerapp.models.SendOTPResponse
import com.cartravelsdailerapp.models.VerifyOTPResponse
import com.cartravelsdailerapp.viewmodels.LoginAndSignUpViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar

class OtpActivity : AppCompatActivity() {
    lateinit var binding: ActivityOtpBinding
    lateinit var vm: LoginAndSignUpViewModel
    lateinit var mProgressDialog: ProgressDialog
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mProgressDialog = ProgressDialog(this@OtpActivity)
        sharedPreferences = getSharedPreferences(PrefUtils.CallTravelsSharedPref, MODE_PRIVATE)

        val myViewModelFactory =
            MyViewModelFactory(this@OtpActivity.application)

        vm = ViewModelProvider(
            this@OtpActivity,
            myViewModelFactory
        )[LoginAndSignUpViewModel::class.java]

        binding.btVerify.setOnClickListener {
            val otp = binding.pinview.text.toString()
            val email = sharedPreferences.getString(PrefUtils.UserEmail, "")
            val password = binding.etPassword.text.toString()
            val confirmpassword = binding.etConfirmpassword.text.toString()
            if (otp.isEmpty()) {
                Snackbar.make(
                    this,
                    binding.root,
                    resources.getString(R.string.enter_otp),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (password.isEmpty()) {
                Snackbar.make(
                    this,
                    binding.root,
                    resources.getString(R.string.enter_password),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else if (confirmpassword.isEmpty()) {
                Snackbar.make(
                    this,
                    binding.root,
                    resources.getString(R.string.enter_confirm_password),
                    Snackbar.LENGTH_SHORT
                ).show()

            } else if (password != confirmpassword) {
                Snackbar.make(
                    this,
                    binding.root,
                    resources.getString(R.string.should_confirm_password_same),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {

                mProgressDialog.setTitle("Loading")
                mProgressDialog.show()
                email?.let { it1 -> vm.verifyOTP(it1, password, otp) }
            }
        }
        vm.verifyOTPResp.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    mProgressDialog.show()

                }

                is BaseResponse.Success -> {
                    val data = it.data as VerifyOTPResponse
                    mProgressDialog.dismiss()
                    if (data.statusCode == 200) {
                        val intent = Intent(
                            this,
                            MainActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)

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