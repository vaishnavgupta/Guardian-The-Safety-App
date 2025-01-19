package com.example.guardiansafetyapp.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.models.ContactInfo
import com.example.guardiansafetyapp.viewmodel.GuardianAppViewModel

class ContactListRVAdapter(var context: Context):RecyclerView.Adapter<ContactListRVAdapter.MyViewHolder>() {
    private var listOfContacts= listOf<ContactInfo>()
    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val ctName=itemView.findViewById<TextView>(R.id.contactItemName)
        val ctPhone=itemView.findViewById<TextView>(R.id.contactItemPhone)
        val ctAdd=itemView.findViewById<Button>(R.id.buttonAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.each_contact_item,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return listOfContacts.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cuurContact=listOfContacts[position]
        holder.ctName.text=cuurContact.name
        holder.ctPhone.text=cuurContact.number
        holder.ctAdd.setOnClickListener {
            showDialog(cuurContact.name,cuurContact.number)
        }
    }

    private fun showDialog(name: String, number: String) {
        val dialog=Dialog(context)
        val view=LayoutInflater.from(context).inflate(R.layout.add_contact_dialog,null)
        dialog.setContentView(view)
        val contctName=view.findViewById<EditText>(R.id.eTName)
        val conctPhone=view.findViewById<EditText>(R.id.eTPhone)
        val conctRelt=view.findViewById<EditText>(R.id.eTRelation)
        val addBtn=view.findViewById<Button>(R.id.btnAdd)
        val cancelBtn=view.findViewById<Button>(R.id.btnCancel)
        contctName.setText(name)
        conctPhone.setText(number)
        cancelBtn.setOnClickListener { dialog.dismiss() }
        val viewModel=ViewModelProvider(context as ViewModelStoreOwner).get(GuardianAppViewModel::class.java)
        addBtn.setOnClickListener {
            val nme=contctName.text.toString()
            val phn=conctPhone.text.toString()
            val rln=conctRelt.text.toString()
            if(nme.isEmpty() || phn.isEmpty() || rln.isEmpty()){
                Toast.makeText(context,"Please fill all the fields",Toast.LENGTH_SHORT).show()
            }
            else{
                viewModel.addContact(nme,phn,rln)
                contctName.text.clear()
                conctPhone.text.clear()
                conctRelt.text.clear()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    fun setContactList(list:List<ContactInfo>){
        this.listOfContacts=list
        notifyDataSetChanged()
    }
}


