package com.example.guardiansafetyapp.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guardiansafetyapp.MyApplication
import com.example.guardiansafetyapp.Utils
import com.example.guardiansafetyapp.models.ContactNew
import com.example.guardiansafetyapp.models.Users
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GuardianAppViewModel:ViewModel() {

    val nme=MutableLiveData<String>()
    val phNum=MutableLiveData<String>()
    val contactRepo=ContactsRepo()
    private val firestore=FirebaseFirestore.getInstance()

    init {
        getCurrUserInfo()
    }

    private fun getCurrUserInfo()=viewModelScope.launch(Dispatchers.IO) {
        firestore.collection("Users").document(Utils.getCurrUserId()).addSnapshotListener { value, error ->
            if(value!!.exists() && value!=null){
                val userModel=value.toObject(Users::class.java)
                nme.value=userModel?.name
                phNum.value=userModel?.phone
            }

        }
    }

    // Adding Contacts
    fun addContact(frName:String,frPhone:String,frRelation:String){
        val context=MyApplication.instance.applicationContext        //getting context
        val conctHashMap= hashMapOf(
            "friendName" to frName,
            "friendPhone" to frPhone,
            "friendRelation" to frRelation
        )

        firestore.collection("Contacts").document(Utils.getCurrUserId()).collection("eachContact").document(frPhone).set(conctHashMap).addOnCompleteListener { t->
            if(t.isSuccessful){
                Toast.makeText(context,"Contact Added Successfully!",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exp->
            Toast.makeText(context,"Failed ${exp.message}",Toast.LENGTH_SHORT).show()
        }
    }

    //Getting Added Contacts
    fun getAddContacts():LiveData<List<ContactNew>>{
        return contactRepo.getAddedContacts()
    }



}