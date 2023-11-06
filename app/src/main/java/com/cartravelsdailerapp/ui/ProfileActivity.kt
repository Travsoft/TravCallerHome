package com.cartravelsdailerapp.ui

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageView
import android.widget.QuickContactBadge
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import com.alexstyl.contactstore.ContactPredicate
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.allContactColumns
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.PrefUtils.ContactId
import com.cartravelsdailerapp.PrefUtils.ContactName
import com.cartravelsdailerapp.PrefUtils.ContactNumber
import com.cartravelsdailerapp.PrefUtils.ContactUri
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.LayoutPreviewProfilePicBinding
import com.cartravelsdailerapp.db.DatabaseBuilder
import java.io.FileNotFoundException
import java.io.IOException

class ProfileActivity : AppCompatActivity() {
    var name: String = ""
    var number: String = ""
    var photoUri: String = ""
    var activityType: String = ""
    private lateinit var txt_name: TextView
    private lateinit var img_profile: QuickContactBadge
    private lateinit var card_call: CardView
    private lateinit var card_whatsapp: CardView
    private lateinit var img_Favourite: ImageView
    private lateinit var img_Favouritefilled: ImageView
    var db = DatabaseBuilder.getInstance(this).CallHistoryDao()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()

        txt_name = findViewById(R.id.txt_name)
        img_profile = findViewById(R.id.img_profile)
        card_call = findViewById(R.id.card_call)
        card_whatsapp = findViewById(R.id.card_whatsapp)
        img_Favourite = findViewById(R.id.img_Favourite)
        img_Favouritefilled = findViewById(R.id.img_Favourite_filled)

        val d = intent.extras
        name = d?.getString(ContactName).toString()
        number = d?.getString(ContactNumber).toString()
        photoUri = d?.getString(ContactUri).toString()
        activityType = d?.getString(PrefUtils.ActivityType).toString()
        txt_name.text = name
        val store = ContactStore.newInstance(application)
        val cid = d?.getString(ContactId).toString()
        if (cid!="null") {
            val foundContacts = store.fetchContacts(
                predicate = ContactPredicate.ContactLookup(cid.toLong()),
                allContactColumns()
            )
            foundContacts.collect {
                Log.d("80-> ${cid}", "${it.first().phones[0].value.raw}")
                number = it.first().phones[0].value.raw
            }

        }
        val imageUri = getPhotoFromContacts(number)
        if (!imageUri.isNullOrBlank()) {
            if (!TextUtils.isEmpty(imageUri)) {
                loadContactPhotoThumbnail(imageUri).also {
                    img_profile.setImageBitmap(it)
                }
            } else {
                img_profile.setImageToDefault()
            }
        } else {
            if (!photoUri.isNullOrBlank()) {
                if (!TextUtils.isEmpty(photoUri)) {
                    loadContactPhotoThumbnail(photoUri).also {
                        img_profile.setImageBitmap(it)
                    }
                } else {
                    img_profile.setImageToDefault()
                }
            }
        }

        card_call.setOnClickListener {
            val uri = Uri.parse("tel:" + number)
            val telecomManager = this.getSystemService<TelecomManager>()
            val bundle = Bundle()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    telecomManager?.placeCall(uri, bundle)
                }
            }
        }
        card_whatsapp.setOnClickListener {
            openWhatsAppByNumber(number)
        }
        img_profile.setOnClickListener {
            val dialog = Dialog(this, android.R.style.Theme_Light)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val layoutInflater =
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = LayoutPreviewProfilePicBinding.inflate(layoutInflater)

            dialog.setContentView(binding.root)
            dialog.show()
            dialog.setCancelable(false)
            binding.imgClose.setOnClickListener {
                dialog.dismiss()
            }
            if (!imageUri.isNullOrBlank()) {
                if (!TextUtils.isEmpty(imageUri)) {
                    loadContactPhotoThumbnail(imageUri).also {
                        img_profile.setImageBitmap(it)
                    }
                } else {
                    img_profile.setImageToDefault()
                }
            } else

             if (!imageUri.isNullOrBlank()) {
                 if (!TextUtils.isEmpty(imageUri)) {
                     loadContactPhotoThumbnail(imageUri).also {
                         img_profile.setImageBitmap(it)
                     }
                 } else {
                     img_profile.setImageToDefault()
                 }
             } else {
                 if (!photoUri.isNullOrBlank()) {
                     if (!TextUtils.isEmpty(photoUri)) {
                         loadContactPhotoThumbnail(photoUri).also {
                             img_profile.setImageBitmap(it)
                         }
                     } else {
                         img_profile.setImageToDefault()
                     }
                 }
             }
        }

        if (activityType == PrefUtils.ContactFragment) {
            val data = db.getFavouriteContactsByNumber(number)
            if (data?.isFavourites == true) {
                img_Favourite.isVisible = false
                img_Favouritefilled.isVisible = true
            } else {
                img_Favourite.isVisible = true
                img_Favouritefilled.isVisible = false
            }
            img_Favourite.setOnClickListener {
                it.isVisible = false
                img_Favouritefilled.isVisible = true
                Toast.makeText(this, "Added $number as a your favorites", Toast.LENGTH_SHORT).show()
                db.updateContacts(
                    true, data.id
                )
            }
            img_Favouritefilled.setOnClickListener {
                it.isVisible = false
                img_Favourite.isVisible = true
                Toast.makeText(this, "Removed $number from your favorites list", Toast.LENGTH_SHORT)
                    .show()
                db.updateContacts(false, data.id)
            }
        }

    }

    private fun openWhatsAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber))
        intent.setPackage("com.whatsapp")
        startActivity(intent)

    }

    private fun getPhotoFromContacts(num: String): String? {
        val uri =
            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num))
        //  uri = if (phone_uri != null) Uri.parse(phone_uri) else uri
        val cursor: Cursor? = this.contentResolver.query(uri, null, null, null, null)

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

    /**
     * Load a contact photo thumbnail and return it as a Bitmap,
     * resizing the image to the provided image dimensions as needed.
     * @param photoData photo ID Prior to Honeycomb, the contact's _ID value.
     * For Honeycomb and later, the value of PHOTO_THUMBNAIL_URI.
     * @return A thumbnail Bitmap, sized to the provided width and height.
     * Returns null if the thumbnail is not found.
     */
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}