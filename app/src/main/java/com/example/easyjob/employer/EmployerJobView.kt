package com.example.easyjob.employer

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        auth = FirebaseAuth.getInstance()
        val currentUserID = auth.currentUser!!.uid
        Log.d("currentUserID", "current user Id: $currentUserID")
        binding.btnAddJob.setOnClickListener {

            dbRef = FirebaseDatabase.getInstance().getReference("Employers")
            dbRef.child(currentUserID)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val employer = snapshot.getValue(EmployerData::class.java)
                        val employerStatus = employer!!.profileStatus

                        if(employerStatus == "Incomplete"){
                            val alertDialog = AlertDialog.Builder(requireContext())
                            alertDialog.setTitle("Confirm")
                            alertDialog.setMessage(getString(R.string.alert_complete_profile))
                            alertDialog.setPositiveButton("Go to profile page") { _, _ ->
                                it.findNavController().navigate(R.id.action_jobView_to_employerInformation)
                            }
                            alertDialog.setNegativeButton("Cancel") { dialog, _ ->
                                dialog.cancel()
                            }
                            alertDialog.show()
                        }else{
                            val bundle = Bundle().apply {
                                putString("actionPostJob", postJobAction)
                            }

                            it.findNavController().navigate(R.id.action_jobView_to_employerJobForm, bundle)

                            Log.d("employer", "employer: $employer")
                            Log.d("status", "status: $employerStatus")

                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, "Failed to read value.", error.toException())
                }

            })


        }
        return binding.root


    }
}