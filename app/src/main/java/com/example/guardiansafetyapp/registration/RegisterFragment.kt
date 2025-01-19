package com.example.guardiansafetyapp.registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.guardiansafetyapp.R
import com.google.android.material.textfield.TextInputEditText



class RegisterFragment : Fragment() {

    private lateinit var nameInput:TextInputEditText
    private lateinit var emailInput:TextInputEditText
    private lateinit var passInput:TextInputEditText
    private lateinit var registerDetailFragment: RegisterDetailFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_register, container, false)

        nameInput=view.findViewById(R.id.inputName)
        emailInput=view.findViewById(R.id.inputEmail)
        passInput=view.findViewById(R.id.inputPassword)
        registerDetailFragment=RegisterDetailFragment()
        val bundle=Bundle()

        view.findViewById<TextView>(R.id.signInTv).setOnClickListener {
            view.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        view.findViewById<TextView>(R.id.nextTvRegister).setOnClickListener {
            val name=nameInput.text.toString()
            val email=emailInput.text.toString()
            val pass=passInput.text.toString()

            if(name.isEmpty() ||  email.isEmpty() || pass.isEmpty()){
                Toast.makeText(requireContext(),"Please fill all the fields",Toast.LENGTH_SHORT).show()
            }
            else{
                if(pass.length<6) Toast.makeText(requireContext(),"Password must have atleast 6 characters",Toast.LENGTH_SHORT).show()
                else{
                    bundle.putString("name",name)
                    bundle.putString("email",email)
                    bundle.putString("password",pass)
                    //registerDetailFragment.arguments=bundle
                    view.findNavController().navigate(R.id.action_registerFragment_to_registerDetailFragment,bundle)
                }
            }

        }

        return view
    }

}