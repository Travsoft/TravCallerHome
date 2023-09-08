package com.cartravelsdailerapp.ui.adapters

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.models.CallHistory


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
        if (TextUtils.isEmpty(selectedData.SimName.replace("SIM",""))) {
            holder.simType.text = selectedData.subscriberId
        }else{
            holder.simType.text = selectedData.SimName.replace("SIM","")

        }
        holder.duration.text = "${selectedData.duration} See"

/*
        if (!TextUtils.isEmpty(selectedData.photouri)) {
            Glide.with(holder.itemView.context)
                .load(
                    selectedData.photouri
                )
                .into(holder.profile_image)
        }
*/

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
    }

    fun filterList(filterlist: ArrayList<CallHistory>) {
        // below line is to add our filtered
        // list in our course array list.
        listCallHistory = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }
}