package com.cartravelsdailerapp.ui

import android.Manifest
import android.accounts.AccountManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityLoginBinding
import com.cartravelsdailerapp.utils.RunTimePermission
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var vm: MainActivityViewModel
    private lateinit var binding: ActivityLoginBinding
    var email: String? = null
    var mobileNo: String? = null
    private lateinit var job: Job
    var runtimePermission: RunTimePermission = RunTimePermission(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val myViewModelFactory =
            MyViewModelFactory(this@LoginActivity.application)
        vm = ViewModelProvider(
            this@LoginActivity,
            myViewModelFactory
        )[MainActivityViewModel::class.java]
        job = Job()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences(PrefUtils.CallTravelsSharedPref, MODE_PRIVATE)
        setContentView(binding.root)

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
                val intent = Intent(
                    this,
                    SignUpActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(PrefUtils.KeyEmail, email)
                intent.putExtra(PrefUtils.KeyPhoneNumber, mobileNo)
                startActivity(intent)
                val edit = sharedPreferences.edit()
                edit.putBoolean(PrefUtils.IsLogin, true)
                edit.apply()

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
                    Manifest.permission.GET_ACCOUNTS
                ),
                object : RunTimePermission.PermissionCallback {
                    override fun onGranted() {
                        if (sharedPreferences.getBoolean(PrefUtils.IsLogin, false)) {
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    MainActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),

                                )
                        } else {
                            val mProgressDialog = ProgressDialog(this@LoginActivity)
                            mProgressDialog.setTitle("Loading")
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

                            launch(Dispatchers.IO) {
                                freezePleaseIAmDoingHeavyWork()
                            }
                            vm.callLogs.observe(this@LoginActivity) {
                                Thread.sleep(3000)
                                mProgressDialog.dismiss()
                                Log.d("Login activity", "call history completed")
                            }

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
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.GET_ACCOUNTS
                ),
                object : RunTimePermission.PermissionCallback {
                    override fun onGranted() {
                        if (sharedPreferences.getBoolean(PrefUtils.IsLogin, false)) {
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    MainActivity::class.java
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),

                                )
                        } else {
                            val mProgressDialog = ProgressDialog(this@LoginActivity)
                            mProgressDialog.setTitle("Loading")
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

                            launch(Dispatchers.IO) {
                                freezePleaseIAmDoingHeavyWork()
                            }
                            vm.callLogs.observe(this@LoginActivity) {
                                Thread.sleep(3000)
                                mProgressDialog.dismiss()
                                Log.d("Login activity", "call history completed")
                            }

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

    suspend fun freezePleaseIAmDoingHeavyWork() { // function B in image
        withContext(Dispatchers.Default) {
            async {
                //pretend this is a big network call
                vm.getCallLogsHistory()
                vm.getAllContacts()
            }
        }
    }

}