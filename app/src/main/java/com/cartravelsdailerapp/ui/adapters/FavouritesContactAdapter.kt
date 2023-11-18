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
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.databinding.ItemCallhistoryBinding
import com.cartravelsdailerapp.databinding.ItemFavouritesContactsBinding
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.models.Contact
import java.io.FileNotFoundException
import java.io.IOException

class FavouritesContactAdapter(val onclick: OnClickListeners) :
    RecyclerView.Adapter<FavouritesContactAdapter.FavouritesContactVM>() {
    lateinit var binding: ItemFavouritesContactsBinding
    lateinit var context: Context
    lateinit var listOfContact: List<Contact>


    inner class FavouritesContactVM(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesContactVM {
        binding = ItemFavouritesContactsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        context = parent.context
        return FavouritesContactVM(binding.root)
    }

    override fun getItemCount(): Int {
        return listOfContact.size
    }

    override fun onBindViewHolder(holder: FavouritesContactVM, position: Int) {
        with(listOfContact[position]) {
            binding.txtContactsName.text = this.name
            binding.quickProfileImage
            if (!TextUtils.isEmpty(this.photoUri)) {
                binding.quickProfileImage.setImageBitmap(loadContactPhotoThumbnail(this.photoUri))
            } else {
                binding.quickProfileImage.setImageToDefault()
            }
            binding.quickProfileImage.setOnClickListener {
                onclick.navigateToProfilePage(
                    this.name,
                    "",
                    this.photoUri,
                    PrefUtils.ContactFragment,
                    this.contactId
                )
            }

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

    fun updateFavouritesContactList(list: List<Contact>) {
        listOfContact = list
        notifyDataSetChanged()
    }
}