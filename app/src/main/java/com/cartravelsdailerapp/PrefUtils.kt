package com.cartravelsdailerapp

import android.content.Context
import android.preference.PreferenceManager


object PrefUtils {
    const val CallTravelsSharedPref = "CallTravelsSharedPref"
    const val IsLogin = "IsLogin"
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