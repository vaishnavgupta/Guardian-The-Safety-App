package com.example.guardiansafetyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.guardiansafetyapp.Utils
import com.example.guardiansafetyapp.models.ContactNew
import com.google.firebase.firestore.FirebaseFirestore

class ContactsRepo {
    private val firestore=FirebaseFirestore.getInstance()
    private val userUID=Utils.getCurrUserId()

    fun getAddedContacts():LiveData<List<ContactNew>>{
        val contList=MutableLiveData<List<ContactNew>>()
        firestore.collection("Contacts").document(userUID).collection("eachContact").addSnapshotListener { value, error ->
            if(error!=null){
                return@addSnapshotListener
            }
            val listOfContacts= mutableListOf<ContactNew>()
            if(value!!.documents!=null){
                value.documents.forEach { docx->
                    val ctModel=docx.toObject(ContactNew::class.java)
                    ctModel!!.let {
                        listOfContacts.add(it)
                    }
                }
            }
            contList.value=listOfContacts
        }
        return contList
    }
}