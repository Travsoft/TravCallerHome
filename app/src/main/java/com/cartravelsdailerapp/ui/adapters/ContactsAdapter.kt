package com.cartravelsdailerapp.ui.adapters

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.databinding.LayoutItemContactsBinding
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import java.io.FileNotFoundException
import java.io.IOException

class ContactsAdapter(var context: Context, val onclick: OnClickListeners) :
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {
    lateinit var binding: LayoutItemContactsBinding
    var listOfConttacts = ArrayList<Contact>()
    private var isLoadingAdded = false

    inner class ContactsViewHolder(binding: LayoutItemContactsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        binding =
            LayoutItemContactsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listOfConttacts.size
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        with(listOfConttacts[position]) {
            binding.txtContactName.text = this.name
            binding.txtContactNumber.text = this.number
            if (!TextUtils.isEmpty(this.photoUri)) {
                binding.profileImage.setImageBitmap(loadContactPhotoThumbnail(this.photoUri))
            } else {
                binding.profileImage.setImageToDefault()
            }
            binding.profileImage.setOnClickListener {
                onclick.navigateToProfilePage(
                    this.name,
                    this.number,
                    this.photoUri
                )
            }

        }

    }

    fun addAll(list: List<Contact>) {
        listOfConttacts.addAll(list)
    }

    fun filterList(filterlist: ArrayList<Contact>) {
        // below line is to add our filtered
        // list in our course array list.
        listOfConttacts = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    fun removeLoadingContactFooter() {
        isLoadingAdded = false
        val position: Int = listOfConttacts.size - 1
        val result: Contact = listOfConttacts[position]
        if (result != null) {
            listOfConttacts.removeAt(position)
            notifyItemRemoved(position)
        }
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
            afd = context.contentResolver?.openAssetFileDescriptor(thumbUri, "r")
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


}