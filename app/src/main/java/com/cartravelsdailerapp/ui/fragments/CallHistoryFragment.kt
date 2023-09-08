package com.cartravelsdailerapp.ui.fragments

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.databinding.FragmentCallHistoryBinding
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import kotlinx.coroutines.runBlocking
import java.util.*


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
        binding.searchContacts.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(msg: String): Boolean {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                filter(msg)
                return false
            }
        })


        return binding.root
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
        Collections.reverse(listOfCallHistroy)
        adapter = CallHistoryAdapter( listOfCallHistroy, requireContext())
        //  adapter = CallHistoryAdapter(pagingData as ArrayList<CallHistory>, requireContext())
        binding.recyclerViewCallHistory.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewCallHistory.layoutManager = linearLayoutManager
        binding.recyclerViewCallHistory.adapter = adapter

/*
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
*/

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


        } catch (_: Exception) {
        }
    }

}