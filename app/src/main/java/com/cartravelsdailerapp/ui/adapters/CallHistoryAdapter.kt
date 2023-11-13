package com.cartravelsdailerapp.ui.adapters

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.QuickContactBadge
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.models.CallHistory
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class CallHistoryAdapter(
    var context: Context,
    val onclick: OnClickListeners
) :
    RecyclerView.Adapter<CallHistoryAdapter.CallHistoryVm>() {
    private var isLoadingAdded = false
    var listCallHistory = ArrayList<CallHistory>()

    class CallHistoryVm(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name = itemView.findViewById<TextView>(R.id.txt_Contact_name)
        var number = itemView.findViewById<TextView>(R.id.txt_Contact_number)
        var date = itemView.findViewById<TextView>(R.id.txt_Contact_date)
        var calltype = itemView.findViewById<ImageView>(R.id.txt_Contact_type)
        var profile_image = itemView.findViewById<QuickContactBadge>(R.id.profile_image)
        var simType = itemView.findViewById<TextView>(R.id.txt_Contact_simtype)
        var duration = itemView.findViewById<TextView>(R.id.txt_Contact_duration)
        var call = itemView.findViewById<LinearLayout>(R.id.layout_call)
        var profile_contact = itemView.findViewById<LinearLayout>(R.id.profile_contact)
        var layout_sub_item = itemView.findViewById<LinearLayout>(R.id.layout_sub_item)
        var card_whatsapp = itemView.findViewById<CardView>(R.id.card_whatsapp)
        var card_telegram = itemView.findViewById<CardView>(R.id.card_telegram)
        var cardSms = itemView.findViewById<CardView>(R.id.card_sms)
        var card_call = itemView.findViewById<CardView>(R.id.card_call)
        var txt_Contact_number_count =
            itemView.findViewById<TextView>(R.id.txt_Contact_number_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHistoryVm {
        return CallHistoryVm(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_call_history, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listCallHistory.size
    }

    override fun onBindViewHolder(holder: CallHistoryVm, position: Int) {
        val selectedData = listCallHistory[position]

        if (selectedData.name.isNullOrBlank()) {
            holder.name.text = selectedData.number
        } else {
            holder.name.text = selectedData.name
        }
        holder.number.text = selectedData.number
        holder.date.text = selectedData.date
        if (TextUtils.isEmpty(selectedData.SimName.replace("SIM", ""))) {
            holder.simType.isVisible = false
        } else {
            holder.simType.text = selectedData.SimName.replace("SIM", "")

        }
        holder.duration.text = "${selectedData.duration} See"
        holder.itemView.setOnClickListener {
            // Get the current state of the item
            // Get the current state of the item
            val expanded: Boolean = selectedData.IsExpand
            // Change the state
            // Change the state
            selectedData.IsExpand = !expanded
            // Notify the adapter that item has changed
            // Notify the adapter that item has changed
            notifyItemChanged(position)
        }
        // Set the visibility based on state
        // Set the visibility based on state
        holder.layout_sub_item.visibility = if (selectedData.IsExpand) View.VISIBLE else View.GONE
        val imageUri = getPhotoFromContacts(selectedData.number)
        if (!TextUtils.isEmpty(imageUri) && imageUri != null) {
            loadContactPhotoThumbnail(imageUri).also {
                holder.profile_image.setImageBitmap(it)
            }
        } else {
            holder.profile_image.setImageToDefault()
        }
        when (selectedData.calType) {
            "OUTGOING" -> {
                holder.calltype.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        R.drawable.arrow_blue
                    )
                )
            }
            "INCOMING" -> {
                holder.calltype.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        R.drawable.arrow_green
                    )
                )
            }
            "MISSED" -> {
                holder.calltype.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        R.drawable.arrow_red
                    )
                )
            }
        }
        holder.call.setOnClickListener {
            onclick.callOnClick(selectedData.number, selectedData.subscriberId)
        }
        holder.profile_image.setOnClickListener {
            onclick.navigateToProfilePage(
                selectedData.name!!,
                selectedData.number,
                selectedData.photouri,
                PrefUtils.CallHistoryFragment,
                ""

            )
        }
        holder.card_whatsapp.setOnClickListener {
            onclick.openWhatsApp(selectedData.number)

        }
        holder.card_telegram.setOnClickListener {
            onclick.openTelegramApp(selectedData.number)
        }
        holder.cardSms.setOnClickListener {
            onclick.openSMSScreen(selectedData.number)
        }
        holder.card_call.setOnClickListener {
            if (TextUtils.isEmpty(selectedData.name))
                onclick.openPhoneNumberHistory(selectedData.number, selectedData.number!!)
            else
                onclick.openPhoneNumberHistory(selectedData.number, selectedData.name!!)

        }
    }

    fun filterList(filterlist: ArrayList<CallHistory>) {
        // below line is to add our filtered
        // list in our course array list.
        listCallHistory = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }


    private fun getPhotoFromContacts(num: String): String? {
        val uri =
            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num))
        //  uri = if (phone_uri != null) Uri.parse(phone_uri) else uri
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

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
            afd = context?.contentResolver?.openAssetFileDescriptor(thumbUri, "r")
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

    fun addLoadingFooter() {
        isLoadingAdded = true
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        val position: Int = listCallHistory.size - 1
        listCallHistory.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }

    fun add(callHistory: CallHistory) {
        listCallHistory.add(callHistory)
        notifyItemInserted(listCallHistory.size - 1)
    }

    fun addAll(list: List<CallHistory>) {
        listCallHistory.clear()
        listCallHistory.addAll(list)
    }

    fun setMovieList(list: ArrayList<CallHistory>) {
        listCallHistory = list
    }
}