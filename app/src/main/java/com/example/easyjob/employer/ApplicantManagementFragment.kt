package com.example.easyjob.employer

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.databinding.FragmentApplicantManagementBinding
import com.example.easyjob.user.UserApplicationData
import com.example.easyjob.user.UserData
import com.example.easyjob.user.UserJobAppliedAdapter
import com.google.firebase.database.*

class ApplicantManagementFragment : Fragment() {

    private var _binding: FragmentApplicantManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbRef : DatabaseReference
    private lateinit var applicantRecyclerView: RecyclerView
    private lateinit var applicantArrayList: ArrayList<UserData>
    private lateinit var appliedDataList: ArrayList<UserApplicationData>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as EmployerHome).showBottomNavigationView()
        applicantRecyclerView = binding.ManageApplicantList
        applicantRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        applicantRecyclerView.setHasFixedSize(true)
        applicantArrayList = arrayListOf<UserData>()
        appliedDataList = arrayListOf<UserApplicationData>()
        getApplicantList()
        //hide nav bar
        (activity as EmployerHome).hideBottomNavigationView()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicantManagementBinding.inflate(inflater,container,false)

        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController, appBarConfiguration)

        return binding.root
    }

    private fun getApplicantList() {

        val applicantIds = mutableListOf<String>()
        val jobId = arguments?.getString("jobId")
        val pending = "Pending"
        val approved = "Approved"
        dbRef = FirebaseDatabase.getInstance().getReference("Applications")
        dbRef.orderByChild("jobId").equalTo(jobId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (applicantSnapshot in snapshot.children) {
                        val applyDetail = applicantSnapshot.getValue(UserApplicationData::class.java)
                        appliedDataList.add(applyDetail!!)
                        val applicantId = applyDetail.applicantId
                        applicantId?.let { applicantIds.add(it) }
                    }
                    applicantArrayList.clear() // clear applicant list
                    val applicantsRef = FirebaseDatabase.getInstance().getReference("Users")
                    for (userId in applicantIds) {
                        val jobQuery = applicantsRef.orderByKey().equalTo(userId)
                        jobQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    for(userSnapshot in snapshot.children){
                                        val applicantData = userSnapshot.getValue(UserData::class.java)
                                        applicantArrayList.add(applicantData!!)
                                    }
                                }
                                applicantRecyclerView.adapter = ApplicantAdapter(appliedDataList,applicantArrayList)
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }else{
                    binding.etNoApplicantShow.visibility = View.VISIBLE
                    binding.ManageApplicantList.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}