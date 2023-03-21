package com.example.easyjob.user

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
import com.example.easyjob.databinding.FragmentFavouriteJobBinding
import com.example.easyjob.employer.JobData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavouriteJobFragment : Fragment() {

    private var _binding : FragmentFavouriteJobBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbRef : DatabaseReference
    private lateinit var favouriteJobRecyclerView: RecyclerView
    private lateinit var favouriteJobDataArrayList: ArrayList<JobData>



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as UserHome).showBottomNavigationView()
        favouriteJobRecyclerView = binding.myJobAppliedList
        favouriteJobRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        favouriteJobRecyclerView.setHasFixedSize(true)
        favouriteJobDataArrayList = arrayListOf<JobData>()

        checkFavouriteJobExist()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFavouriteJobBinding.inflate(inflater, container, false)

        //action bar
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController, appBarConfiguration)

        //hide bottom navigation bar
        (activity as UserHome).hideBottomNavigationView()

        return binding.root
    }


    private fun checkFavouriteJobExist() {
        val usersNode = "Users"
        val favoriteNode = "Favorites"
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().getReference("$usersNode/$currentUserID/$favoriteNode")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val jobIds = mutableListOf<String>()
                for(jobIdSnapshot in snapshot.children){
                    val jobId = jobIdSnapshot.key
                    jobId?.let { jobIds.add(it) }
                }

                val jobsRef = FirebaseDatabase.getInstance().getReference("Jobs")
                for (jobId in jobIds) {
                    val employerIds = mutableListOf<String>()
                    val jobQuery = jobsRef.orderByKey().equalTo(jobId)
                    jobQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for(jobSnapshot in snapshot.children){
                                    val jobData = jobSnapshot.getValue(JobData::class.java)
                                    favouriteJobDataArrayList.add(jobData!!)
                                    val employerId = jobData.employerId
                                    employerId?.let {employerIds.add(it)}
                                }
                            }
                            else{
                                binding.etNoFavouriteJobShow.visibility = View.VISIBLE
                            }
                            favouriteJobRecyclerView.adapter = UserFavouriteJobAdapter(favouriteJobDataArrayList)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                            Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }
}