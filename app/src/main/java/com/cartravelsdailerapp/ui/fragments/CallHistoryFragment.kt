package com.cartravelsdailerapp.ui.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.PrefUtils.LOCAL_BROADCAST_KEY
import com.cartravelsdailerapp.databinding.FragmentCallHistoryBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.Dialer
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.ArrayList

class CallHistoryFragment : Fragment() {
    var listOfCallHistroy: ArrayList<CallHistory> = ArrayList()
    lateinit var binding: FragmentCallHistoryBinding
    lateinit var adapter: CallHistoryAdapter
    private var REQUESTED_CODE_READ_PHONE_STATE = 1003
    lateinit var calendar: Calendar
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var viewModel: MainActivityViewModel
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.getNewCallLogsHistory()
            listOfCallHistroy = (viewModel.getAllCallLogsHistory()
                    as ArrayList<CallHistory>).sortedBy { i -> i.date }
                .toList() as ArrayList<CallHistory>
            adapter.notifyDataSetChanged()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        calendar = Calendar.getInstance()
        val dayOfWeekString =
            calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)
        binding = FragmentCallHistoryBinding.inflate(layoutInflater)
        binding.cardCallBt.setOnClickListener {
            startActivity(
                Intent(requireContext(), Dialer::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            )
        }
        binding.searchContacts.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(msg: String): Boolean {
                filter(msg)
                return false
            }
        })
        viewModel = (activity as MainActivity).vm
        registerBroadCastReceiver()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    fun hideSoftKeyboard(view: View, context: Context) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<CallHistory> = ArrayList()

        // running a for loop to compare elements.
        for (item in listOfCallHistroy) {
            // checking if the entered string matched with any item of our recycler view.
            if (text.isDigitsOnly()) {
                if (item.number.contains(text.lowercase(Locale.getDefault())) == true
                ) {
                    // if the item is matched we are
                    // adding it to our filtered list.
                    filteredlist.add(item)
                }

            } else {
                if (item.name?.lowercase(Locale.getDefault())
                        ?.contains(text.lowercase(Locale.getDefault())) == true
                ) {
                    // if the item is matched we are
                    // adding it to our filtered list.
                    filteredlist.add(item)
                }

            }
        }

        adapter.filterList(filteredlist)
    }


    private fun loadData() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it.applicationContext,
                    Manifest.permission.READ_PHONE_STATE
                )
            } == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            listOfCallHistroy =
                viewModel.getAllCallLogsHistory() as ArrayList<CallHistory>
            setupRV()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
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

    private fun setupRV() {
        binding.txtNodataFound.isVisible = false
        linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = CallHistoryAdapter(listOfCallHistroy, requireContext())
        binding.recyclerViewCallHistory.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewCallHistory.layoutManager = linearLayoutManager
        binding.recyclerViewCallHistory.adapter = adapter

    }

    private fun registerBroadCastReceiver() {
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(
                receiver,
                IntentFilter(PrefUtils.LOCAL_BROADCAST_KEY)
            )
        }
    }


}