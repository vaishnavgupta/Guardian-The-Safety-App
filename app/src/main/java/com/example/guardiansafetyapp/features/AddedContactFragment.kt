package com.example.guardiansafetyapp.features

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.adapters.AddedContactsRVAdapter
import com.example.guardiansafetyapp.models.ContactNew
import com.example.guardiansafetyapp.viewmodel.GuardianAppViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddedContactFragment : Fragment() {
    private lateinit var viewModel:GuardianAppViewModel
    private lateinit var noContactTV: TextView
    private lateinit var contactsRV:RecyclerView
    private lateinit var contactsRVAdapter: AddedContactsRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_added_contact, container, false)
        viewModel=ViewModelProvider(this).get(GuardianAppViewModel::class.java)
        noContactTV=view.findViewById(R.id.nocontactTV)
        contactsRV=view.findViewById(R.id.contactAddedListRv)
        contactsRVAdapter=AddedContactsRVAdapter()
        contactsRV.layoutManager=LinearLayoutManager(requireContext())

        view.findViewById<FloatingActionButton>(R.id.fabAddContacts).setOnClickListener {
            view.findNavController().navigate(R.id.action_addedContactFragment_to_addContactsFragment)
        }

        viewModel.getAddContacts().observe(viewLifecycleOwner, Observer {
            if(it.isEmpty()){
                noContactTV.visibility=View.VISIBLE
                contactsRV.visibility=View.GONE
            }
            else{
                noContactTV.visibility=View.GONE
                contactsRV.visibility=View.VISIBLE
                contactsRVAdapter.setAddedCtList(it)
                contactsRV.adapter=contactsRVAdapter
            }
        })

        return view
    }


}