package com.example.guardiansafetyapp

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView

class LottieProgressDialog(context:Context):Dialog(context) {
    private val lottieView: LottieAnimationView
    private val messageText: TextView

    init {
        setContentView(R.layout.custom_progress_dialog)
        lottieView=findViewById(R.id.lottieView)
        messageText=findViewById(R.id.progress_message)
        setCancelable(false)
    }

    fun updateMsg(msg:String){
        messageText.text=msg
    }
}