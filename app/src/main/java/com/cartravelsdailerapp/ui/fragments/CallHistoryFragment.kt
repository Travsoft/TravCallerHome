package com.cartravelsdailerapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.provider.CallLog
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.databinding.FragmentCallHistoryBinding
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import java.lang.Long
import java.util.*
import kotlin.Int
import kotlin.String
import kotlin.collections.ArrayList
import kotlin.let
import kotlin.toString


class CallHistoryFragment : Fragment() {
    var listOfCallHistroy: ArrayList<CallHistory> = ArrayList()
    var isLoading = false
    lateinit var binding: FragmentCallHistoryBinding
    lateinit var adapter: CallHistoryAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCallHistoryBinding.inflate(layoutInflater)
        listOfCallHistroy = MainActivity.listData
        adapter = CallHistoryAdapter(listOfCallHistroy,container!!.context.applicationContext)
        // Creates a vertical Layout Manager
        binding.recyclerViewCallHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCallHistory.adapter = adapter
        //initScrollListener()
        return binding.root
    }
}