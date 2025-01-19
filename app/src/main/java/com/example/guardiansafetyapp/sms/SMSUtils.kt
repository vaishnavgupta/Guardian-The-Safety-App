package com.example.guardiansafetyapp.sms

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

object SMSUtils {

    private const val sendSMSPermission=android.Manifest.permission.SEND_SMS

    fun sendSMS(context:Context,phoneNumber:String,message:String){
        if(hasSMSPermission(context)==false){
            Log.d("SMSUtils", "Permission not granted for sending SMS.")
            Toast.makeText(context, "SMS permission not granted.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val smsManager=SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber,null,message,null,null)
            Log.d("SMSUtils", "SMS sent successfully.")
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "SMS sent successfully to $phoneNumber!", Toast.LENGTH_SHORT).show()
            }
        }
        catch (e:Exception){
            Log.e("SMSUtils", "Failed to send SMS: ${e.message}", e)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hasSMSPermission(context: Context): Boolean {
        val checkPermiRes=ContextCompat.checkSelfPermission(context, sendSMSPermission)
        return checkPermiRes==PermissionChecker.PERMISSION_GRANTED
    }
}