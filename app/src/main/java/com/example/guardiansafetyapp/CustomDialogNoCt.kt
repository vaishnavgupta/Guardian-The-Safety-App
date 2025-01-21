package com.example.guardiansafetyapp

import android.app.Dialog
import android.content.Context
import android.widget.Button
import androidx.navigation.NavController

class CustomDialogNoCt(private val navController: NavController,context: Context):Dialog(context) {
    private val btNoCt:Button
    init {
        setContentView(R.layout.custom_ncont_dialog)
        btNoCt=findViewById(R.id.btnAddCtDg)
        btNoCt.setOnClickListener {
            dismiss()
            navController.navigate(R.id.action_dashboardFragment_to_addedContactFragment)
        }
    }

}