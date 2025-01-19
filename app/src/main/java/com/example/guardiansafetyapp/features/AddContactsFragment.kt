package com.example.guardiansafetyapp.features

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guardiansafetyapp.adapters.ContactListRVAdapter
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.models.ContactInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddContactsFragment : Fragment() {

    private lateinit var permissionLauncher:ActivityResultLauncher<String>
    private lateinit var contactList:MutableList<ContactInfo>
    private lateinit var addContactBtn:TextView
    private lateinit var contactRV:RecyclerView
    private lateinit var contactAdapter: ContactListRVAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_add_contacts, container, false)
        addContactBtn=view.findViewById(R.id.addContactBtn)
        contactRV=view.findViewById(R.id.contactListRv)
        contactList= mutableListOf<ContactInfo>()
        contactAdapter= ContactListRVAdapter(requireContext())

        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it==false){
                Toast.makeText(requireContext(),"Need Permission to fetch Contact List",Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            else{
                contactRV.visibility=View.VISIBLE
                fetchContacts()
            }
        }

        view.findViewById<FloatingActionButton>(R.id.fabShowContacts).setOnClickListener {
            view.findNavController().navigate(R.id.action_addContactsFragment_to_addedContactFragment)
        }

        addContactBtn.setOnClickListener {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
            addContactBtn.visibility=View.GONE
        }



        return view
    }

    @SuppressLint("Range")
    private fun fetchContacts() {
        var contacts=requireContext().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        if(contacts!=null){
            while (contacts.moveToNext()){
                var name=contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY))
                var number=contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                var contactFinal=ContactInfo(name,number)
                contactList.add(contactFinal)
            }
        }
        contactAdapter.setContactList(contactList)
        contactRV.layoutManager=LinearLayoutManager(requireContext())
        contactRV.adapter=contactAdapter
    }
}