package com.example.guardiansafetyapp.registration

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.guardiansafetyapp.LottieProgressDialog
import com.example.guardiansafetyapp.R
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class RegisterDetailFragment : Fragment() {
    private val calendar=Calendar.getInstance()
    private lateinit var dateInpText:TextInputEditText
    private lateinit var phoneInput:TextInputEditText
    private lateinit var chkBox:CheckBox
    private lateinit var progDialSignIn: LottieProgressDialog
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_register_detail, container, false)

        //Recieving data from RegisterFragment
        val n=arguments?.getString("name")
        val e=arguments?.getString("email")
        val p=arguments?.getString("password")

        firestore=FirebaseFirestore.getInstance()
        auth=FirebaseAuth.getInstance()
        dateInpText=view.findViewById(R.id.inputDOB)
        phoneInput=view.findViewById(R.id.inputPhoneNum)
        chkBox=view.findViewById(R.id.checkBox)
        progDialSignIn=LottieProgressDialog(requireContext())

        //calender dialog
        dateInpText.setOnClickListener {
            val datePickerDialog=DatePickerDialog(requireContext(),{DatePicker,year:Int,month:Int,dayOfMonth:Int ->
                val selectedDate=Calendar.getInstance()
                selectedDate.set(year,month,dayOfMonth)
                val dateFormat=SimpleDateFormat("dd/MM/yyyy",Locale.getDefault())
                val formatedDate=dateFormat.format(selectedDate.time)
                dateInpText.setText(formatedDate)
            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }



        view.findViewById<TextView>(R.id.finalRegisterTv).setOnClickListener {
            val phone=phoneInput.text.toString()
            val dob=dateInpText.text.toString()
            if(phone.isEmpty() || dob.isEmpty()){
                Toast.makeText(requireContext(),"Please fill all the fields",Toast.LENGTH_SHORT).show()
            }
            else if(phone.length!=10){
                Toast.makeText(requireContext(), "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
            }
            else if(!chkBox.isChecked){
                Toast.makeText(requireContext(),"Accept Our Privacy Policies to Continue",Toast.LENGTH_SHORT).show()
            }
            else{
                registerUser(n,e,p,phone,dob)
            }
        }
        
        return view
    }

    private fun registerUser(n: String?, e: String?, p: String?, phone: String, dob: String) {
        progDialSignIn.show()
        progDialSignIn.updateMsg("Creating your Profile\nPlease Wait...")

        if (e != null && p != null) {
            auth.createUserWithEmailAndPassword(e,p).addOnCompleteListener {
                if(it.isSuccessful){
                    //storing things in firestore
                    val user=auth.currentUser
                    val hashMap= hashMapOf(
                        "uid" to user?.uid,
                        "name" to n,
                        "email" to e,
                        "phone" to phone,
                        "dob" to dob
                    )
                    user?.let {
                        firestore.collection("Users").document(user.uid).set(hashMap).addOnSuccessListener {
                            progDialSignIn.dismiss()
                            Toast.makeText(requireContext(), "Account Created Successfully", Toast.LENGTH_SHORT).show()
                            view?.findNavController()?.navigate(R.id.action_registerDetailFragment_to_loginFragment)
                        }.addOnFailureListener { error->
                            progDialSignIn.dismiss()
                            Toast.makeText(requireContext(), "Failed to save data: ${error.message}", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                else{
                    Toast.makeText(requireContext(),"Failed to Create Account${it.exception?.message}",Toast.LENGTH_SHORT).show()
                    progDialSignIn.dismiss()
                }
            }
        }


    }

}