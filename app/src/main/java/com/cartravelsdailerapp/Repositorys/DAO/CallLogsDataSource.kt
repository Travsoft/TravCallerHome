package com.cartravelsdailerapp.Repositorys.DAO

import android.R
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.cartravelsdailerapp.models.CallHistory
import java.io.InputStream
import java.util.*


class CallLogsDataSource(private val contentResolver: ContentResolver, val context: Context) {
    var simpDate = SimpleDateFormat("dd/MM/yyyy kk:mm");
    val callHistoryList = mutableListOf<CallHistory>()
    private lateinit var callHistory: CallHistory
    var dir: String? = null

    /*CallLog.Calls.DATE + " DESC"*/
    @RequiresApi(Build.VERSION_CODES.M)
    fun fetchCallLogsList(): List<CallHistory> {
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.TYPE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID,
                CallLog.Calls.CACHED_PHOTO_URI
            ),
            null,
            null, CallLog.Calls.DATE + " DESC"
        )
        while (cursor?.moveToNext() == true) {
            when (cursor.getColumnIndex(CallLog.Calls.TYPE).let { cursor.getString(it).toInt() }) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            cursor?.getColumnIndex(CallLog.Calls.NUMBER)
                ?.let { cursor.getString(it) }?.let {
                    CallHistory(
                        number = it,
                        name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                            ?: null,
                        type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)).toInt(),
                        date = simpDate.format(
                            Date(
                                cursor.getLong(
                                    cursor.getColumnIndex(
                                        CallLog.Calls.DATE
                                    )
                                )
                            )
                        ).toString(),
                        duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                            .toString(),
                        subscriberId = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                            ?: "",
                        calType = dir.toString(),
                        photouri = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))
                            ?: "",
                        SimName = getSimCardInfosBySubscriptionId(
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                ?: "0",
                        )?.displayName?.toString() ?: "",
                    )
                }?.let {
                    callHistoryList.add(
                        it
                    )
                }


        }
        cursor?.close()

        return callHistoryList.distinctBy { i -> i.number }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun fetchCallLogSingle(): CallHistory {
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI.buildUpon()
                .appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1")
                .build(), arrayOf(
                CallLog.Calls.TYPE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID,
                CallLog.Calls.CACHED_PHOTO_URI
            ),
            null,
            null, CallLog.Calls.DATE + " DESC"
        )
        while (cursor?.moveToNext() == true) {
            when (cursor.getColumnIndex(CallLog.Calls.TYPE).let { cursor.getString(it).toInt() }) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            cursor?.getColumnIndex(CallLog.Calls.NUMBER)
                ?.let { cursor.getString(it) }?.let {
                    callHistory = CallHistory(
                        number = it,
                        name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                            ?: null,
                        type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)).toInt(),
                        date = simpDate.format(
                            Date(
                                cursor.getLong(
                                    cursor.getColumnIndex(
                                        CallLog.Calls.DATE
                                    )
                                )
                            )
                        ).toString(),
                        duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                            .toString(),
                        subscriberId = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                            ?: "",
                        calType = dir.toString(),
                        photouri = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI))
                            ?: "",
                        SimName = getSimCardInfosBySubscriptionId(
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                ?: "0",
                        )?.displayName?.toString() ?: "",
                    )
                }


        }
        cursor?.close()

        return callHistory

    }

    private fun getSimCardInfosBySubscriptionId(subscriptionId: String): SubscriptionInfo? {
        val subscriptionManager: SubscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        return subscriptionManager.activeSubscriptionInfoList.find {
            try {
                it.subscriptionId == subscriptionId.toInt()
            } catch (e: Exception) {
                return null
            }
        }
    }
/*
    fun getPhoto(phoneNumber: String?): Bitmap? {
        val phoneUri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        var photoUri: Uri? = null
        val cr: ContentResolver = getContentResolver()
        val contact: Cursor? =
            cr.query(phoneUri, arrayOf<String>(ContactsContract.Contacts._ID), null, null, null)
        photoUri = if (contact.moveToFirst()) {
            val userId: Long =
                contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID))
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId)
        } else {
            val defaultPhoto: Bitmap =
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_contact_picture)
            return getCircleBitmap(defaultPhoto)
        }
        if (photoUri != null) {
            val input: InputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                cr, photoUri
            )
            if (input != null) {
                return getCircleBitmap(BitmapFactory.decodeStream(input))
            }
        } else {
            val defaultPhoto: Bitmap =
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_contact_picture)
            return getCircleBitmap(defaultPhoto)
        }
        val defaultPhoto: Bitmap =
            BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture)
        contact.close()
        return defaultPhoto
    }
*/

}