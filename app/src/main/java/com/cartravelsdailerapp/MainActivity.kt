package com.cartravelsdailerapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.DateFormat
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.cartravelsdailerapp.databinding.ActivityMainBinding
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.fragments.CallHistoryFragment
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var REQUESTED_CODE_READ_PHONE_STATE = 1003
    @RequiresApi(Build.VERSION_CODES.N)
    val dateTimeFormat: DateFormat =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
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
            with(binding) {
                setContentView(root)
                initNavHost()
                setUpBottomNavigation()
                listData= getAllCallHistory() as ArrayList<CallHistory>
            }
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CALL_PHONE
                ),
                REQUESTED_CODE_READ_PHONE_STATE
            )

        }
    }

    private fun ActivityMainBinding.setUpBottomNavigation() {
        val bottomNavigationItems = mutableListOf(
            CurvedBottomNavigation.Model(
                CALL_HISTORY,
                "",
                R.drawable.histroy
            ),
            CurvedBottomNavigation.Model(
                HOME_ITEM, "",
                R.drawable.home
            ),
            CurvedBottomNavigation.Model(
                Chat_ITEM,
                "",
                R.drawable.histroy
            )

        )
        bottomNavigation.apply {
            bottomNavigationItems.forEach { add(it) }
            setOnClickMenuListener {
                navController.navigate(it.id)
            }
            // optional
            setupNavController(navController)
        }
    }

    private fun initNavHost() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }


    // if you need your backstack of your items always back to home please override this method
    override fun onBackPressed() {
        if (navController.currentDestination!!.id == HOME_ITEM)
            super.onBackPressed()
        else {
            when (navController.currentDestination!!.id) {
                /*  OFFERS_ITEM -> {
                      navController.popBackStack(R.id.homeFragment, false)
                  }
                  SECTION_ITEM -> {
                      navController.popBackStack(R.id.homeFragment, false)
                  }
                  CART_ITEM -> {
                      navController.popBackStack(R.id.homeFragment, false)
                  }
                  MORE_ITEM -> {
                      navController.popBackStack(R.id.homeFragment, false)
                  }
                  else -> {
                      navController.navigateUp()
                  }*/
            }
        }
    }


    companion object {
        const val HOME_ITEM = R.id.homeFragment
        const val Chat_ITEM = R.id.chatFragment
        const val CALL_HISTORY = R.id.CallHistoryFragment
        var listData = ArrayList<CallHistory>()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUESTED_CODE_READ_PHONE_STATE -> {
                if (grantResults.size > 0 && grantResults.all { it == 0 }) {
                    runBlocking {
                        listData.addAll(withContext(Dispatchers.Default) {
                            getAllCallHistory()
                        })
                    }

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("Range")
     fun getAllCallHistory(): MutableList<CallHistory> {
        this.contentResolver?.query(
            CallLog.Calls.CONTENT_URI, null, null,
            null, null
        )?.let {
            val callHistoryList = mutableListOf<CallHistory>()
            var dir: String? = null
            while (it.moveToNext()) {
                when (it.getString(it.getColumnIndex(CallLog.Calls.TYPE)).toInt()) {
                    CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                    CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                    CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
                }
                callHistoryList.add(
                    CallHistory(
                        number = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER)),
                        name = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_NAME))
                            ?: null,
                        type = it.getString(it.getColumnIndex(CallLog.Calls.TYPE)).toInt(),
                        date = dateTimeFormat.format(it.getLong(it.getColumnIndex(CallLog.Calls.DATE)))
                            .toString(),
                        duration = it.getLong(it.getColumnIndex(CallLog.Calls.DURATION)).toString(),
                        subscriberId = it.getString(it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                            ?: "",
                        calType = dir.toString(),
                        photouri = it.getString(it.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))
                            ?: "",
                        SimName = getSimCardInfosBySubscriptionId(
                            it.getString(it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                ?: "0",
                        )?.displayName?.toString() ?: "",
                    )
                )

            }
            it.close()
            return callHistoryList.reversed().distinctBy { i -> i.name }.toMutableList()
        }

        return mutableListOf()

    }

    /**
     * This Function Will return list of SubscriptionInfo
     */
    private fun getSimCardInfosBySubscriptionId(subscriptionId: String): SubscriptionInfo? {
        val subscriptionManager: SubscriptionManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP_MR1")
            }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUESTED_CODE_READ_PHONE_STATE
            )

            return null
        } else {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                subscriptionManager.activeSubscriptionInfoList.find {
                    try {
                        it.subscriptionId == subscriptionId.toInt()
                    } catch (e: Exception) {
                        return null
                    }
                }
            } else {
                return null
            }
        }

    }
}
