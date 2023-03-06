package com.example.easyjob.employer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.databinding.FragmentJobViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class JobView : Fragment() {

    private  var _binding: FragmentJobViewBinding? =null
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

        dbRef = FirebaseDatabase.getInstance().getReference("Jobs")

        dbRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){

                    for(jobSnapshot in snapshot.children){
                        val job = jobSnapshot.getValue(JobData::class.java)
                        jobArrayList.add(job!!)
                    }

                    jobRecyclerView.adapter = JobAdapter(jobArrayList)
                }

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJobViewBinding.inflate(inflater,container,false)
        return binding.root
    }


}