package com.example.easyjob.employer

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentJobDetailBinding
import com.example.easyjob.databinding.FragmentJobEditBinding
import com.google.firebase.database.*
import java.util.ArrayList


class JobEditFragment : Fragment() {
    private  var _binding: FragmentJobEditBinding? =null
    private val binding get() = _binding!!
    //private val jobId = arguments?.getString("jobId").toString()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentJobEditBinding.inflate(inflater,container,false)

        //hide nav bar

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

        val jobId = arguments?.getString("jobId")
        if(!jobId.isNullOrEmpty()){
            fillData()
        }else{
            Toast.makeText(requireContext(), "Fail to get job details data", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }

        binding.btnUpdateJob.setOnClickListener{
            validateJobData()
        }

        binding.btnCancel.setOnClickListener{
            showConfirmationDialog()
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showConfirmationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Confirm")
        alertDialog.setMessage("Do you want to exit without saving changes?")
        alertDialog.setPositiveButton("Yes") { _, _ ->
            requireActivity().onBackPressed()
        }
        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    private fun fillData() {

        val jobId = arguments?.getString("jobId")
        database = FirebaseDatabase.getInstance().reference
        database.child("Jobs").child(jobId.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val job = snapshot.getValue(JobData::class.java)
                    if (job != null) {
                        binding.etEnterJobTitle.setText(job.jobTitle)
                        binding.etJobSalary.setText(job.jobSalary)
                        binding.btnTimePickerStart.text = job.workingHourStart
                        binding.btnTimePickerEnd.text = job.workingHourEnd
                        binding.etWorkplace.setText(job.workplace)
                        binding.etJobRequirement.setText(job.jobRequirement)
                        binding.etResponsibilities.setText(job.jobResponsibilities)

                        // get the jobType value from the snapshot
                        val jobType = job.jobType

                        // check if the jobType ArrayList contains a specific value and set the checkbox accordingly
                        if (jobType!!.contains("Full Time")) {
                            binding.chkJobTypeFullTime.isChecked = true
                        }
                        if (jobType.contains("Part Time")) {
                            binding.chkJobTypePartTime.isChecked = true
                        }
                        if (jobType.contains("Internship")) {
                            binding.chkJobTypeInternship.isChecked = true
                        }

                        if(job.jobStatus == "Available")
                            binding.swJobStatus.isChecked
                        else
                            binding.swJobStatus.isChecked = false
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun validateJobData() {

        val selectedJobTypes = ArrayList<String>()
        if (binding.chkJobTypeInternship?.isChecked == true) {
            selectedJobTypes.add(binding.chkJobTypeInternship?.text.toString())
        }
        if (binding.chkJobTypePartTime?.isChecked == true) {
            selectedJobTypes.add(binding.chkJobTypePartTime?.text.toString())
        }
        if (binding.chkJobTypeFullTime?.isChecked == true) {
            selectedJobTypes.add(binding.chkJobTypeFullTime?.text.toString())
        }

        val jobstatus = if(binding.swJobStatus.isChecked)"Available" else "Unavailable"

        if(binding.etEnterJobTitle.text.toString().isEmpty())
        {
            binding.etEnterJobTitle.error = "Job title can't be empty"
        }
        else if(selectedJobTypes.isEmpty())
        {
            binding.tvJobType.error = "Please select at least one job type"
        }
        else if(binding.etJobSalary.text.toString().isEmpty())
        {
            binding.etJobSalary.error = "Salary can't be empty"
        }
        else if(binding.btnTimePickerStart?.text.toString() =="START")
        {
            binding.btnTimePickerStart?.error = "Please choose the starting time"
        }
        else if(binding.btnTimePickerEnd?.text.toString() =="END")
        {
            binding.btnTimePickerEnd?.error = "Please select an off duty time"
        }
        else if(binding.etWorkplace.text.toString().isEmpty())
        {
            binding.etWorkplace.error = "Please enter the location"
        }
        else if(binding.etJobRequirement.text.toString().isEmpty())
        {
            binding.etJobRequirement.error = "Please list the job requirements"
        }
        else if(binding.etResponsibilities.text.toString().isEmpty())
        {
            binding.etJobRequirement.error = "Please list the job responsibilities"
        }else{
            database = FirebaseDatabase.getInstance().reference
            val jobId = arguments?.getString("jobId")
            val jobRef = database.child("Jobs").child(jobId.toString())

            jobRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val job = snapshot.getValue(JobData::class.java)

                    // 更新 job 对象的属性
                    job?.apply {
                        jobTitle = binding.etEnterJobTitle.text.toString()
                        jobType = selectedJobTypes
                        jobSalary = binding.etJobSalary.text.toString()
                        workingHourStart = binding.btnTimePickerStart.text.toString()
                        workingHourEnd = binding.btnTimePickerEnd.text.toString()
                        workplace = binding.etWorkplace.text.toString()
                        jobRequirement = binding.etJobRequirement.text.toString()
                        jobResponsibilities = binding.etResponsibilities.text.toString()
                        jobStatus = jobstatus

                    }

                    // 将 job 对象写入到指定节点
                    jobRef.setValue(job).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(requireContext(), "Updated Successfully!!", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        } else {
                            Toast.makeText(requireContext(), "Something error, your job cannot be updated", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                    Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



}