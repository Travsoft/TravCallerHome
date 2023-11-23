package com.cartravelsdailerapp.ui

import android.Manifest
import android.Manifest.permission.CALL_PHONE
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartravelsdailerapp.MainActivity
import com.cartravelsdailerapp.PrefUtils.EnteredNumber
import com.cartravelsdailerapp.PrefUtils.TelegramAppPackage
import com.cartravelsdailerapp.PrefUtils.WhatsAppPackage
import com.cartravelsdailerapp.R
import com.cartravelsdailerapp.broadcastreceivers.CustomPhoneStateReceiver
import com.cartravelsdailerapp.dialerstates.CallManager
import com.cartravelsdailerapp.dialerstates.GsmCall
import com.cartravelsdailerapp.models.Contact
import com.cartravelsdailerapp.service.MyConnectionService
import com.cartravelsdailerapp.ui.adapters.ContactsAdapter
import com.cartravelsdailerapp.utils.isPackageInstalled
import com.cartravelsdailerapp.viewmodels.MainActivityViewModel
import com.cartravelsdailerapp.viewmodels.MyViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.Disposables
import kotlinx.coroutines.*
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext


class Dialer : AppCompatActivity(), CoroutineScope, View.OnClickListener {
    private val LOG_TAG = "DialerActivity"
    lateinit var time: String

    lateinit var dialerView: LinearLayout
    lateinit var tel: TelecomManager
    lateinit var fab: FloatingActionButton
    lateinit var clear_img: ImageView
    private var updatesDisposable = Disposables.empty()
    private var timerDisposable = Disposables.empty()
    lateinit var edtInput: EditText
    lateinit var menut: Menu
    var bundle = Bundle()
    lateinit var img_sim: ImageView
    lateinit var txt_sim_type: TextView
    lateinit var subList: List<SubscriptionInfo>
    lateinit var img_whatsapp: ImageView
    lateinit var img_telegram: ImageView
    lateinit var recy_list_contacts: RecyclerView
    lateinit var subscriptionManager: SubscriptionManager
    lateinit var listContacts: ArrayList<Contact>
    lateinit var contactsAdapter: ContactsAdapter
    private lateinit var job: Job
    lateinit var cancel_button: ImageView
    lateinit var add_user: ImageView
    lateinit var contactIntent: Intent
    lateinit var launcherContact: ActivityResultLauncher<Intent>
    lateinit var viewModel: MainActivityViewModel
    lateinit var receiver: CustomPhoneStateReceiver
    var simName: String = "SIM1"
    private val onResult: (String, String?, Uri?) -> Unit = { phone, name, photoUri ->
        launch {
            viewModel.getNewCallLogsHistory(phone, simName)
            val intent = Intent(
                this@Dialer,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(EnteredNumber, edtInput.toString())
            startActivity(intent)

        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        job = Job()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialer)
        edtInput = findViewById(R.id.edtInput)
        dialerView = findViewById(R.id.dialerView)
        clear_img = findViewById(R.id.clear_img)
        img_sim = findViewById(R.id.img_sim)
        fab = findViewById(R.id.fab)
        txt_sim_type = findViewById(R.id.txt_sim_type)
        img_whatsapp = findViewById(R.id.img_whatsapp)
        img_telegram = findViewById(R.id.img_telegram)
        recy_list_contacts = findViewById(R.id.recy_list_contacts)
        cancel_button = findViewById(R.id.cancel_button)
        add_user = findViewById(R.id.add_user)
        supportActionBar?.hide()
        img_whatsapp.setOnClickListener(this)
        img_telegram.setOnClickListener(this)
        cancel_button.setOnClickListener(this)
        add_user.setOnClickListener(this)

        val myViewModelFactory =
            MyViewModelFactory(this.application)

        viewModel = ViewModelProvider(
            this,
            myViewModelFactory
        )[MainActivityViewModel::class.java]

        launcherContact =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                }
            }
        fab.setOnClickListener { view ->
            if (dialerView.visibility == View.GONE) {
                dialerView.visibility = View.VISIBLE
                fab.setImageResource(R.drawable.ic_call)
            } else {

                if (edtInput.length() > 0) {
                    callTheEnteredNumber()
                    receiver = CustomPhoneStateReceiver(onResult, edtInput.toString())
                    ContextCompat.registerReceiver(
                        this,
                        receiver,
                        IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED),
                        ContextCompat.RECEIVER_EXPORTED
                    )
                }
            }

            Log.d("MyInCallService", "inFab")
        }


        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(TelecomManager::class.java).defaultDialerPackage != packageName
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        ) {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                .let(::startActivity)
        }

        if (Build.VERSION.SDK_INT > 22) {
            subscriptionManager =
                this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        } else {
            val tManager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            subList = subscriptionManager.activeSubscriptionInfoList.toList()

            if (!subList.isEmpty()) {
                subList[0].simSlotIndex
                val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
                val list = telecomManager.callCapablePhoneAccounts

                txt_sim_type.text = subList[0].displayName.toString().replace("SIM", "")
                bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, list[0])

                img_sim.setOnClickListener {
                    if (txt_sim_type.text.equals("1")) {
                        txt_sim_type.text = subList[1].displayName.toString().replace("SIM", "")
                        bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, list[1])
                        simName = subList[1].displayName.toString()
                    } else {
                        txt_sim_type.text = subList[0].displayName.toString().replace("SIM", "")
                        bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, list[0])
                        simName = subList[0].displayName.toString()
                    }
                }
            }
        }
/*
        launch {
            freezePleaseIAmDoingHeavyWork()
        }
*/
    }

    override fun onResume() {
        updatesDisposable = CallManager.updates()
            .doOnEach { Log.i(LOG_TAG, "updated call: $it") }
            .doOnError { throwable -> Log.e(LOG_TAG, "Error processing call", throwable) }
            .subscribe { updateView(it) }
        super.onResume()
    }

    fun updateView(gsmCall: GsmCall) {
        if (supportActionBar != null) {
            when (gsmCall.status) {
                GsmCall.Status.DISCONNECTED -> {
                    //red
                    supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#375f91")))

                }
                GsmCall.Status.DIALING -> {
                    supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#5f6368")))
                }
                GsmCall.Status.RINGING -> {
                    //down green
                    supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#3d8c39")))
                }
                GsmCall.Status.ACTIVE -> {
                    //indigo
                    supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#5c00d2")))

                }
                GsmCall.Status.UNKNOWN -> {
                    //grey
                    supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#375f91")))

                }
                GsmCall.Status.CONNECTING -> {
                    //grey and blue
                    supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#375f91")))

                }
                //
            }


        }
        Log.d("DialerActivity", gsmCall.status.toString())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun createPhoneAccountHandle(context: Context, accountName: String): PhoneAccountHandle {
        return PhoneAccountHandle(
            ComponentName(context, MyConnectionService::class.java),
            accountName
        )
    }

    private fun callTheEnteredNumber() {
        callThisPerson()
    }


    fun buttonClickEvent(v: View) {
        when (v.id) {
            R.id.dialpad_1_holder -> {
                edtInput.append("1")
            }
            R.id.dialpad_2_holder -> {
                edtInput.append("2")
            }
            R.id.dialpad_3_holder -> {
                edtInput.append("3")
            }
            R.id.dialpad_4_holder -> {
                edtInput.append("4")
            }
            R.id.dialpad_5_holder -> {
                edtInput.append("5")
            }
            R.id.dialpad_6_holder -> {
                edtInput.append("6")
            }
            R.id.dialpad_7_holder -> {
                edtInput.append("7")
            }
            R.id.dialpad_8_holder -> {
                edtInput.append("8")
            }
            R.id.dialpad_9_holder -> {
                edtInput.append("9")
            }
            R.id.dialpad_0_holder -> {
                edtInput.append("0")
            }
            R.id.dialpad_asterisk_holder -> {
                edtInput.append("*")
            }
            R.id.dialpad_hashtag_holder -> {
                edtInput.append("#")
            }

        }

        clear_img.setOnClickListener {
            if (edtInput.length() > 0) {
                edtInput.setText(
                    edtInput.text.substring(
                        0,
                        edtInput.text.toString().length - 1
                    )
                )
                edtInput.setSelection(edtInput.getText().length)//position cursor at the end of the line
            }
        }
    }


    private fun callThisPerson() {
        if (ContextCompat.checkSelfPermission(
                this,
                CALL_PHONE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    CALL_PHONE
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(CALL_PHONE),
                    42
                )
            }
        } else {
            // Permission has already been granted
            callPhone()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 42) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay!
                callPhone()
            } else {
                // permission denied, boo! Disable the
                // functionality
            }
            return
        }
    }

    private fun validateUSSD(str: String): Boolean {
        val sPattern = Pattern.compile("^\\*[0-9\\*#]*[0-9]+[0-9\\*#]*#$")

        var value = sPattern.matcher(str).matches()

        return value
    }

    private fun callPhone() {
        var handller = Handler()
        handller.postDelayed({
            /* Create an Intent that will start the Menu-Activity. */
            Log.d("ussddata", "inside handler")
            // finish()
        }, 3000)

        //  var phonecall = createPhoneAccountHandle(this, MyConnectionService.EXTRA_PHONE_ACCOUNT)
        Log.d("MyInCallService", "callPhone start")

        var s = edtInput.text.toString()
        var callstring: String


        var uri: Uri = Uri.fromParts("tel", edtInput.text.toString(), null)
        //Connection.PROPERTY_SELF_MANAGED
        /* bundle.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
     bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phonecall)*/

        Log.d("MyInCallService", "callPhone after 3 lines ")

        tel = getSystemService(Context.TELECOM_SERVICE) as TelecomManager


        if (ActivityCompat.checkSelfPermission(
                this,
                CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            tel.placeCall(uri, bundle)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            909 -> {
                Log.d("ussddata", data.toString())
            }
        }
    }

    override fun onClick(v: View?) {
        val number = edtInput.text.toString()
        when (v?.id) {
            R.id.img_telegram -> {

                if (this.isPackageInstalled(this, TelegramAppPackage)) {
                    if (number.length >= 10) {
                        openTelegramAppByNumber(number)
                    }
                } else {
                    Snackbar.make(
                        findViewById(R.id.container),
                        "Telegram App Not Found",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            R.id.img_whatsapp -> {
                if (number.length >= 10) {
                    if (this.isPackageInstalled(this, WhatsAppPackage)) {
                        openWhatsAppByNumber(number)
                    } else {
                        Snackbar.make(
                            findViewById(R.id.container),
                            "Whats App Not Found",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
            R.id.cancel_button -> {
                onBackPressed()
            }
            R.id.add_user -> {
                contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
                contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
                contactIntent
                    // .putExtra(ContactsContract.Intents.Insert.NAME, name)
                    .putExtra(ContactsContract.Intents.Insert.PHONE, number)
                //.putExtra(ContactsContract.Intents.Insert.EMAIL, email)
                launcherContact.launch(contactIntent)
            }

/*
            R.id.call_list_rv -> {
                Log.d("call_lis_rv", "clicked")
                dialerView.visibility = View.GONE
            }
*/
        }
    }

    private fun openWhatsAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber))
        intent.setPackage("com.whatsapp")
        startActivity(intent)

    }

    private fun openTelegramAppByNumber(toNumber: String) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("tg://openmessage?user_id=" + toNumber))
        intent.setPackage("org.telegram.messenger");
        startActivity(intent)
    }

    private fun openGpay() {
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            /* .appendQueryParameter("pa", "your-merchant-vpa@xxx")
             .appendQueryParameter("pn", "your-merchant-name")
             .appendQueryParameter("mc", "your-merchant-code")
             .appendQueryParameter("tr", "your-transaction-ref-id")
             .appendQueryParameter("tn", "your-transaction-note")
             .appendQueryParameter("am", "your-order-amount")
             .appendQueryParameter("cu", "INR")
             .appendQueryParameter("url", "your-transaction-url")*/
            .build()
        val intent = Intent(Intent.ACTION_VIEW)
        //  intent.data = uri
        intent.setPackage("com.phonepe.app")
        // this.startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE)
        startActivity(intent)
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    suspend fun freezePleaseIAmDoingHeavyWork() { // function B in image
/*
        val list = withContext(Dispatchers.Default) {
            //pretend this is a big network call
            CallLogsDataSource(
                application.contentResolver,
                this@Dialer
            ).readContacts()
        }
*/
        //  contactsAdapter = ContactsAdapter(this)
        val layoutInflater = LinearLayoutManager(this)
        recy_list_contacts.layoutManager = layoutInflater
        recy_list_contacts.adapter = contactsAdapter
        contactsAdapter.notifyDataSetChanged()

    }

}