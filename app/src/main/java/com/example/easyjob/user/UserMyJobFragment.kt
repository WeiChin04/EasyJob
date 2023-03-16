package com.example.easyjob.user

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.databinding.FragmentUserMyJobBinding
import com.example.easyjob.employer.JobData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserMyJobFragment : Fragment() {

    private var _binding: FragmentUserMyJobBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbRef : DatabaseReference
    private lateinit var jobRecyclerView: RecyclerView
    private lateinit var jobArrayList: ArrayList<UserApplicationData>
    private lateinit var jobDataArrayList: ArrayList<JobData>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as UserHome).showBottomNavigationView()
        jobRecyclerView = binding.myJobAppliedList
        jobRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobRecyclerView.setHasFixedSize(true)
        jobArrayList = arrayListOf<UserApplicationData>()
        jobDataArrayList = arrayListOf<JobData>()
        checkAppliedJobExist()
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserMyJobBinding.inflate(inflater,container,false)
        return binding.root
    }

    private fun checkAppliedJobExist() {

        val jobIds = mutableListOf<String>()
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Applications")
        dbRef.orderByChild("applicantId").equalTo(currentUserID).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(jobSnapshot in snapshot.children){
                        val jobApplied = jobSnapshot.getValue(UserApplicationData::class.java)
                        jobArrayList.add(jobApplied!!)
                        val jobId = jobApplied.jobId
                        jobId?.let { jobIds.add(it) }
                    }
                    jobDataArrayList.clear() // clear jobDataList
                    val jobsRef = FirebaseDatabase.getInstance().getReference("Jobs")
                    for (jobId in jobIds) {
                        val employerIds = mutableListOf<String>()
                        val jobQuery = jobsRef.orderByKey().equalTo(jobId)
                        jobQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for(jobSnapshot in snapshot.children){
                                        val jobData = jobSnapshot.getValue(JobData::class.java)
                                        jobDataArrayList.add(jobData!!)
                                        val employerId = jobData.employerId
                                        employerId?.let {employerIds.add(it)}
                                    }
                                }
                                jobRecyclerView.adapter = UserJobAppliedAdapter(jobArrayList,jobDataArrayList)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }else{
                    binding.etNoAppliedJobShow.visibility = View.VISIBLE
                    binding.myJobAppliedList.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })

    }



}