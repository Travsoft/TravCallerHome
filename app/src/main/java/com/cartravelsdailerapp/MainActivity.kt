package com.cartravelsdailerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.cartravelsdailerapp.databinding.ActivityMainBinding
import com.cartravelsdailerapp.models.CallHistory
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
            initNavHost()
            setUpBottomNavigation()
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

}
