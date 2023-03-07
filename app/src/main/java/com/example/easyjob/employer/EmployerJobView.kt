package com.example.easyjob.employer

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.databinding.FragmentEmployerJobViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class EmployerJobView : Fragment() {

    private  var _binding: FragmentEmployerJobViewBinding? =null
    private val binding get() = _binding!!
    private lateinit var dbRef : DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var uid: String
    private lateinit var auth: FirebaseAuth
    private lateinit var jobRecyclerView: RecyclerView
    private lateinit var jobArrayList: ArrayList<JobData>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as EmployerHome).showBottomNavigationView()
        jobRecyclerView = binding.myJobList
        jobRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobRecyclerView.setHasFixedSize(true)
        jobArrayList = arrayListOf<JobData>()
        getJobData()
    }

    private fun getJobData() {

        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Jobs")

        dbRef.orderByChild("employerId").equalTo(currentUserID).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(jobSnapshot in snapshot.children){
                        val job = jobSnapshot.getValue(JobData::class.java)
                        jobArrayList.add(job!!)
                    }
                    jobRecyclerView.adapter = JobAdapter(jobArrayList)
                }else{
                    binding.etNoJobShow.visibility = View.VISIBLE
                    binding.myJobList.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmployerJobViewBinding.inflate(inflater,container,false)
        return binding.root
    }


}