package com.example.easyjob.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.E

class UserDataViewModel: ViewModel() {

    private lateinit var dbref: DatabaseReference
    private val _userLiveData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userLiveData

    fun getData(userId: String) {
        dbref = FirebaseDatabase.getInstance().getReference("Users")

        dbref.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userData = snapshot.getValue(UserData::class.java)
                    _userLiveData.value = userData!!
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}