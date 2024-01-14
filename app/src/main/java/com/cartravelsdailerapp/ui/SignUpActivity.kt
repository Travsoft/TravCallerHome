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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.cartravelsdailerapp.BaseResponse
import com.cartravelsdailerapp.PrefUtils
import com.cartravelsdailerapp.PrefUtils.KeyEmail
import com.cartravelsdailerapp.PrefUtils.KeyPhoneNumber
import com.cartravelsdailerapp.PrefUtils.UserEmail
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.databinding.ActivitySignUpBinding
import com.cartravelsdailerapp.databinding.PopupLayoutBinding
import com.cartravelsdailerapp.models.UserRegisterRequest
import com.cartravelsdailerapp.ui.adapters.SpinerArrayListAdapter
import com.cartravelsdailerapp.viewmodels.LoginAndSignUpViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.cvaghela.spinner.searchablespinner.interfaces.OnItemSelectedListener
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.util.ArrayList

class SignUpActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: ActivitySignUpBinding
    var image_uri: Uri? = null
    lateinit var vm: LoginAndSignUpViewModel
    lateinit var mProgressDialog: ProgressDialog
    lateinit var adapter: SpinerArrayListAdapter

     var selectedState: String? = null
    var selectedDistrict: String? = null
    var selectedCity: String? = null
    var selectedMandal: String? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences(PrefUtils.CallTravelsSharedPref, MODE_PRIVATE)
        setContentView(binding.root)
        mProgressDialog = ProgressDialog(this@SignUpActivity)
        mProgressDialog.setTitle("Loading")
        val myViewModelFactory =
            MyViewModelFactory(this@SignUpActivity.application)

        vm = ViewModelProvider(
            this@SignUpActivity,
            myViewModelFactory
        )[LoginAndSignUpViewModel::class.java]

        vm.getStates("")

        supportActionBar?.title = "Sign Up"
        val d = intent.extras
        val email = d?.getString(UserEmail)
        val phoneNumber = d?.getString(KeyPhoneNumber)
        binding.etEmail.setText(email)
        binding.etSim1.setText(phoneNumber)
        binding.btSignup.setOnClickListener {
            val name = binding.etName.text.toString()
            val jobTitle = binding.etJobtitle.text.toString()
            val companyName = binding.etCompanyName.text.toString()
            val sim1Number = binding.etSim1.text.toString()
            val sim2Number = binding.etSim2.text.toString()
            val email = binding.etEmail.text.toString()
            val state = selectedState
            val district = selectedDistrict
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
            } else if (selectedState?.isEmpty() == true) {
                initErrorMessage(R.string.enter_state)
            } else if (selectedDistrict?.isEmpty() == true) {
                initErrorMessage(R.string.enter_district)
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
                    image_uri.toString(),
                    name,
                    email,
                    pinCode,
                    sim1Number,
                    password,
                    state!!,
                    district!!,
                    cityName,
                    webLink = "",
                    sim2Number,
                    jobTitle,
                    companyName
                )
                val file = image_uri?.let { it1 -> getFileFromUri(this, it1) }
                vm.userRegister(request, file)
            }


        }
        initObserve()
        vm.userData.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    //  showLoading()
                    mProgressDialog.show()

                }

                is BaseResponse.Success -> {
                    // stopLoading()
                    mProgressDialog.dismiss()
                    if (it.data?.data?.first()?.id?.isEmpty() != true) {
                        val intent = Intent(
                            this,
                            Login2Activity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        val edit = sharedPreferences.edit()
                        edit.putBoolean(PrefUtils.IsLogin, true)
                        edit.apply()
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

    private fun initObserve() {
        vm.getStatusResp.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    //  showLoading()
                    mProgressDialog.show()

                }

                is BaseResponse.Success -> {
                    // stopLoading()
                    mProgressDialog.dismiss()
                    val states = it.data?.totalStates?.distinct()
                    if (states?.isNotEmpty() == true) {
                        adapter = SpinerArrayListAdapter(this, states as ArrayList<String>, states)
                        binding.spinnerState.setAdapter(adapter)
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
        binding.spinnerState.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(view: View?, position: Int, id: Long) {
                selectedState = adapter.getItem(
                    position
                ).toString()
                Snackbar.make(
                    binding.root,
                    "Selected State $selectedState",
                    Snackbar.LENGTH_SHORT
                ).show()
                mProgressDialog.show()

                selectedState?.let { vm.getDistrict("", it) }
            }

            override fun onNothingSelected() {
                Snackbar.make(binding.root, "Nothing Selected", Snackbar.LENGTH_SHORT)
                    .show()
            }

        })
        vm.getDistrictResp.observe(this) {
            when (it) {
                is BaseResponse.Loading -> {
                    //  showLoading()
                    mProgressDialog.show()

                }

                is BaseResponse.Success -> {
                    // stopLoading()
                    mProgressDialog.dismiss()
                    val districts = it.data?.totalDistricts?.distinct()
                    if (districts?.isNotEmpty() == true) {
                        adapter = SpinerArrayListAdapter(this, districts as ArrayList<String>, districts)
                        binding.spinnerDistrict.setAdapter(adapter)
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
        binding.spinnerDistrict.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(view: View?, position: Int, id: Long) {
                selectedDistrict = adapter.getItem(
                    position
                ).toString()
                Snackbar.make(
                    binding.root,
                    "Item on position $position : $selectedDistrict Selected",
                    Snackbar.LENGTH_SHORT
                ).show()
                mProgressDialog.show()
                selectedDistrict?.let { vm.getCities("", it) }
            }

            override fun onNothingSelected() {
                Snackbar.make(binding.root, "Nothing Selected", Snackbar.LENGTH_SHORT)
                    .show()
            }

        })

        vm.getCityResp.observe(this){
            when (it) {
                is BaseResponse.Loading -> {
                    //  showLoading()
                    mProgressDialog.show()

                }

                is BaseResponse.Success -> {
                    // stopLoading()
                    mProgressDialog.dismiss()
                    val citys = it.data?.totalcities?.distinct()
                    if (citys?.isNotEmpty() == true) {
                        adapter = SpinerArrayListAdapter(this, citys as ArrayList<String>, citys)
                        binding.spinnerCity.setAdapter(adapter)
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
        binding.spinnerCity.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(view: View?, position: Int, id: Long) {
                selectedCity = adapter.getItem(
                    position
                ).toString()
                Snackbar.make(
                    binding.root,
                    "Selected City $selectedCity",
                    Snackbar.LENGTH_SHORT
                ).show()
                mProgressDialog.show()
                selectedCity?.let { vm.getMandal("", it) }
            }

            override fun onNothingSelected() {
                Snackbar.make(binding.root, "Nothing Selected", Snackbar.LENGTH_SHORT)
                    .show()
            }

        })

        vm.getMandalResp.observe(this){
            when (it) {
                is BaseResponse.Loading -> {
                    //  showLoading()
                    mProgressDialog.show()

                }

                is BaseResponse.Success -> {
                    // stopLoading()
                    mProgressDialog.dismiss()
                    val mandels = it.data?.totalMandals?.distinct()
                    if (mandels?.isNotEmpty() == true) {
                        adapter = SpinerArrayListAdapter(this, mandels as ArrayList<String>, mandels)
                        binding.spinnerMandal.setAdapter(adapter)
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
        binding.spinnerMandal.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(view: View?, position: Int, id: Long) {
                selectedMandal = adapter.getItem(
                    position
                ).toString()
                Snackbar.make(
                    binding.root,
                    "Selected Mandal $selectedMandal",
                    Snackbar.LENGTH_SHORT
                ).show()
                mProgressDialog.show()
                selectedMandal?.let { vm.getMandal("", it) }
            }

            override fun onNothingSelected() {
                Snackbar.make(binding.root, "Nothing Selected", Snackbar.LENGTH_SHORT)
                    .show()
            }

        })


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