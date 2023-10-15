package com.cartravelsdailerapp.ui.adapters

interface OnClickListeners {
    fun callOnClick(number: String, subscriberId: String)
    fun navigateToProfilePage(name: String, number: String, photoUri: String)
    fun openWhatsApp(number: String)
    fun openTelegramApp(number: String)
    fun openSMSScreen(number: String)
    fun openPhoneNumberHistory(number: String,name: String)
}