package com.cartravelsdailerapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityLogin2Binding
import com.cartravelsdailerapp.models.UserLoginResponse
import com.cartravelsdailerapp.viewmodels.LoginAndSignUpViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar

class Login2Activity : AppCompatActivity() {
    lateinit var binding: ActivityLogin2Binding
    lateinit var vm: LoginAndSignUpViewModel
    lateinit var mProgressDialog: ProgressDialog
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        mProgressDialog = ProgressDialog(this@Login2Activity)
        mProgressDialog.setTitle("Loading")
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val myViewModelFactory =
            MyViewModelFactory(this@Login2Activity.application)

        vm = ViewModelProvider(
            this@Login2Activity,
            myViewModelFactory
        )[LoginAndSignUpViewModel::class.java]
        sharedPreferences = getSharedPreferences(PrefUtils.CallTravelsSharedPref, MODE_PRIVATE)

        val d = intent.extras
        val email = d?.getString(PrefUtils.UserEmail)
        val phoneNumber = d?.getString(PrefUtils.KeyPhoneNumber)
        binding.etPhone.setText(phoneNumber)

        binding.btLogin.setOnClickListener {
            val enteredPhNumber = binding.etPhone.text.toString()
            val pasword = binding.etPassword.text.toString()
            if (enteredPhNumber?.isEmpty() == true) {
                initErrorMessage(R.string.enter_email)
            } else if (pasword?.isEmpty() == true) {
                initErrorMessage(R.string.enter_password)
            } else {
                mProgressDialog.show()
                vm.userLogin(enteredPhNumber, pasword)
            }
        }
        binding.txtForgotpassword.setOnClickListener {
            val intent = Intent(
                this,
                ForgotPassword::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        vm.userLoginResp.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    mProgressDialog.show()

                }

                is BaseResponse.Success -> {
                    val data = it.data as UserLoginResponse
                    val userData = data.data
                    mProgressDialog.dismiss()
                    if (data.statusCode == 200) {
                        val edit = sharedPreferences.edit()
                        edit.putBoolean(PrefUtils.IsLogin, true)
                        edit.putString(PrefUtils.UserProfileUrl, userData.first().profilePicture)
                        edit.putString(PrefUtils.userId, userData.first().id)
                        edit.putString(PrefUtils.userToken, userData.first().token)
                        edit.putString(PrefUtils.UserEmail, userData.first().email)
                        edit.apply()

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

        binding.txtSignUp.setOnClickListener {
            val intent = Intent(
                this,
                SignUpActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }

    }

    private fun initErrorMessage(msg: Int) {
        Snackbar.make(
            binding.root,
            msg,
            Snackbar.LENGTH_SHORT
        ).show()

    }
}