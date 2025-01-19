package com.example.guardiansafetyapp.registration

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.guardiansafetyapp.HomeActivity
import com.example.guardiansafetyapp.LottieProgressDialog
import com.example.guardiansafetyapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginFragment : Fragment() {
    private lateinit var emailInput:TextInputEditText
    private lateinit var passInput:TextInputEditText
    private lateinit var auth:FirebaseAuth
    private lateinit var progressDialog:LottieProgressDialog
    private lateinit var bgProgress:View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_login, container, false)
        bgProgress=view.findViewById(R.id.translView)
        emailInput=view.findViewById(R.id.inputEmailLogin)
        passInput=view.findViewById(R.id.inputPasswordLogin)
        auth=FirebaseAuth.getInstance()
        progressDialog=LottieProgressDialog(requireContext())

        view.findViewById<TextView>(R.id.signUpTv).setOnClickListener {
            view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        view.findViewById<TextView>(R.id.signInTVBtn).setOnClickListener {
            val e=emailInput.text.toString()
            val p=passInput.text.toString()
            if(e.isEmpty()||p.isEmpty()){
                Toast.makeText(requireContext(),"Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
            else{
                loginUser(e,p)
            }
        }

        return view
    }

    private fun loginUser(e: String, p: String) {
        bgProgress.visibility=View.VISIBLE
        progressDialog.show()
        progressDialog.updateMsg("Checking Your Details")
        auth.signInWithEmailAndPassword(e,p).addOnCompleteListener {
            if(it.isSuccessful){
                bgProgress.visibility=View.GONE
                progressDialog.dismiss()
                val intent= Intent(activity,HomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            else{
                bgProgress.visibility=View.GONE
                progressDialog.dismiss()
                Toast.makeText(requireContext(),"Invalid Credentials\n Please Enter Correct Credentials",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            bgProgress.visibility=View.GONE
            progressDialog.dismiss()
            when(it){
                is FirebaseAuthInvalidCredentialsException ->{
                    Toast.makeText(requireContext(),"Invalid Credentials\n Please Enter Correct Credentials",Toast.LENGTH_SHORT).show()
                }
                else->{
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}