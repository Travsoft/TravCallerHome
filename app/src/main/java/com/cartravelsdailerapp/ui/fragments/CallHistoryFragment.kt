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
        initScrollListener()
        return binding.root
    }

    private fun initScrollListener() {
        binding.recyclerViewCallHistory.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == listOfCallHistroy.size - 1) {
                        //bottom of list!
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }

    private fun loadMore() {
        adapter.notifyItemInserted(listOfCallHistroy.size - 1)
        val handler = Handler()
        handler.postDelayed(Runnable {
            listOfCallHistroy.removeAt(listOfCallHistroy.size - 1)
            val scrollPosition: Int = listOfCallHistroy.size
            adapter.notifyItemRemoved(scrollPosition)
            var currentSize = scrollPosition
            val nextLimit = currentSize + 10
            while (currentSize - 1 < nextLimit) {
                listOfCallHistroy.add(listOfCallHistroy[nextLimit])
                currentSize++
            }
            adapter.notifyDataSetChanged()
            isLoading = false
        }, 2000)
    }

}