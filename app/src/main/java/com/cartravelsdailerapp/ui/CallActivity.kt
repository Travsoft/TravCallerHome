package com.cartravelsdailerapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cartravelsdailerapp.R

class CallActivity : AppCompatActivity() {
    private lateinit var recorder: MediaRecorder
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
       /* requestPermissions()

        val makeCall : Button = findViewById(R.id.makeCall)
        makeCall.setOnClickListener{
            if(hasPermissions()){
                val intent = Intent(Intent.ACTION_DIAL)
                startActivity(intent)
            }
        }*/
    }
    private fun hasPermissions() : Boolean{
        val recordAudioPermission = Manifest.permission.RECORD_AUDIO
        val writeToFilePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val phoneState = Manifest.permission.READ_PHONE_STATE
        return (ContextCompat.checkSelfPermission(this, recordAudioPermission) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, writeToFilePermission) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, phoneState) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions(){
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE)
        ActivityCompat.requestPermissions(this, permissions, 123)
    }
}