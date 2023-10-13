package com.cartravelsdailerapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityCallHistroyBinding
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import com.cartravelsdailerapp.ui.adapters.CallHistoryByNumberAdapter
import com.cartravelsdailerapp.utils.CarTravelsDialer
import com.cartravelsdailerapp.viewmodels.CallHistoryViewmodel
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class CallHistroyActivity : AppCompatActivity(), CoroutineScope {
    lateinit var vm: CallHistoryViewmodel
    lateinit var binding: ActivityCallHistroyBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var adapter: CallHistoryByNumberAdapter
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        val myViewModelFactory =
            MyViewModelFactory(this@CallHistroyActivity.application)

        vm = ViewModelProvider(
            this@CallHistroyActivity,
            myViewModelFactory
        )[CallHistoryViewmodel::class.java]
        val d = intent.extras
        val number = d?.getString(CarTravelsDialer.ContactNumber).toString()
        val name = d?.getString(CarTravelsDialer.ContactName).toString()
        binding = ActivityCallHistroyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        adapter = CallHistoryByNumberAdapter()
        linearLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        launch {
            vm.getCallLogsHistoryByNumber(number)
        }

        vm.callLogsByNumber.observe(this@CallHistroyActivity) {
            adapter.updateCallHistoryByNumber(it)
            binding.recyclerCallHistoryByNumber.itemAnimator = DefaultItemAnimator()
            binding.recyclerCallHistoryByNumber.layoutManager = linearLayoutManager
            binding.recyclerCallHistoryByNumber.adapter = adapter
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
}