package com.example.guardiansafetyapp

import com.google.firebase.auth.FirebaseAuth

class Utils {

    companion object {
        private val auth = FirebaseAuth.getInstance()
        private var userId: String = ""

        //using the fun to get user id anywhwre

        fun getCurrUserId(): String {
            if (auth.currentUser != null) {
                userId = auth.currentUser!!.uid
            }
            return userId
        }
    }
}