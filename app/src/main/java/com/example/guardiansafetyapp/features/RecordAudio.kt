package com.example.guardiansafetyapp.features

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.viewmodel.GuardianAppViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

const val auddioReqCode=200
class RecordAudio : AppCompatActivity(),AudioTimer.onTimerTickListener {
    private var permissionGranted=false
    private var permiss= arrayOf(Manifest.permission.RECORD_AUDIO)
    private lateinit var recBtn:ImageButton
    private lateinit var deleteBtn:ImageButton
    private lateinit var listBtn:ImageButton
    private lateinit var doneBtn:ImageButton
    private lateinit var recorder:MediaRecorder
    private var isRecording=false
    private var isPaused=false
    private var dirName=""
    private var fileName=""
    private lateinit var bottomSheetBehaviour:BottomSheetBehavior<LinearLayout>
    private lateinit var timer:AudioTimer
    private lateinit var timerTV:TextView
    private lateinit var vibrator: Vibrator
    private lateinit var btmShLay:LinearLayout
    private lateinit var btmShBG:View
    private lateinit var btmFileNameInp:TextInputEditText
    private lateinit var cnclBtn:MaterialButton
    private lateinit var shrBtn:MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio)

        permissionGranted=ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED
        if(permissionGranted==false) ActivityCompat.requestPermissions(this,permiss,
            auddioReqCode)

        btmFileNameInp=findViewById(R.id.fileNameInput)
        cnclBtn=findViewById(R.id.btnCancel)
        shrBtn=findViewById(R.id.btnShare)
        btmShLay=findViewById(R.id.bottomSheetLayout)
        btmShBG=findViewById(R.id.bottomSheetBg)
        bottomSheetBehaviour=BottomSheetBehavior.from(btmShLay)
        timerTV=findViewById(R.id.tvTimer)
        timer=AudioTimer(this)
        recBtn=findViewById(R.id.recordBtn)
        deleteBtn=findViewById(R.id.deleteBtn)
        listBtn=findViewById(R.id.ListBtn)
        doneBtn=findViewById(R.id.doneBtn)
        vibrator= getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


        bottomSheetBehaviour.peekHeight=0
        bottomSheetBehaviour.state=BottomSheetBehavior.STATE_COLLAPSED

        recBtn.setOnClickListener {
            when{
                isPaused->resumeRecording()
                isRecording->pauseRecording()
                else->startRecording()
            }
            vibrator.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE))
        }

        listBtn.setOnClickListener {
            //todo
        }
        doneBtn.setOnClickListener {
            stopRecording()
            bottomSheetBehaviour.state=BottomSheetBehavior.STATE_EXPANDED
            btmShBG.visibility=View.VISIBLE
            btmFileNameInp.setText(fileName)
        }
        //bottomSheet Btn Working
        cnclBtn.setOnClickListener {
            File("$dirName$fileName.mp3").delete()
            dismissBtmSheet()
        }
        shrBtn.setOnClickListener {
            dismissBtmSheet()
            shareFile()
        }
        btmShBG.setOnClickListener {
            File("$dirName$fileName.mp3").delete()
            dismissBtmSheet()
        }


        deleteBtn.setOnClickListener {
            stopRecording()
            File("$dirName$fileName.mp3").delete()
        }
        deleteBtn.isClickable=false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== auddioReqCode){
            permissionGranted=(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Audio permission granted.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Audio permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun shareFile(){
        val audioFile=File("$dirName$fileName.mp3")
        if(!audioFile.exists()){
            Toast.makeText(this, "File does not exist.", Toast.LENGTH_SHORT).show()
            return
        }

        //Getting URI from FileProvider
        val fileUri=FileProvider.getUriForFile(this,"${packageName}.fileprovider",audioFile)
        //Intent to share
        val shrIntent=Intent(Intent.ACTION_SEND).apply {
            type="audio/*"
            putExtra(Intent.EXTRA_STREAM,fileUri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(shrIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dismissBtmSheet(){
        btmShBG.visibility=View.GONE
        bottomSheetBehaviour.state=BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun pauseRecording(){
        isPaused=true
        recorder.pause()
        recBtn.setImageResource(R.drawable.ripple_audio)
        timer.pauseTimer()
    }

    private fun resumeRecording(){
        isPaused=false
        recorder.resume()
        recBtn.setImageResource(R.drawable.round_pause_24)
        timer.startTimer()
    }

    private fun startRecording(){
        if(!permissionGranted){
            ActivityCompat.requestPermissions(this,permiss,
                auddioReqCode)
            return
        }
        //setting recorder
        recorder=MediaRecorder()
        dirName="${externalCacheDir?.absolutePath}/"
        var simpleDateFormat=SimpleDateFormat("yyyy.MM.dd_hh.mm.ss")
        var date=simpleDateFormat.format(Date())
        fileName="audio_GuardianSafety_${date}"
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirName$fileName.mp3")

            try {
                prepare()
            }catch (e:IOException){

            }
            start()
        }
        recBtn.setImageResource(R.drawable.round_pause_24)
        isRecording=true
        isPaused=false
        timer.startTimer()
        deleteBtn.isClickable=true
        deleteBtn.setImageResource(R.drawable.ic_delete)
        listBtn.visibility=View.GONE
        doneBtn.visibility=View.VISIBLE
    }

    private fun stopRecording(){
        timer.stopTimer()
        recorder.apply {
            stop()
            release()
        }
        isRecording=false
        isPaused=false
        listBtn.visibility=View.VISIBLE
        doneBtn.visibility=View.GONE
        deleteBtn.isClickable=false
        deleteBtn.setImageResource(R.drawable.ic_delete_disabled)
        recBtn.setImageResource(R.drawable.ripple_audio)
        timerTV.text="00:00.00"
    }

    override fun onTimerTick(duration: String) {
        timerTV.text = duration
    }
}

