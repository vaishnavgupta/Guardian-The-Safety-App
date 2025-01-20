package com.example.guardiansafetyapp.features

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.models.ContactNew
import com.example.guardiansafetyapp.sms.SMSUtils
import com.example.guardiansafetyapp.viewmodel.GuardianAppViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class TrackMeFragment : Fragment(),OnMapReadyCallback {
    private val accessLocationCode=105
    private val SMS_PERMISSION_CODE = 101
    private lateinit var fusedLocationClient:FusedLocationProviderClient
    private var latitude=28.535517
    private var longitude=77.391029
    private  var googleMap:GoogleMap?=null
    private var userName=""
    private var phoneNum=""
    private lateinit var currAddEt:EditText
    private lateinit var shareLocBtn:TextView
    private lateinit var viewModel:GuardianAppViewModel
    private lateinit var list:List<ContactNew>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_track_me, container, false)
        requestLocationPermission()
        //requestSmsPermission()
        viewModel=ViewModelProvider(this).get(GuardianAppViewModel::class.java)
        list= listOf()
        viewModel.getAddContacts().observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                Toast.makeText(requireContext(), "No Contacts Added!", Toast.LENGTH_SHORT).show()
            }
            else{
                list=it
            }
        })
        viewModel.nme.observe(viewLifecycleOwner, Observer {
            userName=it
        })
        viewModel.phNum.observe(viewLifecycleOwner, Observer {
            phoneNum=it
        })
        currAddEt=view.findViewById(R.id.eTCurrentLoc)
        shareLocBtn=view.findViewById(R.id.shareCurrLocBtn)
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(requireActivity())
        shareLocBtn.setOnClickListener {
            checkAndSendSMS()
        }
        val mapFragment=childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    private fun checkAndSendSMS() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
        } else {
            shareCurrentLocation() // Proceed to share location if permission is granted
        }
    }


    private fun getAddressFromLatLng(lat:Double,lng:Double):String{
        //Using Geocoder Android Class
        val geocoder=Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses=geocoder.getFromLocation(lat,lng,1)
            if(addresses!=null && addresses.isNotEmpty()){
                val adress=addresses[0]
                val fullAddress=adress.getAddressLine(0)
                currAddEt.setText(fullAddress)
                return fullAddress
            }else{
                Toast.makeText(requireContext(),"Unable to retrieve Address",Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            Toast.makeText(requireContext(),"Error Address ${e.message}",Toast.LENGTH_LONG).show()
        }
        return "No Address Found"
    }

    private fun requestLocationPermission() {
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),accessLocationCode)
        }
        else if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS),SMS_PERMISSION_CODE)
        }
    }

    private fun getCurrentLocationOfUser() {
        //checking permissions again
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),accessLocationCode)
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { loc->
            if(loc!=null){
                latitude=loc.latitude
                longitude=loc.longitude
                updateMapLocation()
                getAddressFromLatLng(latitude,longitude)       //show address in ET
            }else {
                Toast.makeText(requireContext(), "Unable to retrieve location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to get Current Location ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap?) {

        //move camera to default loc initially
        val latLong= com.google.android.gms.maps.model.LatLng(latitude, longitude)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong,15f))
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.map_style))
        map?.uiSettings?.isZoomControlsEnabled=true
        map?.uiSettings?.isCompassEnabled=true
        map?.uiSettings?.isMapToolbarEnabled=false
        map?.uiSettings?.isScrollGesturesEnabledDuringRotateOrZoom=false
        val markerOptions=MarkerOptions()
        markerOptions.position(latLong)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.location_pin_1_svgrepo_com)))
        markerOptions.title("Location")
        markerOptions.flat(true)
        map?.addMarker(markerOptions)

        googleMap=map
        //Attempt to get current loc
        getCurrentLocationOfUser()
    }

    private fun getBitmapFromDrawable(i: Int): android.graphics.Bitmap? {
        var bitmap:android.graphics.Bitmap?=null
        val drawable= ResourcesCompat.getDrawable(resources,i,null)
        if(drawable!=null){
            bitmap=android.graphics.Bitmap.createBitmap(150,150,android.graphics.Bitmap.Config.ARGB_8888)
            val canas=Canvas(bitmap)
            drawable.setBounds(0,0,canas.width,canas.height)
            drawable.draw(canas)
        }
        return bitmap
    }

    private fun updateMapLocation() {
        val latLong = com.google.android.gms.maps.model.LatLng(latitude, longitude)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
        googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.map_style))
        googleMap?.uiSettings?.isZoomControlsEnabled=true
        googleMap?.uiSettings?.isCompassEnabled=true
        googleMap?.uiSettings?.isMapToolbarEnabled=false
        googleMap?.uiSettings?.isScrollGesturesEnabledDuringRotateOrZoom=false
        val markerOptions=MarkerOptions()
        markerOptions.title("Location")
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.location_pin_1_svgrepo_com)))
        markerOptions.flat(true)
        markerOptions.position(latLong)
        googleMap?.addMarker(markerOptions)
    }

    private fun shareCurrentLocation(){
        if(latitude!=0.0 && longitude!=0.0){
            val address=getAddressFromLatLng(latitude,longitude)
            val locationLink="https://www.google.com/maps?q=$latitude,$longitude"
            val finalMsg="Location: ${address}\nLocation Link: ${locationLink}\nFrom: $userName ( $phoneNum )\n\nSent via Guardian - The Safety App"
            if(list.isEmpty()){
                Toast.makeText(requireContext(), "No Contacts Added!", Toast.LENGTH_SHORT).show()
                return
            }
            val smsManager= SmsManager.getDefault()
            val parts=smsManager.divideMessage(finalMsg)
            for(contact in list){
                val phoneNum="+91${contact.friendPhone}"
                smsManager.sendMultipartTextMessage(phoneNum,null,parts,null,null)
                Log.d("EmergencySms","SMS sent to ${contact.friendName}")
            }
            Toast.makeText(requireContext(), "All messages sent successfully!", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(requireContext(), "Location not available yet.", Toast.LENGTH_SHORT).show()
        }
    }


    //@Deprecated("Deprecated in Java")
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
        else if(requestCode==SMS_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "SMS permission granted.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "SMS permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}