package com.cartravelsdailerapp.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telecom.TelecomManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.Repositorys.DAO.CallHistoryDao
import com.cartravelsdailerapp.databinding.FragmentAutoCallListFrgBinding
import com.cartravelsdailerapp.db.AppDatabase
import com.cartravelsdailerapp.db.DatabaseBuilder
import com.cartravelsdailerapp.models.AutoCallContacts
import com.cartravelsdailerapp.ui.adapters.AutoDialerAdapter
import com.cartravelsdailerapp.ui.adapters.IAutoDialerCallbacks
import com.google.android.material.snackbar.Snackbar


class AutoCallListFrg : Fragment(), IAutoDialerCallbacks {
    lateinit var binding: FragmentAutoCallListFrgBinding
    lateinit var db: CallHistoryDao
    lateinit var adapter: AutoDialerAdapter
    lateinit var listOfAutoDialedContacts: List<AutoCallContacts>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAutoCallListFrgBinding.inflate(layoutInflater)
        db = DatabaseBuilder.getInstance(requireContext()).CallHistoryDao()
        getAutoCallData()
        binding.btAdd.setOnClickListener {
            val name = binding.etName.text.toString()
            val number = binding.etNumber.text.toString()
            if (name.isEmpty()) {
                Snackbar.make(binding.root, "Enter Name", Snackbar.LENGTH_SHORT).show()
            } else if (name.isEmpty()) {
                Snackbar.make(binding.root, "Enter Number", Snackbar.LENGTH_SHORT).show()
            } else {
                val autoCallContacts = AutoCallContacts(name, number, false, isAutoDialer = true)
                db.insertAutoCallContacts(autoCallContacts)
                getAutoCallData()
            }
        }
        binding.btAutoCall.setOnClickListener {
            listOfAutoDialedContacts.forEach {
                callPhone(it.phoneNumber)
            }
        }
        return binding.root
    }

    override fun onCheckedChanged(item: AutoCallContacts, isChecked: Boolean) {
        db.updateIsAutoDialerContact(isChecked, item.id)
    }

    fun getAutoCallData() {
        listOfAutoDialedContacts = db.getAutoCallContacts()
        adapter =
            AutoDialerAdapter(listOfAutoDialedContacts as ArrayList<AutoCallContacts>, this)
        binding.recyclerAutoDailedContacts.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerAutoDailedContacts.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun callPhone(number: String) {
        var handller = Handler()
        val telecomManager = context?.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager
        val list = telecomManager.callCapablePhoneAccounts
        handller.postDelayed({
            /* Create an Intent that will start the Menu-Activity. */
            Log.d("ussddata", "inside handler")
            // finish()
        }, 3000)

        //  var phonecall = createPhoneAccountHandle(this, MyConnectionService.EXTRA_PHONE_ACCOUNT)
        Log.d("MyInCallService", "callPhone start")

        var callstring: String


        var uri: Uri = Uri.fromParts("tel", number, null)
        //Connection.PROPERTY_SELF_MANAGED
        /* bundle.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
     bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phonecall)*/

        Log.d("MyInCallService", "callPhone after 3 lines ")


        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val bundle = Bundle()
            bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, list[0])

            telecomManager.placeCall(uri, bundle)
        }


    }

}