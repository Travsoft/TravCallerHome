package com.cartravelsdailerapp.ui.fragments

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.cartravelsdailerapp.PrefUtils
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
import com.squareup.picasso.Picasso

class CallHistoryFragment : Fragment() {
    lateinit var binding: FragmentCallHistoryBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var viewModel: MainActivityViewModel
    lateinit var db: AppDatabase
    val listofPages = mutableListOf<Fragment>(CallLogsFrag(), ContactsFrag())
    val callHistorytabtitle = arrayOf(
        "Call History",
        "Contacts",
        "Auto Call"
    )
    lateinit var sharedPreferences: SharedPreferences

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
        sharedPreferences = requireContext().getSharedPreferences(
            PrefUtils.CallTravelsSharedPref,
            AppCompatActivity.MODE_PRIVATE
        )

        val email = sharedPreferences.getString(PrefUtils.KeyEmail, "")
        val phoneNumber = sharedPreferences.getString(PrefUtils.KeyPhoneNumber, "")
        val userId = sharedPreferences.getString(PrefUtils.userId, "")
        val profileUrl = sharedPreferences.getString(PrefUtils.UserProfileUrl, "")
        val token = sharedPreferences.getString(PrefUtils.userToken, "")

        if (profileUrl?.isNotEmpty() == true) {
            Glide
                .with(this)
                .load(profileUrl)
                .centerCrop()
                .centerInside()
                .transform( RoundedCorners(5))
                .placeholder(R.drawable.userprofile)
                .into(binding.imgProfile)
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
                2 -> tab.icon = resources.getDrawable(R.drawable.ic_call)

            }


        }.attach()
        binding.cardDialerBt.setOnClickListener {
            startActivity(
                Intent(requireContext(), Dialer::class.java)
                /*.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),*/
            )
        }


    }

    class CallHistoryFraAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        val listofPages = mutableListOf<Fragment>(CallLogsFrag(), ContactsFrag(),AutoCallListFrg())

        override fun getItemCount(): Int = listofPages.size

        override fun createFragment(position: Int): Fragment {

            return when (position) {
                0 -> CallLogsFrag()
                1 -> ContactsFrag()
                2-> AutoCallListFrg()
                else -> CallLogsFrag()
            }
        }

    }

}

