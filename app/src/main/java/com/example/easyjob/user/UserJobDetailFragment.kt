package com.example.easyjob.user

import android.annotation.SuppressLint
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
import com.example.easyjob.databinding.FragmentUserJobDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class UserJobDetailFragment : Fragment() {

    private  var _binding: FragmentUserJobDetailBinding? =null
    private val binding get() = _binding!!
    private var currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentUserJobDetailBinding.inflate(inflater,container,false)
        database = FirebaseDatabase.getInstance()

        //hide nav bar
        (activity as UserHome).hideBottomNavigationView()

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

        binding.btnApplyJob.setOnClickListener{
            checkApplication()
        }

        return binding.root
    }

    private fun checkApplication(){

        dbRef = database.getReference("Applications")
        val jobId = arguments?.getString("job_id")
        val appliedJobId = "$currentUser-$jobId"

        val ref = FirebaseDatabase.getInstance().getReference("Applications").child(appliedJobId)
        val status = "Pending"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val appliedAt = sdf.format(Date())

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(requireContext(), "You have applied this job before", Toast.LENGTH_SHORT).show()
                } else {
                    val newApplication = UserApplicationData(
                        jobId,
                        currentUser,
                        status,
                        appliedAt
                    )
                    dbRef.child(appliedJobId).setValue(newApplication)
                    Toast.makeText(requireContext(), "Applied Successfully!!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        val title = arguments?.getString("job_title")
        val salary = arguments?.getString("job_salary")
        val location = arguments?.getString("job_location")
        val jobType = arguments?.getString("job_type")
        val jobWorkingHourStar = arguments?.getString("job_working_hour_start")
        val jobWorkingHourEnd = arguments?.getString("job_working_hour_end")
        val requirement = arguments?.getString("job_requirement")
        val responsibilities = arguments?.getString("job_responsibilities")

        binding.tvJobDetailTitle.text = title
        binding.tvJobDetailSalary.text = "RM $salary"
        binding.tvJobDetailsWorkPlace.text = location
        binding.tvJobDetailsJobType.text = jobType
        binding.tvJobDetailsWorkingHourStart.text = jobWorkingHourStar
        binding.tvJobDetailsWorkingHourEnd.text = jobWorkingHourEnd
        binding.tvJobDetailRequirementContent.text = requirement
        binding.tvJobDetailResContent.text = responsibilities
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}