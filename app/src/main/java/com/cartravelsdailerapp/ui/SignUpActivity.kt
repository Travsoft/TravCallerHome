package com.cartravelsdailerapp.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.PrefUtils.KeyEmail
import com.cartravelsdailerapp.PrefUtils.KeyPhoneNumber
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivitySignUpBinding
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import com.cartravelsdailerapp.models.UserRegisterRequest
import com.cartravelsdailerapp.viewmodels.LoginAndSignUpViewModel
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.snackbar.Snackbar
import java.io.FileDescriptor
import java.io.IOException

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    var image_uri: Uri? = null
    lateinit var vm: LoginAndSignUpViewModel
    var profileFile: String = ""
    lateinit var mProgressDialog: ProgressDialog

    private var galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.getResultCode() === RESULT_OK) {
                image_uri = it.data?.data
                val inputImage = uriToBitmap(image_uri!!)
                val rotated = rotateBitmap(inputImage!!)
                binding.imgProfile.setImageBitmap(rotated)
                profileFile = image_uri.toString()
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
                profileFile = inputImage.toString()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mProgressDialog = ProgressDialog(this@SignUpActivity)
        mProgressDialog.setTitle("Loading")
        val myViewModelFactory =
            MyViewModelFactory(this@SignUpActivity.application)

        vm = ViewModelProvider(
            this@SignUpActivity,
            myViewModelFactory
        )[LoginAndSignUpViewModel::class.java]

        supportActionBar?.title = "Sign Up"
        val d = intent.extras
        val email = d?.getString(KeyEmail)
        val phoneNumber = d?.getString(KeyPhoneNumber)
        binding.etEmail.setText(email)
        binding.etSim1.setText(phoneNumber)
        binding.btSignup.setOnClickListener {
            /*val intent = Intent(
                this,
                Login2Activity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)*/
            val name = binding.etName.text.toString()
            val jobTitle = binding.etJobtitle.text.toString()
            val companyName = binding.etCompanyName.text.toString()
            val sim1Number = binding.etSim1.text.toString()
            val sim2Number = binding.etSim2.text.toString()
            val email = binding.etEmail.text.toString()
            val state = binding.etState.text.toString()
            val pinCode = binding.etPinCode.text.toString()
            val cityName = binding.etPinCityname.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmpassword = binding.etConfimpassword.text.toString()
            if (name.isEmpty()) {
                initErrorMessage(R.string.entername)
            } else if (jobTitle.isEmpty()) {
                initErrorMessage(R.string.enter_job_title)
            } else if (companyName.isEmpty()) {
                initErrorMessage(R.string.enter_company)
            } else if (sim1Number.isEmpty()) {
                initErrorMessage(R.string.enter_sim_one)
            } else if (sim2Number.isEmpty()) {
                initErrorMessage(R.string.enter_sim_two)

            } else if (email.isEmpty()) {
                initErrorMessage(R.string.enter_email)
            } else if (state.isEmpty()) {
                initErrorMessage(R.string.enter_state)
            } else if (pinCode.isEmpty()) {
                initErrorMessage(R.string.enter_pin_code)
            } else if (cityName.isEmpty()) {
                initErrorMessage(R.string.enter_city_name)
            } else if (password.isEmpty()) {
                initErrorMessage(R.string.enter_password)

            } else if (confirmpassword.isEmpty()) {
                initErrorMessage(R.string.enter_confirm_password)
            } else if (password != confirmpassword) {
                initErrorMessage(R.string.should_confirm_password_same)
            } else {
                mProgressDialog = ProgressDialog(this)
                mProgressDialog.setTitle("Loading")
                mProgressDialog.show()
                val request = UserRegisterRequest(
                    profileFile,
                    name,
                    email,
                    pinCode,
                    sim1Number,
                    password,
                    state,
                    "",
                    cityName,
                    webLink = "",
                    sim2Number,
                    jobTitle,
                    companyName
                )
                vm.userRegister(request)
            }


        }
        vm.userData.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    //  showLoading()
                    mProgressDialog.show()

                }

                is BaseResponse.Success -> {
                    // stopLoading()
                    mProgressDialog.dismiss()
                   if (it.data?.data?.first()?.id?.isEmpty() != true){

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
                    mProgressDialog.dismiss()
                }
                else -> {
                    //stopLoading()
                    mProgressDialog.dismiss()
                }
            }

        }

        binding.imgProfile.setOnClickListener {
            chooseImage()
        }
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

    private fun initErrorMessage(msg: Int) {
        Snackbar.make(
            binding.root,
            msg,
            Snackbar.LENGTH_SHORT
        ).show()

    }
}