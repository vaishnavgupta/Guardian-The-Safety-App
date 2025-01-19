package com.example.guardiansafetyapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.guardiansafetyapp.R
import com.example.guardiansafetyapp.adapters.ContactListRVAdapter.MyViewHolder
import com.example.guardiansafetyapp.models.ContactNew

class AddedContactsRVAdapter():RecyclerView.Adapter<AddedContactsRVAdapter.MyViewHolder>() {
    private var addedCtList= listOf<ContactNew>()
    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val ct_Relation=itemView.findViewById<TextView>(R.id.tVContRelation)
        val ct_Number=itemView.findViewById<TextView>(R.id.tvCntNuber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.each_added_contact,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return addedCtList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currContact=addedCtList[position]
        holder.ct_Relation.text=currContact.friendRelation
        holder.ct_Number.text=currContact.friendPhone
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setAddedCtList(list:List<ContactNew>){
        this.addedCtList=list
        notifyDataSetChanged()
    }

}