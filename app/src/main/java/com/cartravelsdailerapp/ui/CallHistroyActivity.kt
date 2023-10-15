package com.cartravelsdailerapp.ui

import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
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
import java.io.FileNotFoundException
import java.io.IOException
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
        val imageUri = getPhotoFromContacts(number)
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
        binding.imgBack.setOnClickListener {
            this.onBackPressed()
        }
        if (!TextUtils.isEmpty(name)) {
            binding.txtContactName.text = name
        } else {
            binding.txtContactName.text = number
        }
        binding.txtContactNumber.text = number
        if (!TextUtils.isEmpty(imageUri) && imageUri != null) {
            loadContactPhotoThumbnail(imageUri).also {
                binding.imgProfile.setImageBitmap(it)
            }
        } else {
            binding.imgProfile.setImageToDefault()
        }
    }
    private fun getPhotoFromContacts(num: String): String? {
        val uri =
            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num))
        //  uri = if (phone_uri != null) Uri.parse(phone_uri) else uri
        val cursor: Cursor? = this.getContentResolver().query(uri, null, null, null, null)

        if (cursor != null) {
            if (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
                val image_uri =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI))
                Log.d("image_uri-->", "name $name id $id image_uri $image_uri")
                return image_uri
            }
            cursor.close()
        }
        return ""
    }

    private fun loadContactPhotoThumbnail(photoData: String): Bitmap? {
        // Creates an asset file descriptor for the thumbnail file
        var afd: AssetFileDescriptor? = null
        // try-catch block for file not found
        return try {
            // Creates a holder for the URI
            val thumbUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                // If Android 3.0 or later,
                // sets the URI from the incoming PHOTO_THUMBNAIL_URI
                Uri.parse(photoData)
            } else {
                // Prior to Android 3.0, constructs a photo Uri using _ID
                /*
                 * Creates a contact URI from the Contacts content URI
                 * incoming photoData (_ID)
                 */
                val contactUri: Uri =
                    Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, photoData)
                /*
                 * Creates a photo URI by appending the content URI of
                 * Contacts.Photo
                 */
                Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
            }

            /*
             * Retrieves an AssetFileDescriptor object for the thumbnail URI
             * using ContentResolver.openAssetFileDescriptor
             */
            afd = this.contentResolver?.openAssetFileDescriptor(thumbUri, "r")
            /*
             * Gets a file descriptor from the asset file descriptor.
             * This object can be used across processes.
             */
            return afd?.fileDescriptor?.let { fileDescriptor ->
                // Decodes the photo file and returns the result as a Bitmap
                // if the file descriptor is valid
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null)
            }
        } catch (e: FileNotFoundException) {
            /*
             * Handle file not found errors
             */
            null
        } finally {
            // In all cases, close the asset file descriptor
            try {
                afd?.close()
            } catch (e: IOException) {
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
}