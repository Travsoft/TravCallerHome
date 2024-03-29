package com.cartravelsdailerapp.ui

import android.Manifest
import android.accounts.AccountManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.alexstyl.contactstore.ContactStore
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityLoginBinding
import com.cartravelsdailerapp.utils.RunTimePermission
import com.cartravelsdailerapp.viewmodels.LoginAndSignUpViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var vm: LoginAndSignUpViewModel
    private lateinit var binding: ActivityLoginBinding
    var email: String? = null
    var mobileNo: String? = null
    private lateinit var job: Job
    lateinit var listOfContactStore: ContactStore
    var runtimePermission: RunTimePermission = RunTimePermission(this)
    lateinit var mProgressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        mProgressDialog = ProgressDialog(this@LoginActivity)
        mProgressDialog.setTitle("Loading")
        val myViewModelFactory =
            MyViewModelFactory(this@LoginActivity.application)
        vm = ViewModelProvider(
            this@LoginActivity,
            myViewModelFactory
        )[LoginAndSignUpViewModel::class.java]
        job = Job()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences(PrefUtils.CallTravelsSharedPref, MODE_PRIVATE)
        setContentView(binding.root)
        listOfContactStore = ContactStore.newInstance(this)
        binding.etEmail.setText(sharedPreferences.getString(PrefUtils.UserEmail, ""))
    }

    override fun onResume() {
        super.onResume()
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
                mProgressDialog = ProgressDialog(this@LoginActivity)
                mProgressDialog.setTitle("Loading")
                mProgressDialog.show()
                vm.userExist(email!!, mobileNo!!)

                vm.userExistResp.observe(this) {
                    when (it) {
                        is BaseResponse.Loading -> {
                            //  showLoading()
                            mProgressDialog.show()

                        }

                        is BaseResponse.Success -> {
                            // stopLoading()
                            mProgressDialog.dismiss()
                            if (it.data?.alreadyExists == true) {
                                Snackbar.make(
                                    binding.root,
                                    it.data.message, Snackbar.LENGTH_SHORT
                                ).show()

                                val intent = Intent(
                                    this,
                                    Login2Activity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.putExtra(PrefUtils.UserEmail, email)
                                intent.putExtra(PrefUtils.KeyPhoneNumber, mobileNo)
                                startActivity(intent)
                                val edit = sharedPreferences.edit()
                                edit.putString(PrefUtils.UserEmail, email)
                                edit.apply()
                                finish()


                            } else {
                                val intent = Intent(
                                    this,
                                    SignUpActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.putExtra(PrefUtils.UserEmail, email)
                                intent.putExtra(PrefUtils.KeyPhoneNumber, mobileNo)
                                startActivity(intent)
                                val edit = sharedPreferences.edit()
                                edit.putString(PrefUtils.UserEmail, email)
                                edit.putString(PrefUtils.KeyPhoneNumber, mobileNo)
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
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            runtimePermission.requestPermission(
                listOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.GET_ACCOUNTS,
                    android.Manifest.permission.CAMERA
                ),
                object : RunTimePermission.PermissionCallback {
                    override fun onGranted() {
                        if (sharedPreferences.getBoolean(PrefUtils.IsLogin, false)) {
                        } else {

                            mProgressDialog.setMessage("Preparing Call History...")
                            mProgressDialog.setCancelable(false)
                            mProgressDialog.show()
                            binding.etEmail.text?.clear()
                            binding.etMobile.text?.clear()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                getPhoneNumbers().forEach {
                                    val phoneNumber = it
                                    Log.d("DREG_PHONE", "phone number: $phoneNumber")
                                    binding.etMobile.setText(phoneNumber)
                                }

                            }
                            binding.etEmail.setText(GetEmailId())

                        }

                    }

                    override fun onDenied() {
                        //show message if not allow storage permission
                        Toast.makeText(this@LoginActivity, "d", Toast.LENGTH_LONG).show()
                    }
                })

        } else {

            runtimePermission.requestPermission(
                listOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.GET_ACCOUNTS,
                    android.Manifest.permission.CAMERA
                ),
                object : RunTimePermission.PermissionCallback {
                    override fun onGranted() {
                        if (sharedPreferences.getBoolean(PrefUtils.IsLogin, false)) {
                        } else {

                            binding.etEmail.text?.clear()
                            binding.etMobile.text?.clear()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                getPhoneNumbers().forEach {
                                    val phoneNumber = it
                                    Log.d("DREG_PHONE", "phone number: $phoneNumber")
                                    binding.etMobile.setText(phoneNumber)
                                }

                            }
                            binding.etEmail.setText(GetEmailId())

                        }

                    }

                    override fun onDenied() {
                        //show message if not allow storage permission
                        Toast.makeText(this@LoginActivity, "d", Toast.LENGTH_LONG).show()
                    }
                })

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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun isFromAPI(apiLevel: Int) = Build.VERSION.SDK_INT >= apiLevel
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO
}