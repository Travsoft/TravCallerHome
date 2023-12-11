package com.cartravelsdailerapp

import android.content.Context
import android.preference.PreferenceManager


object PrefUtils {
    const val ContactName = "contactName"
    const val SIMIndex = "SimIndex"
    const val PhotoUri = "photoUri"
    const val EnteredNumber = "entered_number"
    const val ContactProfile = "imageUri"
    const val ContactNumber = "contactNumber"
    const val ContactUri = "contactUri"
    const val WhatsAppPackage = "com.whatsapp"
    const val TelegramAppPackage = "org.telegram.messenger"
    const val GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"
    var GOOGLE_PAY_REQUEST_CODE = 123
    const val ContactId = "contactid"
    const val CallTravelsSharedPref = "CallTravelsSharedPref"
    const val IsLogin = "IsLogin"
    const val LOCAL_BROADCAST_KEY = "insert_incomingCall_local_broadcast"
    const val ACTION_STEP_COUNTER_NOTIFICATION = "ACTION_STEP_COUNTER_NOTIFICATION"
    const val PERMISION_REQUEST = 100
    const val REQUESTED_CODE_READ_PHONE_STATE = 1003
    const val OUTGOING = "OUTGOING"
    const val INCOMING = "INCOMING"
    const val MISSED = "MISSED"
    const val DataFormate = "dd/MM/yyyy kk:mm"
    const val WhatsApp = "com.whatsapp"
    const val WhatsAppBusiness = "com.whatsapp.w4b"
    const val TelegramMessage = "org.telegram.messenger"
    const val WhatsUri = "http://api.whatsapp.com/send?phone="
    const val TelegramUri = "tg://openmessage?user_id="
    const val PackageName = "com.cartravelsdailerapp"
    const val KeyEmail = "email"
    const val KeyPhoneNumber = "number"
    const val ActivityType = "activity_type"
    const val CallHistoryFragment = "CallHistoryFragment"
    const val ContactFragment = "ContactFragment"
    fun getBoolean(contect1: Context?, string1: String?, bool1: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(contect1).getBoolean(string1, bool1)
    }

    fun getInt(ass: Context?, ssss: String?, sdss: Int): Int {
        return PreferenceManager.getDefaultSharedPreferences(ass).getInt(ssss, sdss)
    }

    fun getFloat(sa: Context?, hrfjkhnfgv: String?, efdfdf: Float): Float {
        return PreferenceManager.getDefaultSharedPreferences(sa).getFloat(hrfjkhnfgv, efdfdf)
    }

    fun getString(sa: Context?, jh: String?, defstring: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(sa).getString(jh, defstring)
    }

    //////////////////////////////Setter
    fun setBoolean(context2: Context?, string2: String?, boolean2: Boolean) {
        val localEditor = PreferenceManager.getDefaultSharedPreferences(context2).edit()
        localEditor.putBoolean(string2, boolean2)
        localEditor.apply()
    }

    fun setString(context2: Context?, string2: String?, str: String?) {
        val localEditor = PreferenceManager.getDefaultSharedPreferences(context2).edit()
        localEditor.putString(string2, str)
        localEditor.apply()
    }

    fun setFloat(context3: Context?, string3: String?, float1: Float) {
        val localEditor = PreferenceManager.getDefaultSharedPreferences(context3).edit()
        localEditor.putFloat(string3, float1)
        localEditor.apply()
    }

    fun setInt(context32: Context?, stringss: String?, `val`: Int) {
        val localEditor = PreferenceManager.getDefaultSharedPreferences(context32).edit()
        localEditor.putInt(stringss, `val`)
        localEditor.apply()
    }
}