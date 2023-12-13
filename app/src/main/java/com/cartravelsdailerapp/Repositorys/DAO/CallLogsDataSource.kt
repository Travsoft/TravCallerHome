package com.cartravelsdailerapp.Repositorys.DAO

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.provider.CallLog
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.models.CallHistory
import java.util.*


private const val s = "SDK_INT"

class CallLogsDataSource(private val contentResolver: ContentResolver, val context: Context) {
    val callHistoryList = mutableListOf<CallHistory>()
    private lateinit var callHistory: CallHistory
    var dir: String? = null
    private lateinit var cursor: Cursor

    /*CallLog.Calls.DATE + " DESC"*/
    fun fetchCallLogsList(): List<CallHistory> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                cursor = contentResolver.query(
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
                    null, null
                )!!

            } catch (ex: Exception) {
                Log.d("ex-->", ex.message.toString())
            }

        } else {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.TYPE,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.PHONE_ACCOUNT_ID
                ),
                null,
                null, null
            )!!

        }
        while (cursor.moveToNext()) {
            when (cursor.getColumnIndex(CallLog.Calls.TYPE).let { cursor.getString(it).toInt() }) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            cursor.getColumnIndex(CallLog.Calls.NUMBER)
                ?.let { cursor.getString(it) }?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                        CallHistory(
                            number = it,
                            name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                                ?: null,
                            type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt(),
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

                    } else {
                        CallHistory(
                            number = it,
                            name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                                ?: null,
                            type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt(),
                            date = Date(
                                cursor.getLong(
                                    cursor.getColumnIndex(
                                        CallLog.Calls.DATE
                                    )
                                )
                            ).toString(),
                            duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                                .toString(),
                            subscriberId = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                ?: "",
                            calType = dir.toString(),
                            photouri = "",
                            SimName = getSimCardInfosBySubscriptionId(
                                cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                    ?: "0",
                            )?.displayName?.toString() ?: "",
                        )
                    }

                }?.let {
                    callHistoryList.add(
                        it
                    )
                }
        }
        cursor.close()
        return callHistoryList
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun fetchCallLogSingle(number: String): CallHistory {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1")
                    .build(),
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
                CallLog.Calls.NUMBER + " = ?",
                arrayOf(number), null
            )!!
        } else {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1")
                    .build(), arrayOf(
                    CallLog.Calls.TYPE,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.PHONE_ACCOUNT_ID
                ),
                null,
                null, CallLog.Calls.DATE + " DESC"
            )!!

        }
        if(!cursor.isClosed) {
            while (cursor.moveToNext()) {
                when (cursor.getColumnIndex(CallLog.Calls.TYPE)
                    .let { cursor.getString(it).toInt() }) {
                    CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                    CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                    CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
                }
                cursor.getColumnIndex(CallLog.Calls.NUMBER)
                    .let { cursor.getString(it) }?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val simpDate = SimpleDateFormat(PrefUtils.DataFormate)
                            callHistory = CallHistory(
                                number = it,
                                name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                                    ?: null,
                                type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                    .toInt(),
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

                        } else {
                            callHistory = CallHistory(
                                number = it,
                                name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                                    ?: null,
                                type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                    .toInt(),
                                date =
                                Date(
                                    cursor.getLong(
                                        cursor.getColumnIndex(
                                            CallLog.Calls.DATE
                                        )
                                    )
                                ).toString(),
                                duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                                    .toString(),
                                subscriberId = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                    ?: "",
                                calType = dir.toString(),
                                photouri = "",
                                SimName = getSimCardInfosBySubscriptionId(
                                    cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                                        ?: "0",
                                )?.displayName?.toString() ?: "",
                            )

                        }

                    }
            }
        }
        cursor.close()

        return callHistory

    }

/*
    fun readContacts(): List<Contact> {
        val data = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone._ID
        )

        Log.i("readContacts", "Reading Contacts")
        val listOfContact = ArrayList<Contact>()
        val contentResolver = context.contentResolver
        val nameCursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        if (nameCursor!!.moveToFirst()) {
            do {
                val id: String =
                    nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.Contacts._ID))
                val columnContactName =
                    nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val name = nameCursor.getString(columnContactName) ?: ""
                var number = ""
                var photo: Bitmap? = null
                var photoUri: Uri? = null
                if (nameCursor.getInt(nameCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val numberCursor: Cursor? = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (numberCursor!!.moveToNext()) {
                        number =
                            numberCursor.getString(numberCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                    numberCursor.close()

                    val phoneContactID =
                        nameCursor.getLong(nameCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                    val contactUri: Uri = ContentUris.withAppendedId(
                        ContactsContract.Contacts.CONTENT_URI,
                        phoneContactID
                    )
                    photoUri = Uri.withAppendedPath(
                        contactUri,
                        ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                    )

                    val photoCursor: Cursor? = contentResolver.query(
                        photoUri, arrayOf(ContactsContract.Contacts.Photo.PHOTO),
                        null, null, null
                    )

                    if (photoCursor!!.moveToFirst()) {
                        val data = photoCursor.getBlob(0)
                        if (data != null) {
                            photo = BitmapFactory.decodeStream(ByteArrayInputStream(data))
                        }
                    }
                    photoCursor.close()
                }
                listOfContact.add(Contact(name, number, photoUri.toString(), isFavourites = false))
            } while (nameCursor.moveToNext())
        }
        nameCursor.close()
        Log.d("321--->", listOfContact.count().toString())
        return listOfContact
    }
*/

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

}