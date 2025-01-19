package com.example.guardiansafetyapp.features

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.models.ContactNew
import com.example.guardiansafetyapp.sms.SMSUtils
import com.example.guardiansafetyapp.viewmodel.GuardianAppViewModel

class EmergencySMSFragment : Fragment() {
    private val SMS_PERMISSION_CODE = 101
    private lateinit var viewModel: GuardianAppViewModel
    private lateinit var contactsList:List<ContactNew>
    private var userName=""
    private var phoneNum=""
    private lateinit var dialog:ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_emergency_s_m_s, container, false)
        requestSmsPermission()
        dialog=ProgressDialog(requireContext())
        viewModel=ViewModelProvider(this).get(GuardianAppViewModel::class.java)
        contactsList= listOf<ContactNew>()
        viewModel.getAddContacts().observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                Toast.makeText(requireContext(), "No Contacts Added!", Toast.LENGTH_SHORT).show()
            }
            else{
                contactsList=it
            }
        })
        viewModel.nme.observe(viewLifecycleOwner, Observer {
            userName=it
        })
        viewModel.phNum.observe(viewLifecycleOwner, Observer {
            phoneNum=it
        })
        view.findViewById<TextView>(R.id.button).setOnClickListener {
            dialog.setMessage("Sending SMS...")
            dialog.setCancelable(false)
            dialog.show()
            val msg=view.findViewById<EditText>(R.id.msgEditText).text.toString()
            val finalMsg= "$msg\nFrom: $userName ( $phoneNum )\n\nSent via Guardian - The Safety App"
            val totalContacts = contactsList.size
            var sentCount = 0
            if(contactsList.isEmpty()){
                Toast.makeText(requireContext(), "No Contacts Added!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }
            for(contact in contactsList){
                val phoneNum="+91${contact.friendPhone}"
                SMSUtils.sendSMS(requireContext(),phoneNum,finalMsg)
                sentCount++
                dialog.setMessage("Sending SMS...( $sentCount / $totalContacts )")
            }
            dialog.dismiss()
            view.findViewById<EditText>(R.id.msgEditText).setText("")
            Toast.makeText(requireContext(), "All messages sent successfully!", Toast.LENGTH_SHORT).show()
        }


        return view
    }
    fun requestSmsPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS),SMS_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==SMS_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(requireContext(), "SMS permission granted.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "SMS permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}