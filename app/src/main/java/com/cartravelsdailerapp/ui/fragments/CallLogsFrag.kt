package com.cartravelsdailerapp.ui.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.broadcastreceivers.CustomPhoneStateReceiver
import com.cartravelsdailerapp.databinding.FragmentCallLogsBinding
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import com.cartravelsdailerapp.ui.CallHistroyActivity
import com.cartravelsdailerapp.ui.ProfileActivity
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import com.cartravelsdailerapp.ui.adapters.OnClickListeners
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale.filter
import kotlin.coroutines.CoroutineContext

class CallLogsFrag : Fragment(), CoroutineScope, OnClickListeners {
    lateinit var binding: FragmentCallLogsBinding
    lateinit var viewModel: MainActivityViewModel
    private lateinit var job: Job
    lateinit var callLogsAdapter: CallHistoryAdapter
    var receiver: CustomPhoneStateReceiver? = null
    private val onResult: (String, String?, Uri?) -> Unit = { phone, name, photoUri ->
        launch {
            viewModel.getNewCallLogsHistory(phone, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myViewModelFactory =
            MyViewModelFactory(requireActivity().application)

        viewModel = ViewModelProvider(
            this,
            myViewModelFactory
        )[MainActivityViewModel::class.java]
        binding = FragmentCallLogsBinding.inflate(layoutInflater)
        callLogsAdapter = CallHistoryAdapter(requireContext(), this)
        viewModel.getCallLogsHistoryDb()
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
        binding.recyclerViewCallHistory.isNestedScrollingEnabled = false
        ViewCompat.setNestedScrollingEnabled(binding.recyclerViewCallHistory, false)

        return binding.root
    }

    private fun filter(text: String) {
        if (text.isEmpty()) {
            viewModel.getCallLogsHistoryDb()
        } else {
            // creating a new array list to filter our data.
            val filteredlist: ArrayList<CallHistory> =
                DatabaseBuilder.getInstance(requireContext()).CallHistoryDao()
                    .searchCall(text) as ArrayList<CallHistory>
            callLogsAdapter.addAll(filteredlist.distinctBy { u -> u.number } as ArrayList<CallHistory>)
            val linearLayoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.recyclerViewCallHistory.itemAnimator = DefaultItemAnimator()
            binding.recyclerViewCallHistory.layoutManager = linearLayoutManager
            binding.recyclerViewCallHistory.adapter = callLogsAdapter
            callLogsAdapter.notifyDataSetChanged()
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.callLogsdb.observe(this) {
            callLogsAdapter.addAll(it)
            val linearLayoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.recyclerViewCallHistory.itemAnimator = DefaultItemAnimator()
            binding.recyclerViewCallHistory.setHasFixedSize(false)
            // binding.recyclerViewCallHistory.isNestedScrollingEnabled = false
            binding.recyclerViewCallHistory.layoutManager = linearLayoutManager
            binding.recyclerViewCallHistory.adapter = callLogsAdapter

            callLogsAdapter.notifyDataSetChanged()
        }
        // register broadcast manager
        val localBroadcastManager = LocalBroadcastManager.getInstance(requireContext())
        localBroadcastManager.registerReceiver(
            receiver_local,
            IntentFilter(PrefUtils.LOCAL_BROADCAST_KEY)
        )


    }

    var receiver_local: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val phone = intent.getStringExtra(PrefUtils.ContactNumber)
                phone?.let { viewModel.getNewCallLogsHistory(it, "") }
                Log.e("98--phone", "Number is ,$phone")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun callOnClick(number: String, subscriberId: String) {
        val uri = Uri.parse("tel:$number")
        val telecomManager = context?.getSystemService<TelecomManager>()
        val callCapablePhoneAccounts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            telecomManager?.callCapablePhoneAccounts
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        val bundle = Bundle()
        if (callCapablePhoneAccounts != null) {
            callCapablePhoneAccounts.find {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    it.id == subscriberId
                } else {
                    TODO("VERSION.SDK_INT < M")
                }
            }
                ?.let { handle: PhoneAccountHandle ->
                    bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
                }
        }
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.CALL_PHONE
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {
            telecomManager?.placeCall(uri, bundle)
        }
        receiver = CustomPhoneStateReceiver(onResult, number)
        ContextCompat.registerReceiver(
            requireContext(),
            receiver,
            IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun navigateToProfilePage(
        name: String,
        number: String,
        photoUri: String,
        activityType: String,
        contactId: String
    ) {
        val data = Bundle()

        if (activityType.equals(PrefUtils.CallHistoryFragment)) {
            if (name.isBlank()) {
                data.putString(PrefUtils.ContactName, number)
            } else {
                data.putString(PrefUtils.ContactName, name)
                data.putString(PrefUtils.ContactId, contactId)
            }
            data.putString(PrefUtils.ContactNumber, number)
            data.putString(PrefUtils.ContactUri, photoUri)
            data.putString(PrefUtils.ActivityType, activityType)

            if (!number.isNullOrBlank() || !name.isNullOrBlank()) {
                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtras(data)
                context?.startActivity(intent)
            }
        } else {
            data.putString(PrefUtils.ContactId, contactId)
            data.putString(PrefUtils.ContactUri, photoUri)
            data.putString(PrefUtils.ContactName, name)
            data.putString(PrefUtils.ActivityType, activityType)

            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtras(data)
            context?.startActivity(intent)
        }
    }

    override fun openWhatsApp(number: String) {
        if (isAppInstalled("com.whatsapp.w4b")) {
            // showPopup(context, point, selectedData.number)
            AlertDialogOpenWhatsApp(number)
        } else {
            openWhatsAppByNumber(number)
        }

    }

    override fun openTelegramApp(number: String) {
        openTelegramAppByNumber(number)

    }

    override fun openSMSScreen(number: String) {
        openDefaultSmsAppByNumber(number)
    }

    override fun openPhoneNumberHistory(number: String, name: String) {
        val data = Bundle()
        data.putString(PrefUtils.ContactNumber, number)
        data.putString(PrefUtils.ContactName, name)
        val intent = Intent(context, CallHistroyActivity::class.java)
        intent.putExtras(data)
        context?.startActivity(intent)
    }

    fun isAppInstalled(packageName: String?): Boolean {
        val pm = context?.packageManager
        try {
            pm?.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("isAppInstalled", "error :${e.message}")
        }
        return false
    }

    private fun AlertDialogOpenWhatsApp(number: String) {
        val builder: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
        val layoutInflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupLayoutBinding.inflate(layoutInflater)
        builder?.setView(binding.root)

        val dialog: AlertDialog = builder!!.create()
        dialog.show()
        binding.imageClose.setOnClickListener {
            dialog.dismiss()
        }
        binding.imageWhatsappBussiness.setOnClickListener {
            launchWhatsAppBusinessApp(number)
        }
        binding.imageWhatsapp.setOnClickListener {
            openWhatsAppByNumber(number)
        }
    }

    private fun openWhatsAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(PrefUtils.WhatsUri + toNumber))
        intent.setPackage(PrefUtils.WhatsApp)
        context?.startActivity(intent)

    }

    private fun openDefaultSmsAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$toNumber"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        context?.startActivity(intent)
    }

    fun launchWhatsAppBusinessApp(toNumber: String) {
        val pm: PackageManager? = context?.packageManager
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(PrefUtils.WhatsUri + toNumber)
            )
            intent.setPackage(PrefUtils.WhatsAppBusiness)
            // pm.getLaunchIntentForPackage("com.whatsapp.w4b")
            context?.startActivity(intent)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(
                context,
                getText(R.string.Please_install_WA_Business_App),
                Toast.LENGTH_SHORT
            ).show()
        } catch (exception: NullPointerException) {
        }
    }


    private fun openTelegramAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(PrefUtils.TelegramUri + toNumber))
        intent.setPackage(PrefUtils.TelegramMessage)
        context?.startActivity(intent)
    }

    override fun onDetach() {
        super.onDetach()
        if (receiver != null)
            context?.unregisterReceiver(receiver)

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


}