package com.cartravelsdailerapp.ui.adapters

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import com.cartravelsdailerapp.models.CallHistory
import com.cartravelsdailerapp.ui.ProfileActivity
import com.cartravelsdailerapp.utils.CarTravelsDialer.ContactName
import com.cartravelsdailerapp.utils.CarTravelsDialer.ContactNumber
import com.cartravelsdailerapp.utils.CarTravelsDialer.ContactUri
import java.io.ByteArrayInputStream
import java.io.InputStream


class CallHistoryAdapter(var listCallHistory: ArrayList<CallHistory>, var context: Context) :
    RecyclerView.Adapter<CallHistoryAdapter.CallHistoryVm>() {
    class CallHistoryVm(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name = itemView.findViewById<TextView>(R.id.txt_Contact_name)
        var number = itemView.findViewById<TextView>(R.id.txt_Contact_number)
        var date = itemView.findViewById<TextView>(R.id.txt_Contact_date)
        var calltype = itemView.findViewById<ImageView>(R.id.txt_Contact_type)
        var profile_image = itemView.findViewById<ImageView>(R.id.profile_image)
        var simType = itemView.findViewById<TextView>(R.id.txt_Contact_simtype)
        var duration = itemView.findViewById<TextView>(R.id.txt_Contact_duration)
        var call = itemView.findViewById<LinearLayout>(R.id.layout_call)
        var profile_contact = itemView.findViewById<LinearLayout>(R.id.profile_contact)
        var layout_sub_item = itemView.findViewById<LinearLayout>(R.id.layout_sub_item)
        var card_whatsapp = itemView.findViewById<CardView>(R.id.card_whatsapp)
        var card_telegram = itemView.findViewById<CardView>(R.id.card_telegram)
        var cardSms = itemView.findViewById<CardView>(R.id.card_sms)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHistoryVm {
        return CallHistoryVm(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_item_call_history, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return this.listCallHistory.size
    }

    override fun onBindViewHolder(holder: CallHistoryVm, position: Int) {
        val selectedData = listCallHistory.get(position)

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

        Glide.with(holder.itemView.context)
            .load(BitmapFactory.decodeStream(openPhoto(selectedData.subscriberId.toLong())))
            .into(holder.profile_image)


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
            val uri = Uri.parse("tel:" + selectedData.number)
            val telecomManager = holder.itemView.context.getSystemService<TelecomManager>()
            val callCapablePhoneAccounts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                telecomManager?.callCapablePhoneAccounts
            } else {
                TODO("VERSION.SDK_INT < M")
            }
            val bundle = Bundle()
            if (callCapablePhoneAccounts != null) {
                callCapablePhoneAccounts.find {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        it.id == selectedData.subscriberId
                    } else {
                        TODO("VERSION.SDK_INT < M")
                    }
                }
                    ?.let { handle: PhoneAccountHandle ->
                        bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
                    }
            }
            if (ActivityCompat.checkSelfPermission(
                    holder.itemView.context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                telecomManager?.placeCall(uri, bundle)
            }
        }
        holder.profile_contact.setOnClickListener {
            val data = Bundle()
            data.putString(ContactName, selectedData.name)
            data.putString(ContactNumber, selectedData.number)
            data.putString(ContactUri, selectedData.photouri)
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtras(data)
            context.startActivity(intent)
        }
        holder.card_whatsapp.setOnClickListener {

            if (isAppInstalled("com.whatsapp.w4b")) {
                val location = IntArray(2)
                holder.itemView.getLocationOnScreen(location)
                val point = Point()
                point.x = location[0]
                point.y = location[1]
                showPopup(context, point, selectedData.number)
            } else {
                openWhatsAppByNumber(selectedData.number)
            }
        }
        holder.card_telegram.setOnClickListener {
            openTelegramAppByNumber(selectedData.number)
        }
        holder.cardSms.setOnClickListener {
            openDefaultSmsAppByNumber(selectedData.number)
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

    private fun openWhatsAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber))
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)

    }

    private fun openTelegramAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("tg://openmessage?user_id=" + toNumber))
        intent.setPackage("org.telegram.messenger")
        context.startActivity(intent)
    }

    private fun openDefaultSmsAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + toNumber))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        context.startActivity(intent)
    }

    fun launchWhatsAppBusinessApp(toNumber: String) {
        val pm: PackageManager = context.packageManager
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber)
            )
            intent.setPackage("com.whatsapp.w4b")
            // pm.getLaunchIntentForPackage("com.whatsapp.w4b")
            context.startActivity(intent)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "Please install WA Business App", Toast.LENGTH_SHORT).show()
        } catch (exception: NullPointerException) {
        }
    }

    fun isAppInstalled(packageName: String?): Boolean {
        val pm = context.packageManager
        try {
            pm.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("isAppInstalled", "error :${e.message}")
        }
        return false
    }

    private fun showPopup(context: Context, p: Point, number: String) {

        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val binding = PopupLayoutBinding.inflate(layoutInflater)
        val popUp = PopupWindow(context)
        popUp.contentView = binding.root
        popUp.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popUp.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popUp.isFocusable = true

        val x = 200
        val y = 60
        popUp.setBackgroundDrawable(ColorDrawable())
        popUp.animationStyle = R.style.popup_window_animation
        popUp.showAtLocation(binding.root, Gravity.NO_GRAVITY, p.x + x, p.y + y)

        binding.imageClose.setOnClickListener {
            popUp.dismiss()
        }
        binding.imageWhatsappBussiness.setOnClickListener {
            launchWhatsAppBusinessApp(number)
        }
        binding.imageWhatsapp.setOnClickListener {
            openWhatsAppByNumber(number)
        }

    }

    private fun openPhoto(contactId: Long): InputStream? {
        val contactUri =
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        val photoUri =
            Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
        Log.d("contactId" + contactId, photoUri.toString())
        val cursor: Cursor = context.getContentResolver().query(
            photoUri,
            arrayOf<String>(ContactsContract.Contacts.Photo.PHOTO),
            null,
            null,
            null
        )
            ?: return null
        try {
            if (cursor.moveToFirst()) {
                val data: ByteArray = cursor.getBlob(0)
                if (data != null) {
                    return ByteArrayInputStream(data)
                }
            }
        } finally {
            cursor.close()
        }
        return null
    }
}