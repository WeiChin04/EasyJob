package com.example.easyjob.employer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentJobDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class JobDetailFragment : Fragment() {

    private  var _binding: FragmentJobDetailBinding? =null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentJobDetailBinding.inflate(inflater,container,false)

        //hide nav bar
        (activity as EmployerHome).hideBottomNavigationView()

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


        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "EmployerProfileImages/$currentUser"
        val imageRef = storageRef.child(filePathAndName)


        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imgJob)
        }.addOnFailureListener {}

        val jobId = arguments?.getString("job_id")
        binding.btnEditJob.setOnClickListener{
            val bundle = Bundle().apply {
                putString("jobId", jobId)
            }
            it.findNavController().navigate(R.id.action_jobDetailFragment_to_employerJobForm, bundle)
        }

        binding.btnManageCandidate.setOnClickListener {
            val bundle = Bundle().apply {
                putString("jobId", jobId)
            }
            it.findNavController().navigate(R.id.action_jobDetailFragment_to_applicantManagementFragment, bundle)
        }

        return binding.root
    }

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