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
import android.view.View
import android.view.ViewGroup
import android.widget.QuickContactBadge
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.LayoutItemContactsBinding
import com.cartravelsdailerapp.models.Contact
import java.io.FileNotFoundException
import java.io.IOException

class ContactsAdapter(var context: Context, val onclick: OnClickListeners) :
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {
    var listOfConttacts = ArrayList<Contact>()

    class ContactsViewHolder(item: View) :
        RecyclerView.ViewHolder(item) {
        var txtContactName = itemView.findViewById<TextView>(R.id.txt_Contact_name)
        var profileImage = itemView.findViewById<QuickContactBadge>(R.id.profile_image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        return ContactsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_contacts, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listOfConttacts.size
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val listOfContacts = listOfConttacts[position]
        holder.txtContactName.text = listOfContacts.name

        with(listOfConttacts[position]) {
            if (!TextUtils.isEmpty(this.photoUri)) {
                holder.profileImage.setImageBitmap(loadContactPhotoThumbnail(this.photoUri))
            } else {
                holder.profileImage.setImageToDefault()
            }
            holder.profileImage.setOnClickListener {
                onclick.navigateToProfilePage(
                    this.name,
                    this.number,
                    this.photoUri,
                    PrefUtils.ContactFragment
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