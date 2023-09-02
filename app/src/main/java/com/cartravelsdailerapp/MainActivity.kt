package com.cartravelsdailerapp

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.cartravelsdailerapp.databinding.ActivityMainBinding
import com.cartravelsdailerapp.models.CallHistory
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.Long
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.Array
import kotlin.Int
import kotlin.IntArray
import kotlin.String
import kotlin.apply
import kotlin.arrayOf
import kotlin.let
import kotlin.toString
import kotlin.with


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    var REQUESTED_CODE_READ_PHONE_STATE = 1003

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
            initNavHost()
            setUpBottomNavigation()
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                WRITE_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runBlocking {
                listData.addAll(withContext(Dispatchers.Default) {
                    getAllCallHistory()
                })
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    READ_PHONE_STATE,
                    READ_CONTACTS,
                    READ_CALL_LOG,
                    READ_PHONE_NUMBERS,
                    WRITE_CALL_LOG,
                    READ_PHONE_STATE,
                    CALL_PHONE
                ),
                REQUESTED_CODE_READ_PHONE_STATE
            )

        }
    }

    /**
     * This Function Will return list of SubscriptionInfo
     */
    private fun getSimCardInfosBySubscriptionId(subscriptionId: String): SubscriptionInfo? {
        val subscriptionManager: SubscriptionManager =
            getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        if (ActivityCompat.checkSelfPermission(
                this,
                READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_PHONE_STATE),
                REQUESTED_CODE_READ_PHONE_STATE
            )

            return null
        } else {
            return subscriptionManager.activeSubscriptionInfoList.find {
                it.subscriptionId == subscriptionId.toInt()
            }
        }

    }

    @SuppressLint("Range")
    fun getAllCallHistory(): MutableList<CallHistory> {

        this.contentResolver?.query(
            CallLog.Calls.CONTENT_URI, null, null,
            null, null
        )?.let {
            val callHistoryList = mutableListOf<CallHistory>()
            var dir: String? = null
            var formatter: SimpleDateFormat = SimpleDateFormat(
                "dd-MMM-yyyy"
            )

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
                        date = formatter.format(
                            Date(Long.valueOf(it.getString(it.getColumnIndex(CallLog.Calls.DATE))))
                        ).toString(),
                        //date = Date(Long.valueOf(it.getString(it.getColumnIndex(CallLog.Calls.DATE)))).toString(),
                        duration = it.getString(it.getColumnIndex(CallLog.Calls.DURATION))
                            .toLong(),
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
}
