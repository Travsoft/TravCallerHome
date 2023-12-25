package com.cartravelsdailerapp.ui.adapters

import com.cartravelsdailerapp.models.CallHistory

interface OnClickListeners {
    fun callOnClick(number: String, subscriberId: String)
    fun navigateToProfilePage(
        name: String,
        number: String,
        photoUri: String,
        activityType: String,
        contactId: String
    )

    fun openWhatsApp(number: String)
    fun openTelegramApp(number: String)
    fun openSMSScreen(number: String)
    fun openPhoneNumberHistory(number: String, name: String)
    fun deleteContact(contactId: String)
    fun addContact(contactId: String, lookupKey: String, callHistory: CallHistory)
    fun editContact(contactId: String, lookupKey: String, callHistory: CallHistory)
    fun block_number(nuumber: String)
}