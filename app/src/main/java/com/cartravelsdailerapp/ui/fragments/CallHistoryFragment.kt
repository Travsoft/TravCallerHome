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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.Repositorys.DAO.CallLogsDataSource
import com.cartravelsdailerapp.databinding.FragmentCallHistoryBinding
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import com.cartravelsdailerapp.ui.CallHistroyActivity
import com.cartravelsdailerapp.ui.Dialer
import com.cartravelsdailerapp.ui.ProfileActivity
import com.cartravelsdailerapp.ui.adapters.*
import com.cartravelsdailerapp.utils.CarTravelsDialer
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext


class CallHistoryFragment : Fragment(), CoroutineScope, OnClickListeners {
    var listOfCallHistroy = ArrayList<CallHistory>()
    var listOfContact = ArrayList<Contact>()
    lateinit var binding: FragmentCallHistoryBinding
    lateinit var adapter: CallHistoryAdapter
    private var REQUESTED_CODE_READ_PHONE_STATE = 1003
    lateinit var calendar: Calendar
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var viewModel: MainActivityViewModel
    private lateinit var job: Job
    private val PAGE_START = 1
    private val PAGE_ContactSTART = 1
    private var isLoading = false
    private var isContactLoading = false
    private var isLastPage = false
    private var isContactLastPage = false
    private val TOTAL_PAGES = 5
    private val TOTAL_CONTACT_PAGES = 5
    private var currentPage = PAGE_START
    private var currentContactPage = PAGE_ContactSTART
    lateinit var contactsAdapter: ContactsAdapter
    lateinit var favcontactsAdapter: FavouritesContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        registerBroadCastReceiver()
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            launch {
                viewModel.getNewCallLogsHistory()
                Toast.makeText(context, "getNewCallLogsHistory", Toast.LENGTH_SHORT).show()
            }
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
                if (binding.recyclerViewCallHistory.isVisible) {
                    filter(msg)
                } else {
                    filterContacts(msg)
                }
                return false
            }
        })
        val myViewModelFactory =
            MyViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(
            this,
            myViewModelFactory
        )[MainActivityViewModel::class.java]

        binding.cardHistory.setOnClickListener {
            binding.recyclerViewCallHistory.isVisible = true
            binding.recyListContacts.isVisible = false
            binding.recyListFavouritesContacts.isVisible=false
            binding.txtCallHistory.setTextColor(resources.getColor(R.color.orange))
            binding.txtContacts.setTextColor(resources.getColor(R.color.black))
            binding.viewHistory.setBackgroundColor(resources.getColor(R.color.orange))
            binding.viewContacts.setBackgroundColor(resources.getColor(R.color.white))
            loadData()
        }
        binding.cardContacts.setOnClickListener {
            binding.recyclerViewCallHistory.isVisible = false
            binding.recyListContacts.isVisible = true
            binding.recyListFavouritesContacts.isVisible=true
            binding.txtCallHistory.setTextColor(resources.getColor(R.color.black))
            binding.txtContacts.setTextColor(resources.getColor(R.color.orange))
            binding.viewHistory.setBackgroundColor(resources.getColor(R.color.white))
            binding.viewContacts.setBackgroundColor(resources.getColor(R.color.orange))
            loadContactsData()
        }
        binding.cardHistory.performClick()

        return binding.root
    }

    fun hideSoftKeyboard(view: View, context: Context) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun filter(text: String) {
        if (text.isEmpty()) {
            loadData()
        } else {
            // creating a new array list to filter our data.
            val filteredlist: ArrayList<CallHistory> =
                DatabaseBuilder.getInstance(requireContext()).CallHistoryDao()
                    .searchCall(text) as ArrayList<CallHistory>
            adapter.filterList(filteredlist.distinctBy { u -> u.number } as ArrayList<CallHistory>)
        }
    }

    private fun filterContacts(text: String) {
        if (text.isEmpty()) {
            loadContactsData()
        } else {
            // creating a new array list to filter our data.
            val filteredlist: ArrayList<Contact> =
                DatabaseBuilder.getInstance(requireContext()).CallHistoryDao()
                    .searchContactCall(text) as ArrayList<Contact>
            contactsAdapter.filterList(filteredlist.distinctBy { u -> u.number } as ArrayList<Contact>)
        }
    }


    private fun loadData() {
        setupRV()
        listOfCallHistroy.clear()
        var d = viewModel.getAllCallLogsHistory(0)
        adapter.addAll(d)
    }

    private fun loadContactsData() {
        setUpContactsRv()
        listOfContact.clear()
        var d = viewModel.getAllContacts(0)
        contactsAdapter.addAll(d)
    }

    private fun setupRV() {
        binding.txtNodataFound.isVisible = false
        linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = CallHistoryAdapter(requireContext(), this@CallHistoryFragment)
        binding.recyclerViewCallHistory.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewCallHistory.layoutManager = linearLayoutManager
        binding.recyclerViewCallHistory.adapter = adapter

        binding.recyclerViewCallHistory.addOnScrollListener(object :
            PaginationScrollListener(linearLayoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage;
            }

            override fun isLoading(): Boolean {
                return isLoading;
            }

            override fun loadMoreItems() {
                isLoading = true;
                currentPage += 10;
                loadNextPage();
            }

        });
        viewModel.newCallLogs.observe(this@CallHistoryFragment)
        {
            val date = listOf(it)
            listOfCallHistroy.add(0, date.get(0))
            adapter.notifyDataSetChanged()
        }
        loadFirstPage();


    }

    private fun setUpContactsRv() {
        contactsAdapter = ContactsAdapter(requireContext(), this@CallHistoryFragment)
        val listOfFavouritesContacts =
            DatabaseBuilder.getInstance(requireContext()).CallHistoryDao().getAllFavouriteContacts(true)
        favcontactsAdapter = FavouritesContactAdapter()
        favcontactsAdapter.updateFavouritesContactList(listOfFavouritesContacts)
        linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyListContacts.itemAnimator = DefaultItemAnimator()
        binding.recyListContacts.layoutManager = linearLayoutManager
        binding.recyListContacts.adapter = contactsAdapter
        binding.recyListFavouritesContacts.itemAnimator = DefaultItemAnimator()
        binding.recyListFavouritesContacts.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyListFavouritesContacts.adapter = favcontactsAdapter
        binding.recyListContacts.addOnScrollListener(object :
            PaginationScrollListener(linearLayoutManager) {
            override fun isLastPage(): Boolean {
                return isContactLastPage;
            }

            override fun isLoading(): Boolean {
                return isContactLoading;
            }

            override fun loadMoreItems() {
                isContactLoading = true;
                currentContactPage += 10;
                loadNextContactPage()
            }

        })
        loadContactsFirstPage()
        if (listOfFavouritesContacts.isEmpty() == true) {
            binding.recyListFavouritesContacts.isVisible = false
        } else {
            binding.recyListFavouritesContacts.isVisible = true
        }
    }

    private fun loadFirstPage() {
        viewModel.callLogsdb.observe(this) {
            adapter.addAll(it)
            if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter() else isLastPage =
                true
        }
    }

    private fun loadContactsFirstPage() {
        viewModel.contacts.observe(this) {
            contactsAdapter.addAll(it)
            if (currentContactPage <= TOTAL_CONTACT_PAGES) contactsAdapter.addLoadingFooter() else isContactLastPage =
                true
        }
    }

    private fun loadNextContactPage() {
        contactsAdapter.removeLoadingFooter();
        isContactLoading = false
        val d = DatabaseBuilder.getInstance(requireContext()).CallHistoryDao()
            .getAllContacts(currentContactPage)
        contactsAdapter.addAll(d)
        listOfContact.addAll(d)
        if (currentContactPage != TOTAL_CONTACT_PAGES) {
            contactsAdapter.addLoadingFooter()
        } else {
            isContactLoading = true
        }

    }

    private fun loadNextPage() {
        adapter.removeLoadingFooter();
        isLoading = false
        val d = DatabaseBuilder.getInstance(requireContext()).CallHistoryDao().getAll(currentPage)
        adapter.addAll(d)
        listOfCallHistroy.addAll(d)
        if (currentPage != TOTAL_PAGES) {
            adapter.addLoadingFooter()
        } else {
            isLastPage = true
        }

    }


    private fun registerBroadCastReceiver() {
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(
                receiver,
                IntentFilter(PrefUtils.LOCAL_BROADCAST_KEY)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun callOnClick(number: String, subscriberId: String) {
        val uri = Uri.parse("tel:" + number)
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

    override fun navigateToProfilePage(name: String, number: String, photoUri: String) {
        val data = Bundle()
        if (name.isBlank()) {
            data.putString(CarTravelsDialer.ContactName, number)
        } else {
            data.putString(CarTravelsDialer.ContactName, name)
        }
        data.putString(CarTravelsDialer.ContactNumber, number)
        data.putString(CarTravelsDialer.ContactUri, photoUri)
        val intent = Intent(context, ProfileActivity::class.java)
        intent.putExtras(data)
        context?.startActivity(intent)
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
        data.putString(CarTravelsDialer.ContactNumber, number)
        data.putString(CarTravelsDialer.ContactName, name)
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
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + toNumber))
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
}