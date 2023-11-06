package com.cartravelsdailerapp.utils

import android.content.Context
import android.content.pm.PackageManager
import com.cartravelsdailerapp.models.Contact
import com.cartravelsdailerapp.ui.fragments.CallHistoryFragment


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