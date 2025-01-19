package com.example.guardiansafetyapp.features

import android.os.Handler
import android.os.Looper

class AudioTimer(listener:onTimerTickListener) {

    interface onTimerTickListener{
        fun onTimerTick(duration:String)

    }

    //using a Handler and Runnable
    private var handler=Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var duration=0L
    private var delay=100L
    init {
        runnable= Runnable {
            duration+=delay
            handler.postDelayed(runnable,delay)
            listener.onTimerTick(formatMsToTime())
        }
    }

    fun startTimer(){
        handler.postDelayed(runnable,delay)
    }
    fun pauseTimer(){
        handler.removeCallbacks(runnable)
    }
    fun stopTimer(){
        handler.removeCallbacks(runnable)
        duration=0L
    }
    fun formatMsToTime():String{
        val milliSec=duration%1000
        val seconds=(duration/1000)%60
        val minutes=(duration/(1000*60))%60
        val hours=(duration/(1000*60*60))

        var formated=if(hours>0)
            "%02d:%02d:%02d.%02d".format(hours,minutes,seconds,milliSec/10)
        else
            "%02d:%02d.%02d".format(minutes,seconds,milliSec/10)
        return formated

    }


}