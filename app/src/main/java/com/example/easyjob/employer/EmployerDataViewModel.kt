package com.example.easyjob.employer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.easyjob.employer.EmployerData
import com.google.firebase.database.*

class EmployerDataViewModel: ViewModel() {
    private lateinit var dbref: DatabaseReference
    private val _employerLiveData = MutableLiveData<EmployerData>()
    val employerData: LiveData<EmployerData> = _employerLiveData

    fun getData(employerId: String) {
        dbref = FirebaseDatabase.getInstance().getReference("Employers")

        dbref.child(employerId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val employerData = snapshot.getValue(EmployerData::class.java)
                    _employerLiveData.value = employerData!!
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}