package com.cartravelsdailerapp.ui.fragments

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.FragmentCallHistoryBinding
import com.cartravelsdailerapp.db.AppDatabase
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import com.cartravelsdailerapp.ui.Dialer
import com.cartravelsdailerapp.ui.ProfileActivity
import com.cartravelsdailerapp.ui.SearchActivity
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator

class CallHistoryFragment : Fragment() {
    lateinit var binding: FragmentCallHistoryBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var viewModel: MainActivityViewModel
    lateinit var db: AppDatabase
    val listofPages = mutableListOf<Fragment>(CallLogsFrag(), ContactsFrag())
    val callHistorytabtitle = arrayOf(
        "Call History",
        "Contacts"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallHistoryBinding.inflate(layoutInflater)
        val myViewModelFactory =
            MyViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(
            this,
            myViewModelFactory
        )[MainActivityViewModel::class.java]

        binding.layoutSearch.setOnClickListener {
            val goToSearchScreen = Intent(requireContext(), SearchActivity::class.java)
            startActivity(goToSearchScreen)
        }

        binding.imgProfile.setOnClickListener {
            var intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    fun hideSoftKeyboard(view: View, context: Context) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.adapter = CallHistoryFraAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = callHistorytabtitle[position]
            when (position) {
                0 -> tab.icon = resources.getDrawable(R.drawable.ic_history)

                1 -> tab.icon = resources.getDrawable(R.drawable.ic_chat)

            }


        }.attach()
        binding.cardDialerBt.setOnClickListener {
            startActivity(
                Intent(requireContext(), Dialer::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            )
        }


    }

    class CallHistoryFraAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        val listofPages = mutableListOf<Fragment>(CallLogsFrag(), ContactsFrag())

        override fun getItemCount(): Int = listofPages.size

        override fun createFragment(position: Int): Fragment {

            return when (position) {
                0 -> CallLogsFrag()
                1 -> ContactsFrag()
                else -> CallLogsFrag()
            }
        }

    }

}

