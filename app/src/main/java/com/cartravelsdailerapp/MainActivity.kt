package com.cartravelsdailerapp

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavigatorProvider
import androidx.navigation.fragment.NavHostFragment
import com.cartravelsdailerapp.databinding.ActivityMainBinding
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var REQUESTED_CODE_READ_PHONE_STATE = 1003
    var simpDate = SimpleDateFormat("dd/MM/yyyy kk:mm");
    lateinit var vm: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
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

    private fun setUpBottomNavigation() {
        val bottomNavigationItems = mutableListOf(
            CurvedBottomNavigation.Model(
                CALL_HISTORY,
                "",
                R.drawable.history_svgrepo_com
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUESTED_CODE_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == 0 }) {
                    vm.callLogs.observe(this) {
                        initNavHost()
                    }

                }
            }
        }
    }


}
