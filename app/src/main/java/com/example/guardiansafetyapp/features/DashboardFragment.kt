package com.example.guardiansafetyapp.features

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.applozic.mobicomkit.api.account.register.RegistrationResponse
import com.example.guardiansafetyapp.LottieProgressDialog
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.models.ContactNew
import com.example.guardiansafetyapp.sms.SMSUtils
import com.example.guardiansafetyapp.viewmodel.GuardianAppViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.kommunicate.Kommunicate
import io.kommunicate.callbacks.KMLoginHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class DashboardFragment : Fragment() {
    private val accessLocationCode=105
    private val SMS_PERMISSION_CODE = 101
    private lateinit var updtContact:LinearLayout
    private lateinit var viewModel: GuardianAppViewModel
    private lateinit var progressDialog: LottieProgressDialog
    private lateinit var bgProgress:View
    private lateinit var contactsList:List<ContactNew>
    private var address=""
    private var userName=""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude=0.0000
    private var longitude=0.0000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_dashboard, container, false)

        bgProgress=view.findViewById(R.id.translView)
        requestLocationSMSPermission()
        viewModel= ViewModelProvider(this).get(GuardianAppViewModel::class.java)
        viewModel.nme.observe(viewLifecycleOwner, Observer {
            userName=it
            view.findViewById<TextView>(R.id.textView9).text="Welcome $it!"
        })
        viewModel.getAddContacts().observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                Toast.makeText(requireContext(), "No Contacts Added!", Toast.LENGTH_SHORT).show()
            }
            else{
                contactsList=it
            }
        })

        fusedLocationClient= LocationServices.getFusedLocationProviderClient(requireActivity())
        getCurrentLocationOfUser()

        Kommunicate.init(requireContext(),"2d094d1e1a1efdd78d19827894ea3e40a")
        updtContact=view.findViewById(R.id.updtContactsBtn)
        updtContact.setOnClickListener {
            view.findNavController().navigate(R.id.action_dashboardFragment_to_addedContactFragment)
        }
        view.findViewById<LinearLayout>(R.id.supportBtn).setOnClickListener {
            progressDialog=LottieProgressDialog(requireContext())
            bgProgress.visibility=View.VISIBLE
            progressDialog.show()
            progressDialog.updateMsg("Starting ShieldMate")
            openChatBot(requireContext())
        }
        view.findViewById<LinearLayout>(R.id.smsBtn).setOnClickListener {
            view.findNavController().navigate(R.id.action_dashboardFragment_to_emergencySMSFragment)
        }
        view.findViewById<LinearLayout>(R.id.trackBtn).setOnClickListener {
            view.findNavController().navigate(R.id.action_dashboardFragment_to_trackMeFragment)
        }
        view.findViewById<LinearLayout>(R.id.micBtn).setOnClickListener {
            val i=Intent(activity,RecordAudio::class.java)
            startActivity(i)
        }
        view.findViewById<LinearLayout>(R.id.sosBtn).setOnClickListener {
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),accessLocationCode)
                return@setOnClickListener
            }
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS),SMS_PERMISSION_CODE)
                return@setOnClickListener
            }
            val builder=AlertDialog.Builder(requireContext())
            builder.setMessage("Are you sure you want to send SOS?")
            builder.setTitle("SOS")
            builder.setCancelable(false)
            builder.setPositiveButton("Yes") {
                    dialog, which -> sosClick(requireContext())
            }
            builder.setNegativeButton("No") {
                    dialog, which -> dialog.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }


        return view
    }

    private fun getCurrentLocationOfUser() {
        //checking permissions again
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),accessLocationCode)
            return
        }
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS),SMS_PERMISSION_CODE)
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { loc->
            if(loc!=null){
                latitude=loc.latitude
                longitude=loc.longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
            }else {
                Log.d("Location", "Last known location is null, requesting location updates.")
                //requestLocationUpdates()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to get Current Location ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressFromLatLng(lat:Double,lng:Double):String{
        //Using Geocoder Android Class
        val geocoder= Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses=geocoder.getFromLocation(lat,lng,1)
            if(addresses!=null && addresses.isNotEmpty()){
                val adress=addresses[0]
                val fullAddress=adress.getAddressLine(0)
                return fullAddress
            }else{
                Toast.makeText(requireContext(),"Unable to retrieve Address",Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            Toast.makeText(requireContext(),"Error Address ${e.message}",Toast.LENGTH_LONG).show()
        }
        return "No Address Found"
    }

    fun sosClick(context: Context){
        progressDialog=LottieProgressDialog(requireContext())
        bgProgress.visibility=View.VISIBLE
        progressDialog.show()

        lifecycleScope.launch {
            progressDialog.updateMsg("Starting SOS Command")
            //fetching loc
            withContext(Dispatchers.IO){
                getCurrentLocationOfUser()
            }

            if (latitude == 0.0000 || longitude == 0.0000) {
                progressDialog.updateMsg("Failed to fetch location.")
                Toast.makeText(context, "Unable to fetch location. Please try again.", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                bgProgress.visibility=View.GONE
                return@launch  // Exit if location is invalid
            }
            progressDialog.updateMsg("Location fetched successfully!")
            address = withContext(Dispatchers.IO) {
                getAddressFromLatLng(latitude, longitude)
            }
            progressDialog.updateMsg("Getting address from location...")
            Toast.makeText(context, "Address: $address", Toast.LENGTH_SHORT).show()
            if (address!="No Address Found" && contactsList.isNotEmpty()) {
                val finalAddress = "SOS Alert!\n$userName is in danger! Need help! \nCurrent Location: $address\nLocation Link: https://maps.google.com/?q=$latitude,$longitude\nSent via Guardian - The Safety App"
                Log.d("add","$finalAddress")
                val smsManager=SmsManager.getDefault()
                val parts=smsManager.divideMessage(finalAddress)
                for (contact in contactsList){
                    val phoneNum="+91${contact.friendPhone}"
                    smsManager.sendMultipartTextMessage(phoneNum,null,parts,null,null)
                    Log.d("SOS_Sms","SMS sent to ${contact.friendName}")
                }
            }
            progressDialog.updateMsg("SMS sent successfully!")
            delay(2000)
            progressDialog.updateMsg("Opening Audio Recorder...")
            delay(1000)
            progressDialog.dismiss()
            bgProgress.visibility = View.GONE
            val intent = Intent(activity, RecordAudio::class.java)
            startActivity(intent)
        }

    }


    fun openChatBot(context:Context){
        Kommunicate.loginAsVisitor(context, object : KMLoginHandler {
            override fun onSuccess(registrationResponse: RegistrationResponse, context: Context) {
                bgProgress.visibility=View.GONE
                progressDialog.dismiss()
                Kommunicate.openConversation(context)
            }


            override fun onFailure(
                registrationResponse: RegistrationResponse,
                exception: Exception
            ) {
                bgProgress.visibility=View.GONE
                progressDialog.dismiss()
                Toast.makeText(context,"Unable to open Chatbot",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun requestLocationSMSPermission() {
        if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION),accessLocationCode)
        }
        else if(ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS),SMS_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==accessLocationCode){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "Location permission granted.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
        if(requestCode==SMS_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "SMS permission granted.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "SMS permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}