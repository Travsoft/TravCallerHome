package com.cartravelsdailerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavigatorProvider
import androidx.navigation.fragment.NavHostFragment
import com.cartravelsdailerapp.broadcastreceivers.CustomPhoneStateReceiver
import com.cartravelsdailerapp.databinding.ActivityMainBinding
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    lateinit var vm: MainActivityViewModel
    private val onResult: (String, String?, Uri?) -> Unit = { phone, name, photoUri ->
        launch {
            vm.getNewCallLogsHistory()
        }
    }
    lateinit var receiver : CustomPhoneStateReceiver
    private lateinit var job: Job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        job = Job()
        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
            val myViewModelFactory =
                MyViewModelFactory(this@MainActivity.application)
            vm = ViewModelProvider(
                this@MainActivity,
                myViewModelFactory
            )[MainActivityViewModel::class.java]
            initNavHost()
            setUpBottomNavigation()
        }

    }

    override fun onResume() {
        super.onResume()
        val d = intent.extras
        val  number = d?.getString(PrefUtils.EnteredNumber).toString()
        receiver= CustomPhoneStateReceiver(onResult,number)
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )

    }

    private fun setUpBottomNavigation() {
        val bottomNavigationItems = mutableListOf(
            CurvedBottomNavigation.Model(
                CALL_HISTORY,
                "",
                R.drawable.ic_history
            ),
            CurvedBottomNavigation.Model(
                HOME_ITEM, "",
                R.drawable.ic_home
            ),
            CurvedBottomNavigation.Model(
                Chat_ITEM,
                "",
                R.drawable.ic_chat
            )

        )
        binding.bottomNavigation.apply {
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


    companion object {
        const val HOME_ITEM = R.id.homeFragment
        const val Chat_ITEM = R.id.chatFragment
        const val CALL_HISTORY = R.id.CallHistoryFragment
    }

    override fun onBackPressed() {
    }
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
}
