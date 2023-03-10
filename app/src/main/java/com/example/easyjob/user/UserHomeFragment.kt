package com.example.easyjob.user

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.databinding.FragmentUserHomeBinding
import com.example.easyjob.employer.EmployerHome
import com.example.easyjob.employer.JobAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var uid: String
    private lateinit var user: UserData
    private lateinit var userDataViewModel: UserDataViewModel
    private lateinit var jobRecyclerView: RecyclerView
    private lateinit var jobArrayList: ArrayList<UserJobData>
    private lateinit var userJobAdapter: UserJobAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as UserHome).showBottomNavigationView()
        jobRecyclerView = binding.myJobList
        jobRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobRecyclerView.setHasFixedSize(true)
        jobArrayList = arrayListOf<UserJobData>()
        getJobData()

        dbRef = FirebaseDatabase.getInstance().getReference("Jobs")

        userJobAdapter = UserJobAdapter(jobArrayList)
        jobRecyclerView.adapter = userJobAdapter

//        searchJob()

        binding.searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                if (filterList(newText)!=null)
                {
                    Log.d("SearchJob",filterList(newText).toString())
                }else{
                    Log.d("SearchJob","Fail to Get Data!!")
                }
                return true
            }

        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        //get user data form view model
        userDataViewModel = ViewModelProvider(requireActivity())[UserDataViewModel::class.java]

        userDataViewModel.getData(auth.currentUser!!.uid)


        return binding.root
    }


    private fun getJobData() {

        dbRef = FirebaseDatabase.getInstance().getReference("Jobs")

        val status = "Available"
        dbRef.orderByChild("jobStatus").equalTo(status)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (jobSnapshot in snapshot.children) {
                            val job = jobSnapshot.getValue(UserJobData::class.java)
                            jobArrayList.add(job!!)
                        }
                        jobRecyclerView.adapter = UserJobAdapter(jobArrayList)
                    } else {
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

    private fun filterList(query: String?) {
        if (query != null) {
            val filteredList = ArrayList<UserJobData>()
            for (i in jobArrayList) {
                if (i.jobTitle!!.lowercase(Locale.ROOT).contains(query)) {
                    filteredList.add(i)
                }
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(requireContext(), "No Job Found", Toast.LENGTH_SHORT).show()
            } else {
                userJobAdapter.setFilteredList(filteredList)
                jobRecyclerView.adapter = userJobAdapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}