package com.cartravelsdailerapp.ui.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.databinding.FragmentCallHistoryBinding
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import com.cartravelsdailerapp.ui.adapters.PaginationScrollListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.async
import kotlin.collections.ArrayList

class CallHistoryFragment : Fragment() {
    var listOfCallHistroy: ArrayList<CallHistory> = ArrayList()
    lateinit var binding: FragmentCallHistoryBinding
    lateinit var adapter: CallHistoryAdapter
    private var REQUESTED_CODE_READ_PHONE_STATE = 1003
    lateinit var calendar: Calendar
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val PAGE_START = 0
    private var isLoading = false
    private var isLastPage = false
    private var bigDataChunk = listOf<List<CallHistory>>()
    private var pagingData = mutableListOf<CallHistory>()
    private var currentPage: Int = PAGE_START


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        calendar = Calendar.getInstance()
        val dayOfWeekString =
            calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)
        // Inflate the layout for this fragment
        binding = FragmentCallHistoryBinding.inflate(layoutInflater)
        runBlocking {
            setupRV()
            seedData()
            loadData()

        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    suspend fun seedData() {
        runBlocking {
            listOfCallHistroy.addAll(MainActivity.listData)

        }

        bigDataChunk = listOfCallHistroy.chunked(10)
        adapter.notifyDataSetChanged()
    }

    private fun loadData() {
        pagingData.addAll(bigDataChunk[PAGE_START])
        adapter.notifyDataSetChanged()
    }

    private fun setupRV() {
        binding.txtNodataFound.isVisible = false
        linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = CallHistoryAdapter(pagingData as ArrayList<CallHistory>, requireContext())
        binding.recyclerViewCallHistory.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewCallHistory.layoutManager = linearLayoutManager
        binding.recyclerViewCallHistory.adapter = adapter

        binding.recyclerViewCallHistory.addOnScrollListener(object :
            PaginationScrollListener(linearLayoutManager) {
            override fun isLastPage() = isLastPage
            override fun isLoading() = isLoading
            override fun loadMoreItems() {
                isLoading = true
                currentPage += 1
                loadNextPage()
            }
        })

    }

    private fun loadNextPage() {
        binding.loading.visibility = View.VISIBLE
        try {
            // delay 2sec
            Handler(Looper.getMainLooper()).postDelayed({
                bigDataChunk[currentPage].map {
                    pagingData.add(it)
                    binding.recyclerViewCallHistory.post {
                        adapter.notifyItemInserted(pagingData.size - 1)
                    }
                }
                isLoading = false
                binding.loading.visibility = View.GONE
            }, 2000)


        } catch (e: Exception) {
        }
    }

}