package com.cartravelsdailerapp.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityProfileBinding
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import java.io.FileDescriptor
import java.io.IOException


class ProfileActivity : AppCompatActivity() {
    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.getResultCode() === RESULT_OK) {
                image_uri = it.data?.data
                val inputImage = uriToBitmap(image_uri!!)
                val rotated = rotateBitmap(inputImage!!)
                binding.imgProfile.setImageBitmap(rotated)
            }
        }

    //TODO capture the image using camera and display it
    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode === RESULT_OK) {
                val inputImage = uriToBitmap(image_uri!!)
                val rotated = rotateBitmap(inputImage!!)
                binding.imgProfile.setImageBitmap(rotated)
            }
        }


    lateinit var binding: ActivityProfileBinding
    var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        intClickListener()
    }

    private fun intClickListener() {
        binding.imgProfile.setOnClickListener {
            chooseImage()
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgEdit.setOnClickListener {
            binding.layoutName.isEnabled = true
            binding.layourCompanyName.isEnabled = true
            binding.layourEmail.isEnabled = true
            binding.layourJobtitle.isEnabled = true
            binding.layourState.isEnabled = true
            binding.layourJobtitle.isEnabled = true
            binding.layourPinCode.isEnabled = true
            binding.layourCityname.isEnabled = true
            binding.imgSave.isVisible = true
            binding.imgEdit.isVisible = false
        }
        binding.imgSave.setOnClickListener {
            binding.layoutName.isEnabled = false
            binding.layourCompanyName.isEnabled = false
            binding.layourEmail.isEnabled = false
            binding.layourJobtitle.isEnabled = false
            binding.layourState.isEnabled = false
            binding.layourJobtitle.isEnabled = false
            binding.layourPinCode.isEnabled = false
            binding.layourCityname.isEnabled = false
            binding.imgSave.isVisible = false
            binding.imgEdit.isVisible = true
        }
        binding.etPost.doOnTextChanged { text, start, before, count ->
            binding.btPostButton.isVisible = text?.length!! > 0
            binding.imGallery.isVisible = text?.length!! == 0
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        cameraActivityResultLauncher.launch(cameraIntent)


    }

    private fun openGallery() {
        val galleryIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(galleryIntent)

    }

    //TODO takes URI of the image and returns bitmap
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("Range")
    fun rotateBitmap(input: Bitmap): Bitmap? {
        val orientationColumn =
            arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cur: Cursor? = contentResolver.query(image_uri!!, orientationColumn, null, null, null)
        var orientation = -1
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]))
        }
        Log.d("tryOrientation", orientation.toString() + "")
        val rotationMatrix = Matrix()
        rotationMatrix.setRotate(orientation.toFloat())
        return Bitmap.createBitmap(input, 0, 0, input.width, input.height, rotationMatrix, true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun chooseImage() {
        val builder: AlertDialog.Builder? = this.let { AlertDialog.Builder(it) }
        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupLayoutBinding.inflate(layoutInflater)
        binding.imageWhatsapp.setImageResource(R.drawable.ic_gallery)
        binding.imageWhatsappBussiness.setImageResource(R.drawable.ic_camera)
        binding.txtTitle.text = resources.getString(R.string.choose_profile_picture)
        binding.txtWhatsapp.text = resources.getString(R.string.gallery)
        binding.txtWhatsappBussiness.text = resources.getString(R.string.camera)
        builder?.setView(binding.root)

        val dialog: AlertDialog = builder!!.create()
        dialog.show()
        binding.imageClose.setOnClickListener {
            dialog.dismiss()
        }
        binding.imageWhatsappBussiness.setOnClickListener {
            openCamera()
            dialog.dismiss()

        }
        binding.imageWhatsapp.setOnClickListener {
            openGallery()
            dialog.dismiss()

        }
    }

}