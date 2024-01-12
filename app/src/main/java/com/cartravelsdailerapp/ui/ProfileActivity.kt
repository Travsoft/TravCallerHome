package com.cartravelsdailerapp.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivityProfileBinding
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import com.cartravelsdailerapp.models.UserResponse
import com.cartravelsdailerapp.models.UserUpdateResponse
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.cartravelsdailerapp.viewmodels.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileDescriptor
import java.io.IOException


class ProfileActivity : AppCompatActivity() {
    lateinit var vm: ProfileViewModel
    lateinit var mProgressDialog: ProgressDialog
    lateinit var sharedPreferences: SharedPreferences
    lateinit var token: String

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
        mProgressDialog = ProgressDialog(this@ProfileActivity)
        mProgressDialog.setTitle("Loading")
        val myViewModelFactory =
            MyViewModelFactory(this@ProfileActivity.application)

        vm = ViewModelProvider(
            this@ProfileActivity,
            myViewModelFactory
        )[ProfileViewModel::class.java]
        sharedPreferences = getSharedPreferences(PrefUtils.CallTravelsSharedPref, MODE_PRIVATE)
        mProgressDialog.show()
        token = sharedPreferences.getString(PrefUtils.userToken, "").toString()
        vm.getUserDataByToken("Bearer $token")

        vm.userDataResp.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    mProgressDialog.show()
                }

                is BaseResponse.Success -> {
                    val data = it.data as UserResponse
                    bindData(data)
                    mProgressDialog.dismiss()

                    if (data.statusCode == 200) {
                        Log.d("data--->", data.toString())
                    }

                }

                is BaseResponse.Error -> {
                    // processError(it.msg)
                    it.msg?.let { it1 ->
                        Snackbar.make(
                            binding.root,
                            it1, Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    Log.d(
                        "100->", it
                            .msg.toString()
                    )
                    mProgressDialog.dismiss()
                }
                else -> {
                    //stopLoading()
                    mProgressDialog.dismiss()
                }
            }
        }

        intClickListener()
    }

    private fun bindData(data: UserResponse) {
        binding.etName.setText(data.name)
        binding.etJobtitle.setText(data.jobTitle)
        binding.etCompanyName.setText(data.companyName)
        binding.etSim1.setText(data.phoneNumber.toString())
        binding.etEmail.setText(data.email)
        binding.etAlternateNumber.setText(data.alternateNumber)
        binding.etState.setText(data.state)
        binding.etPinCode.setText(data.pinCode)
        binding.etPinCityname.setText(data.city)
        if (data.profilePicture.isNotEmpty()) {
            Picasso.Builder(this).build().load(data.profilePicture).fit().centerCrop()
                .placeholder(R.drawable.userprofile)
                .error(R.drawable.userprofile)
                .into(binding.imgProfile)
        }
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
            binding.layourJobtitle.isEnabled = true
            binding.layourState.isEnabled = true
            binding.layourJobtitle.isEnabled = true
            binding.layourPinCode.isEnabled = true
            binding.layourCityname.isEnabled = true
            binding.layourAlternateNumber.isEnabled = true
            binding.layourDistrict.isEnabled = true
            binding.layourCity.isEnabled = true
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
            binding.layourAlternateNumber.isEnabled = false
            binding.layourDistrict.isEnabled = false
            binding.layourCity.isEnabled = false
            binding.imgSave.isVisible = false
            binding.imgEdit.isVisible = true
            updateUser()
        }
        binding.etPost.doOnTextChanged { text, start, before, count ->
            binding.btPostButton.isVisible = text?.length!! > 0
            binding.imGallery.isVisible = text.length!! == 0
        }
        vm.userUpdateDataResp.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    mProgressDialog.show()
                }

                is BaseResponse.Success -> {
                    val data = it.data as UserUpdateResponse
                    mProgressDialog.dismiss()

                    if (data.statusCode == 200) {
                        Log.d("data--->", data.toString())
                        vm.getUserDataByToken("Bearer $token")
                    }

                }

                is BaseResponse.Error -> {
                    // processError(it.msg)
                    it.msg?.let { it1 ->
                        Snackbar.make(
                            binding.root,
                            it1, Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    Log.d(
                        "100->", it
                            .msg.toString()
                    )
                    mProgressDialog.dismiss()
                }
                else -> {
                    //stopLoading()
                    mProgressDialog.dismiss()
                }
            }

        }

    }

    private fun updateUser() {
        mProgressDialog.show()
        val name = binding.etName.text.toString()
        val jobTitle = binding.etJobtitle.text.toString()
        val companyName = binding.etCompanyName.text.toString()
        val email = binding.etEmail.text.toString()
        val pinCode = binding.etPinCode.text.toString()
        val phoneNumber = binding.etSim1.text.toString()
        val state = binding.etState.text.toString()
        val district = binding.etDistrict.text.toString()
        val city = binding.etCity.text.toString()
        val alternateNumber = binding.etAlternateNumber.text.toString()


        if (image_uri == null) {
            val requestBody: RequestBody =
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name", name)
                    .addFormDataPart("email", email)
                    .addFormDataPart("pinCode", pinCode)
                    .addFormDataPart("phoneNumber", phoneNumber)
                    .addFormDataPart("state", state)
                    .addFormDataPart("district", district)
                    .addFormDataPart("city", city)
                    .addFormDataPart("alternateNumber", alternateNumber)
                    .addFormDataPart("jobTitle", jobTitle)
                    .addFormDataPart("companyName", companyName)
                    .build()

            vm.updateUserDataByToken("Bearer $token", requestBody)
        } else {
            val profile = image_uri?.let { getFileFromUri(this, it) }
            val requestBody: MultipartBody? =
                profile?.let { RequestBody.create("image/*".toMediaTypeOrNull(), it) }?.let {
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("name", name)
                        .addFormDataPart("email", email)
                        .addFormDataPart("pinCode", pinCode)
                        .addFormDataPart("phoneNumber", phoneNumber)
                        .addFormDataPart("state", state)
                        .addFormDataPart("district", district)
                        .addFormDataPart("city", city)
                        .addFormDataPart("alternateNumber", alternateNumber)
                        .addFormDataPart("jobTitle", jobTitle)
                        .addFormDataPart("companyName", companyName)
                        .addFormDataPart(
                            "profilePicture", profile?.name,
                            it
                        )
                        .build()
                }
            if (token != null) {
                if (requestBody != null) {
                    vm.updateUserDataByToken("Bearer $token", requestBody)
                }
            }
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

    fun getFileFromUri(context: Context, uri: Uri): File? {
        uri ?: return null
        uri.path ?: return null

        var newUriString = uri.toString()
        newUriString = newUriString.replace(
            "content://com.android.providers.downloads.documents/",
            "content://com.android.providers.media.documents/"
        )
        newUriString = newUriString.replace(
            "/msf%3A", "/image%3A"
        )
        val newUri = Uri.parse(newUriString)

        var realPath = String()
        val databaseUri: Uri
        val selection: String?
        val selectionArgs: Array<String>?
        if (newUri.path?.contains("/document/image:") == true) {
            databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            selection = "_id=?"
            selectionArgs = arrayOf(DocumentsContract.getDocumentId(newUri).split(":")[1])
        } else {
            databaseUri = newUri
            selection = null
            selectionArgs = null
        }
        try {
            val column = "_data"
            val projection = arrayOf(column)
            val cursor = context.contentResolver.query(
                databaseUri,
                projection,
                selection,
                selectionArgs,
                null
            )
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    realPath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.i("GetFileUri Exception:", e.message ?: "")
        }
        val path = realPath.ifEmpty {
            when {
                newUri.path?.contains("/document/raw:") == true -> newUri.path?.replace(
                    "/document/raw:",
                    ""
                )
                newUri.path?.contains("/document/primary:") == true -> newUri.path?.replace(
                    "/document/primary:",
                    "/storage/emulated/0/"
                )
                else -> return null
            }
        }
        return if (path.isNullOrEmpty()) null else File(path)
    }
}