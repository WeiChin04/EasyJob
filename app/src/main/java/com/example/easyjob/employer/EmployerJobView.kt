package com.example.easyjob.employer

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEmployerJobViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class EmployerJobView : Fragment() {

    private var _binding: FragmentEmployerJobViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var uid: String
    private lateinit var auth: FirebaseAuth
    private lateinit var jobRecyclerView: RecyclerView
    private lateinit var jobArrayList: ArrayList<JobData>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as EmployerHome).showBottomNavigationView()
        jobRecyclerView = binding.myJobList
        jobRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobRecyclerView.setHasFixedSize(true)
        jobArrayList = arrayListOf<JobData>()
        getJobData()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun getJobData() {

        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Jobs")

        dbRef.orderByChild("employerId").equalTo(currentUserID)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (jobSnapshot in snapshot.children) {
                            val job = jobSnapshot.getValue(JobData::class.java)
                            jobArrayList.add(job!!)
                        }
                        jobRecyclerView.adapter = JobAdapter(jobArrayList)
                    } else {
                        binding.etNoJobShow.visibility = View.VISIBLE
                        binding.myJobList.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                    Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT)
                        .show()
                }
            })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerJobViewBinding.inflate(inflater, container, false)
        val postJobAction = "actionPostJob"
        binding.btnAddJob.setOnClickListener {
            val bundle = Bundle().apply {
                putString("actionPostJob", postJobAction)
            }
            it.findNavController()
                .navigate(R.id.action_jobView_to_employerJobForm, bundle)
        }
        return binding.root


    }
}