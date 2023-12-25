package com.cartravelsdailerapp.ui

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.LookupKey
import com.alexstyl.contactstore.thumbnailUri
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivitySearchBinding
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import com.cartravelsdailerapp.ui.adapters.CallHistoryAdapter
import com.cartravelsdailerapp.ui.adapters.ContactsAdapter
import com.cartravelsdailerapp.ui.adapters.OnClickListeners
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext

class SearchActivity : AppCompatActivity(), CoroutineScope, OnClickListeners {
    lateinit var binding: ActivitySearchBinding
    private lateinit var job: Job
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var callLogsAdapter: CallHistoryAdapter

    lateinit var contactsAdapter: ContactsAdapter
    val list = ArrayList<Contact>()
    lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        val myViewModelFactory =
            MyViewModelFactory(this.application)

        viewModel = ViewModelProvider(
            this,
            myViewModelFactory
        )[MainActivityViewModel::class.java]

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        contactsAdapter = ContactsAdapter(this, this)
        binding.recyListContacts.itemAnimator = DefaultItemAnimator()
        binding.recyListContacts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewCallHistory.isNestedScrollingEnabled = false
        binding.recyListContacts.adapter = contactsAdapter

        val listOfContactStore = ContactStore.newInstance(this)
        listOfContactStore.fetchContacts().collect { it ->
            it.forEach {
                if (!it.displayName.isBlank()) {
                    list.add(
                        Contact(
                            it.displayName,
                            "",
                            it.thumbnailUri.toString(),
                            contactId = it.contactId.toString(),
                            isFavourites = it.isStarred,
                            contactsLookUp = it.lookupKey?.value.toString()
                        )
                    )
                }

            }
        }
        callLogsAdapter = CallHistoryAdapter(this, this)
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                launch(Dispatchers.Main) {
                    filterContacts(s.toString())
                    filter(s.toString())
                }

            }

            override fun afterTextChanged(s: Editable?) {
                // Do Nothing
            }

        })

        viewModel.callLogsdb.observe(this) {
            callLogsAdapter.addAll(it)
            val linearLayoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recyclerViewCallHistory.itemAnimator = DefaultItemAnimator()
            binding.recyclerViewCallHistory.setHasFixedSize(false)
            binding.recyclerViewCallHistory.layoutManager = linearLayoutManager
            binding.recyclerViewCallHistory.adapter = callLogsAdapter
            binding.recyclerViewCallHistory.isNestedScrollingEnabled = false

            callLogsAdapter.notifyDataSetChanged()
        }
        binding.imgBack.setOnClickListener {
            finish()
        }

        showKeyboard(binding.etSearch)

    }

    private fun filter(text: String) {
        if (text.isNotBlank()) {
            // creating a new array list to filter our data.
            val filteredlist: ArrayList<CallHistory> =
                DatabaseBuilder.getInstance(this).CallHistoryDao()
                    .searchCall(text) as ArrayList<CallHistory>
            callLogsAdapter.addAll(filteredlist.distinctBy { u -> u.number } as ArrayList<CallHistory>)
            val linearLayoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recyclerViewCallHistory.itemAnimator = DefaultItemAnimator()
            binding.recyclerViewCallHistory.layoutManager = linearLayoutManager
            binding.recyclerViewCallHistory.adapter = callLogsAdapter
            binding.recyclerViewCallHistory.isNestedScrollingEnabled = false
            callLogsAdapter.notifyDataSetChanged()
        } else {
            callLogsAdapter.addAll(emptyList())
            callLogsAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private fun filterContacts(text: String) {
        try {
            if (text.isNotBlank() && list.isNotEmpty()) {
                contactsAdapter.filterList(list.filter { f -> f.name.contains(text, true) }
                    .distinctBy { u -> u.name } as ArrayList<Contact>)
            } else {
                contactsAdapter.addAll(listOf())
                contactsAdapter.notifyDataSetChanged()

            }

        } catch (ex: Exception) {
            print(ex.message)
        }
    }

    override fun callOnClick(number: String, subscriberId: String) {
        val uri = Uri.parse("tel:$number")
        val telecomManager = getSystemService<TelecomManager>()
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
        if (this?.let {
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
                val intent = Intent(this, VisitingCardActivity::class.java)
                intent.putExtras(data)
                startActivity(intent)
            }
        } else {
            data.putString(PrefUtils.ContactId, contactId)
            data.putString(PrefUtils.ContactUri, photoUri)
            data.putString(PrefUtils.ContactName, name)
            data.putString(PrefUtils.ActivityType, activityType)

            val intent = Intent(this, VisitingCardActivity::class.java)
            intent.putExtras(data)
            startActivity(intent)
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
        val intent = Intent(this, CallHistroyActivity::class.java)
        intent.putExtras(data)
        startActivity(intent)
    }

    override fun deleteContact(contactId: String) {

        basicAlert(binding.root, contactId)
    }

    override fun addContact(contactId: String,lookupKey: String,callHistory: CallHistory) {
        TODO("Not yet implemented")
    }

    override fun editContact(contactId: String,lookupKey: String,callHistory: CallHistory) {
        TODO("Not yet implemented")
    }

    override fun block_number(nuumber: String) {
        TODO("Not yet implemented")
    }

    fun isAppInstalled(packageName: String?): Boolean {
        val pm = packageManager
        try {
            pm?.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("isAppInstalled", "error :${e.message}")
        }
        return false
    }

    private fun AlertDialogOpenWhatsApp(number: String) {
        val builder: AlertDialog.Builder? = AlertDialog.Builder(this)
        val layoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        startActivity(intent)

    }

    private fun openDefaultSmsAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$toNumber"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        startActivity(intent)
    }

    fun launchWhatsAppBusinessApp(toNumber: String) {
        val pm: PackageManager? = this.packageManager
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(PrefUtils.WhatsUri + toNumber)
            )
            intent.setPackage(PrefUtils.WhatsAppBusiness)
            // pm.getLaunchIntentForPackage("com.whatsapp.w4b")
            startActivity(intent)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(
                this,
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
        startActivity(intent)
    }

    private fun basicAlert(view: View, contactid: String) {

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setTitle("Delete")
            setMessage("Are you sure your want to delete? ")
            setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->

                launch(Dispatchers.Main) {
                    val listOfContactStore = ContactStore.newInstance(this@SearchActivity)
                    listOfContactStore.execute {
                        delete(
                            contactid.toLong()
                        )
                    }
                    filterContacts("")
                    filter("")
                    binding.etSearch.text.clear()

                }
            }
            setNegativeButton("No") { dialog: DialogInterface, which: Int ->
                dialog.dismiss()
            }
            show()
        }
    }

    // hide keyboard
    fun dismissKeyboard(view: View?) {
        if (view != null) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // show keyboard
    fun showKeyboard(view: View?) {
        if (view == null) return
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view is EditText) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        else imm.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

}