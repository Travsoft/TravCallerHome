package com.cartravelsdailerapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.telecom.TelecomManager
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.utils.CarTravelsDialer.ContactName
import com.cartravelsdailerapp.utils.CarTravelsDialer.ContactNumber
import com.cartravelsdailerapp.utils.CarTravelsDialer.ContactUri
import java.io.FileDescriptor

class ProfileActivity : AppCompatActivity() {
    var name: String = ""
    var number: String = ""
    var photoUri: String = ""
    lateinit var txt_name: TextView
    lateinit var img_profile: ImageView
    lateinit var card_call: CardView
    lateinit var card_whatsapp: CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()
        val d = intent.extras
        name = d?.getString(ContactName).toString()
        number = d?.getString(ContactNumber).toString()
        photoUri = d?.getString(ContactUri).toString()
        txt_name = findViewById(R.id.txt_name)
        img_profile = findViewById(R.id.img_profile)
        card_call = findViewById(R.id.card_call)
        card_whatsapp = findViewById(R.id.card_whatsapp)
        txt_name.text = name
        if (!TextUtils.isEmpty(photoUri))
            img_profile.setImageBitmap(getBitmapFromUri(Uri.parse(photoUri)))

        card_call.setOnClickListener {
            val uri = Uri.parse("tel:" + number)
            val telecomManager = this.getSystemService<TelecomManager>()
            val bundle = Bundle()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    telecomManager?.placeCall(uri, bundle)
                }
            }
        }
        card_whatsapp.setOnClickListener {
            openWhatsAppByNumber(number)
        }

    }
    private fun openWhatsAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber))
        intent.setPackage("com.whatsapp")
        startActivity(intent)

    }


    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor? =
            contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }
}