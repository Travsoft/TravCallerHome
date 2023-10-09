package com.cartravelsdailerapp.ui

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cartravelsdailerapp.IndeterminateProgressDialog
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityLoginBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.utils.RunTimePermission
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var vm: MainActivityViewModel
    private lateinit var binding: ActivityLoginBinding
    var email: String? = null
    var mobileNo: String? = null
    private lateinit var job: Job
    val workManager = WorkManager.getInstance()
    var runtimePermission: RunTimePermission = RunTimePermission(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val db = DatabaseBuilder.getInstance(this).CallHistoryDao()

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
        val dialog = IndeterminateProgressDialog(this@LoginActivity)
        dialog.setMessage("Please wait...")
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
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
                    binding.etEmail.text?.clear()
                    binding.etMobile.text?.clear()
                    getPhoneNumbers().forEach {
                        val phoneNumber = it
                        Log.d("DREG_PHONE", "phone number: $phoneNumber")
                        binding.etMobile.setText(phoneNumber)
                    }
                    binding.etEmail.setText(GetEmailId())
                    launch {
                        vm.getCallLogsHistory()
                    }
                }

                override fun onDenied() {
                    //show message if not allow storage permission
                }
            })
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
        get() = job + Dispatchers.Main

    public class DownLoadFileWorkManager(context: Context, workerParams: WorkerParameters) :
        Worker(context, workerParams) {
        override fun doWork(): Result {
            //TODO perform your async operational task here
            /**
             * We have performed download task here on above example
             */
            return Result.success()
        }
    }

}