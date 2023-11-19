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
import android.widget.LinearLayout
import android.widget.QuickContactBadge
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.alexstyl.contactstore.ContactPredicate
import com.alexstyl.contactstore.ContactPredicate.Companion.ContactLookup
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.allContactColumns
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
        var txtContactNumber = itemView.findViewById<TextView>(R.id.txt_Contact_number)
        var profileImage = itemView.findViewById<QuickContactBadge>(R.id.profile_image)
        var call = itemView.findViewById<LinearLayout>(R.id.layout_call)
        var profile_contact = itemView.findViewById<LinearLayout>(R.id.profile_contact)
        var layout_sub_item = itemView.findViewById<LinearLayout>(R.id.layout_sub_item)
        var card_whatsapp = itemView.findViewById<CardView>(R.id.card_whatsapp)
        var card_telegram = itemView.findViewById<CardView>(R.id.card_telegram)
        var cardSms = itemView.findViewById<CardView>(R.id.card_sms)
        var card_call = itemView.findViewById<CardView>(R.id.card_call)
        var txt_Contact_number_count =
            itemView.findViewById<TextView>(R.id.txt_Contact_number_count)
        var Contact_delete =
            itemView.findViewById<CardView>(R.id.card_delete)
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
        holder.txtContactNumber.text = listOfContacts.number
        if (!TextUtils.isEmpty(listOfContacts.photoUri)) {
            holder.profileImage.setImageBitmap(loadContactPhotoThumbnail(listOfContacts.photoUri))
        } else {
            holder.profileImage.setImageToDefault()
        }
        holder.profileImage.setOnClickListener {
            onclick.navigateToProfilePage(
                listOfContacts.name,
                "",
                listOfContacts.photoUri,
                PrefUtils.ContactFragment,
                listOfContacts.contactId
            )
        }
        holder.itemView.setOnClickListener {
            // Get the current state of the item
            // Get the current state of the item
            val expanded: Boolean = listOfContacts.IsExpand
            // Change the state
            // Change the state
            listOfContacts.IsExpand = !expanded
            // Notify the adapter that item has changed
            // Notify the adapter that item has changed
            notifyItemChanged(position)
            val store = ContactStore.newInstance(context.applicationContext)

            store.fetchContacts(
                predicate = ContactLookup(listOfContacts.contactId.toLong()),
                columnsToFetch = allContactColumns()
            )
                .collect { contacts ->
                    val contact = contacts.firstOrNull()
                    if (contact == null) {
                        println("Contact not found")
                    } else {
                        println("Contact found: $contact")
                        if(contact.phones.isNotEmpty()) {
                            println("Contact found: ${contact.phones.get(0).value.raw}")
                            // Use contact.phones, contact.mails, contact.customDataItems etc
                            listOfConttacts[position].number =
                                contact.phones.get(0).value.raw.toString()
                        }
                    }
                }
        }
        // Set the visibility based on state
        // Set the visibility based on state
        holder.layout_sub_item.visibility = if (listOfContacts.IsExpand) View.VISIBLE else View.GONE
        holder.call.setOnClickListener {
            onclick.callOnClick(listOfContacts.number, "")
        }
        holder.card_whatsapp.setOnClickListener {
            onclick.openWhatsApp(listOfContacts.number)

        }
        holder.card_telegram.setOnClickListener {
            onclick.openTelegramApp(listOfContacts.number)
        }
        holder.cardSms.setOnClickListener {
            onclick.openSMSScreen(listOfContacts.number)
        }
        holder.card_call.setOnClickListener {
            if (TextUtils.isEmpty(listOfContacts.name))
                onclick.openPhoneNumberHistory(listOfContacts.number, listOfContacts.number!!)
            else
                onclick.openPhoneNumberHistory(listOfContacts.number, listOfContacts.name!!)

        }
        holder.Contact_delete.setOnClickListener {
            if (!listOfContacts.contactId.isBlank()){
                onclick.deleteContact(listOfContacts.contactId)
            }
        }
    }

    fun addAll(list: List<Contact>) {
        listOfConttacts.clear()
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
                Uri.withAppendedPath(
                    contactUri,
                    ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                )
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