package com.cartravelsdailerapp.ui.adapters

import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.models.AutoCallContacts

class AutoDialerAdapter(
    val listOfContacts: ArrayList<AutoCallContacts>,
    val iAutoDialerCallbacks: IAutoDialerCallbacks
) :
    RecyclerView.Adapter<AutoDialerAdapter.AutoDialerAdapterVH>() {
    inner class AutoDialerAdapterVH(v: View) : RecyclerView.ViewHolder(v) {
        val name = v.findViewById<TextView>(R.id.txt_name)
        val phoneNumber = v.findViewById<TextView>(R.id.txt_number)
        val isAutoDialer = v.findViewById<CheckBox>(R.id.ch_isAutoDialerv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoDialerAdapterVH {
        return AutoDialerAdapterVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_autodialer_list, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return listOfContacts.size
    }

    override fun onBindViewHolder(holder: AutoDialerAdapterVH, position: Int) {
        val data = listOfContacts[position]
        holder.name.text = data.name
        holder.phoneNumber.text = data.phoneNumber
        holder.isAutoDialer.isChecked = data.isAutoDialer
        holder.isAutoDialer.setOnCheckedChangeListener { compoundButton, b ->
            iAutoDialerCallbacks.onCheckedChanged(data,b)
        }
    }
}