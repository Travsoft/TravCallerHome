package com.cartravelsdailerapp.utils

import android.content.Context
import android.content.pm.PackageManager


object CarTravelsDialer {
    const val ContactName = "contactName"
    const val ContactProfile = "imageUri"
    const val ContactNumber = "contactNumber"
    const val ContactUri = "contactUri"
    const val WhatsAppPackage = "com.whatsapp"
    const val TelegramAppPackage = "org.telegram.messenger"
    const val GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"
    var GOOGLE_PAY_REQUEST_CODE = 123
}

fun Context.isPackageInstalled(context: Context, packageName: String): Boolean {

    var available = true
    try {
        // check if available
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    } catch (e: PackageManager.NameNotFoundException) {
        // if not available set
        // available as false
        available = false
    }
    return available
}