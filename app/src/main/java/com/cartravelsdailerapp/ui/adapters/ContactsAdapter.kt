package com.cartravelsdailerapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.databinding.LayoutItemContactsBinding
import com.cartravelsdailerapp.models.Contact

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {
    lateinit var binding: LayoutItemContactsBinding
    lateinit var listOfConttacts: ArrayList<Contact>

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

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        with(listOfConttacts[position]) {
            binding.txtContactName.text=this.name
            binding.txtContactNumber.text=this.number
        }

    }

    fun updateContacts(list: List<Contact>) {
        listOfConttacts = list as ArrayList<Contact>
        notifyDataSetChanged()
    }
}