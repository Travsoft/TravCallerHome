package com.cartravelsdailerapp.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.Repositorys.DAO.CallHistoryDao
import com.cartravelsdailerapp.databinding.FragmentCallLogsBinding
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import com.cartravelsdailerapp.db.AppDatabase
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.CallHistroyActivity
import com.cartravelsdailerapp.ui.ProfileActivity
import com.cartravelsdailerapp.ui.VisitingCardActivity
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import com.cartravelsdailerapp.ui.adapters.OnClickListeners
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class CallLogsFrag : Fragment(), CoroutineScope, OnClickListeners {
    lateinit var binding: FragmentCallLogsBinding
    lateinit var viewModel: MainActivityViewModel
    private lateinit var job: Job
    lateinit var callLogsAdapter: CallHistoryAdapter
    lateinit var localBroadcastManager: LocalBroadcastManager
    lateinit var launcherContactAdd: ActivityResultLauncher<Intent>
    lateinit var launcherContactEdit: ActivityResultLauncher<Intent>
    lateinit var db: CallHistoryDao
    private val onResult: (String, String?, Uri?, String) -> Unit =
        { phone, name, photoUri, simIndex ->
            launch {
                viewModel.getNewCallLogsHistory(phone, simIndex)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        localBroadcastManager = LocalBroadcastManager.getInstance(requireContext())
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

        binding.imgProfile.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            context?.startActivity(intent)
        }
        launcherContactAdd =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val contactData: Intent? = result.data
                    val uri: Uri = contactData?.data!!
                    var cNumber: String? = null
                    var name: String? = null
                    val c: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
                    if (c?.moveToFirst() == true) {
                        val id: String =
                            c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                         name =
                            c.getString(c.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                        val hasPhone: String =
                            c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        if (hasPhone.equals("1", ignoreCase = true)) {
                            val phones: Cursor = context?.contentResolver?.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null, null
                            )!!
                            phones.moveToFirst()
                            cNumber = phones.getString(phones.getColumnIndex("data1"))
                        }

                    }
                    if (name != null) {
                        db.updateNameCallHistory(name,id )
                    }
                    callLogsAdapter.notifyDataSetChanged()
                }
            }
        launcherContactEdit =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    callLogsAdapter.notifyDataSetChanged()

                }
            }
        db = DatabaseBuilder.getInstance(requireContext()).CallHistoryDao()

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
                val intent = Intent(context, VisitingCardActivity::class.java)
                intent.putExtras(data)
                context?.startActivity(intent)
            }
        } else {
            data.putString(PrefUtils.ContactId, contactId)
            data.putString(PrefUtils.ContactUri, photoUri)
            data.putString(PrefUtils.ContactName, name)
            data.putString(PrefUtils.ActivityType, activityType)

            val intent = Intent(context, VisitingCardActivity::class.java)
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

    override fun deleteContact(contactId: String) {
        TODO("Not yet implemented")
    }

    override fun addContact(contactId: String, lookupKey: String, callHistory: CallHistory) {
        val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
        contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
        contactIntent
            .putExtra(ContactsContract.Intents.Insert.PHONE, callHistory.number)
        launcherContactAdd.launch(contactIntent)
    }

    override fun editContact(contactId: String, lookupKey: String, callHistory: CallHistory) {
        val contactIntent = Intent(Intent.ACTION_EDIT).apply {
            val contactUri = getLookupUri(contactId.toLong(), lookupKey)
            setDataAndType(contactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
            putExtra(ContactsContract.Intents.Insert.NAME, callHistory.name)
            putExtra(ContactsContract.Intents.Insert.PHONE, callHistory.number)
        }
        launcherContactEdit.launch(contactIntent)
    }

    fun getLookupUri(contactId: Long, lookupKey: String?): Uri? {
        return if (TextUtils.isEmpty(lookupKey)) {
            null
        } else ContentUris.withAppendedId(
            Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                lookupKey
            ), contactId
        )
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private fun updateCachedName(id: String, name: String) {
        val contentValues = ContentValues()
        contentValues.put(CallLog.Calls.CACHED_NAME, name)
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            var updated = context?.contentResolver?.update(
                CallLog.Calls.CONTENT_URI,
                contentValues,
                CallLog.Calls._ID + "=" + id,
                null
            )
            Toast.makeText(context, "" + updated.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}