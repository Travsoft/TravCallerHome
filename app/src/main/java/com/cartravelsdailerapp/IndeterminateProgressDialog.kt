package com.cartravelsdailerapp

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class IndeterminateProgressDialog(context: Context) : AlertDialog(context) {
    private val messageTextView: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_loading_dialog, null)
        messageTextView = view.findViewById(R.id.message)
        setView(view)
    }

    override fun setMessage(message: CharSequence?) {
        this.messageTextView.text = message.toString()
    }

}