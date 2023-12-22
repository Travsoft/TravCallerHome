package com.cartravelsdailerapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alexstyl.contactstore.ContactPredicate
import com.alexstyl.contactstore.ContactStore
import com.alexstyl.contactstore.allContactColumns
import com.alexstyl.contactstore.thumbnailUri
import com.cartravelsdailerapp.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VisitingCardViewModel(
    var context: Application,
) : AndroidViewModel(context) {
    private val _contactDetailsByContactId = MutableLiveData<Contact>()
    val contactDetailsByContactId: LiveData<Contact>
        get() = _contactDetailsByContactId

    fun contactDetailsByContactId(cid: String) {
        val store = ContactStore.newInstance(context.applicationContext)
        if (cid != "" && cid != "null") {
            val foundContacts = store.fetchContacts(
                predicate = ContactPredicate.ContactLookup(cid.toLong()),
                allContactColumns()
            )
            foundContacts.collect {
                if (it.isEmpty()){
                    return@collect
                }
                if (it?.first()?.phones?.any() == true) {
                    Log.d("80-> ${cid}", "${it.first().phones[0].value.raw}")
                    val number = it.first().phones[0].value.raw
                    val name = it.first().displayName
                    val cid = it.first().contactId
                    viewModelScope.launch(Dispatchers.Main) {
                        _contactDetailsByContactId.value = Contact(
                            name = name,
                            number = number,
                            it.first().thumbnailUri.toString(),
                            cid.toString(),
                            isFavourites = it.first().isStarred,
                            contactsLookUp = it.first().lookupKey?.value.toString()
                        )
                    }

                }
            }
        }
    }
}