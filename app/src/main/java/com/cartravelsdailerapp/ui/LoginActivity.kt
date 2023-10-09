package com.cartravelsdailerapp.ui

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityLoginBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var vm: MainActivityViewModel
    private lateinit var binding: ActivityLoginBinding
    var email: String? = null
    var mobileNo: String? = null
    private var REQUESTED_CODE_READ_PHONE_STATE = 1003

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
        if (this.let {
                ActivityCompat.checkSelfPermission(
                    it.applicationContext,
                    Manifest.permission.READ_PHONE_STATE
                )
            } == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            getPhoneNumbers().forEach {
                val phoneNumber = it
                Log.d("DREG_PHONE", "phone number: $phoneNumber")
                binding.etMobile.setText(phoneNumber)
            }

            binding.etEmail.setText(GetEmailId())
            vm.getCallLogsHistory()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.GET_ACCOUNTS
                ),
                REQUESTED_CODE_READ_PHONE_STATE
            )

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUESTED_CODE_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == 0 }) {
                    //  vm.getCallLogsHistory()
                    getPhoneNumbers().forEach {
                        val phoneNumber = it
                        Log.d("DREG_PHONE", "phone number: $phoneNumber")
                        binding.etMobile.setText(phoneNumber)
                    }

                    binding.etEmail.setText(GetEmailId())
                }
            }
        }
    }

    fun Context.getPhoneNumbers(): ArrayList<String> { // Required Permissions: READ_PHONE_STATE, READ_PHONE_NUMBERS
        val phoneNumbers = arrayListOf<String>()
        if (isFromAPI(20)) {
            val subscriptionManager = getSystemService(SubscriptionManager::class.java)
            val subsInfoList = subscriptionManager.activeSubscriptionInfoList
            for (subscriptionInfo in subsInfoList) {
                val phoneNumber =
                    if (isFromAPI(33))
                        subscriptionManager.getPhoneNumber(subscriptionInfo.subscriptionId)
                    else subscriptionInfo.number
                if (phoneNumber.isNullOrBlank().not()) phoneNumbers.add(phoneNumber)
            }
        }
        return phoneNumbers
    }

    fun GetEmailId(): String {
        var email = ""
        val manager = getSystemService(ACCOUNT_SERVICE) as AccountManager
        manager.accounts.forEach {
            if (it.type.equals("com.google", true)) {
                email = it.name
            }
        }
        return email
    }

    private fun isFromAPI(apiLevel: Int) = Build.VERSION.SDK_INT >= apiLevel

}