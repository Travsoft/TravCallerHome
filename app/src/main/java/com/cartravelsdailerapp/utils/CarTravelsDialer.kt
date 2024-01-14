package com.cartravelsdailerapp.utils

import android.content.Context
import android.content.pm.PackageManager


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