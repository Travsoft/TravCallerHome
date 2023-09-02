package com.cartravelsdailerapp.ui.adapters

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.models.CallHistory

class CallHistoryAdapter(var listCallHistory: ArrayList<CallHistory>, var context: Context) :
    RecyclerView.Adapter<CallHistoryAdapter.CallHistoryVm>() {
    var REQUESTED_CODE_CALLPHONE: Int = 1002

    class CallHistoryVm(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.txt_Contact_name)
        var number: TextView = itemView.findViewById(R.id.txt_Contact_number)
        var date: TextView = itemView.findViewById(R.id.txt_Contact_date)
        var calltype: ImageView = itemView.findViewById(R.id.txt_Contact_type)
        var profile_image: ImageView = itemView.findViewById(R.id.profile_image)
        var simType: TextView = itemView.findViewById(R.id.txt_Contact_simtype)
        var duration: TextView = itemView.findViewById(R.id.txt_Contact_duration)
        var call: LinearLayout = itemView.findViewById(R.id.layout_call)

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

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: CallHistoryVm, position: Int) {
        val selectedData = listCallHistory.get(position)
        if (selectedData.name.isNullOrBlank()) {
            holder.name.text = selectedData.number
        } else {
            holder.name.text = selectedData.name
        }
        holder.number.text = selectedData.number
        holder.date.text = selectedData.date
        holder.simType.text = selectedData.SimName
        holder.duration.text = selectedData.duration.toString() + " Sec"

        if (!TextUtils.isEmpty(selectedData.photouri)) {
            Glide.with(context.applicationContext)
                .load(
                    Uri.parse(selectedData.photouri)
                )
                .error(android.R.mipmap.sym_def_app_icon)
                .into(holder.profile_image)
        }
        when (selectedData.calType) {
            "OUTGOING" -> {
                holder.calltype.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.outgoing
                    )
                )
            }
            "INCOMING" -> {
                holder.calltype.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.incoming
                    )
                )
            }
            "MISSED" -> {
                holder.calltype.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.missed
                    )
                )
            }
        }
        holder.call.setOnClickListener {
            val uri = Uri.parse("tel:" + selectedData.number)
            val telecomManager = context.getSystemService<TelecomManager>()
            val callCapablePhoneAccounts = telecomManager?.callCapablePhoneAccounts
            val bundle = Bundle()
            if (callCapablePhoneAccounts != null) {
                callCapablePhoneAccounts.find { it.id == selectedData.subscriberId }
                    ?.let { handle: PhoneAccountHandle ->
                        bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
                    }
            }
            telecomManager?.placeCall(uri, bundle)
        }
    }
}