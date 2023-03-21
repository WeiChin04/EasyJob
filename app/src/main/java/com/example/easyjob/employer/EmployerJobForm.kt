package com.example.easyjob.employer

import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.databinding.FragmentEmployerJobFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class EmployerJobForm : Fragment() {
    private  var _binding: FragmentEmployerJobFormBinding? =null
    private val binding get() = _binding!!
    private lateinit var dbRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEmployerJobFormBinding.inflate(inflater,container,false)

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
        //hide nav bar
        (activity as EmployerHome).hideBottomNavigationView()





        /*        TIME PICKER FOR JOB HOUR*/
        val calendarStart = Calendar.getInstance()
        val timePickerDialogStart = TimePickerDialog(
            requireActivity(),
            { _, hourOfDay, minute ->
                calendarStart.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarStart.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                binding.btnTimePickerStart.text = timeFormat.format(calendarStart.time)
            },
            calendarStart.get(Calendar.HOUR_OF_DAY),
            calendarStart.get(Calendar.MINUTE),
            false
        )
        binding.btnTimePickerStart.setOnClickListener{
            timePickerDialogStart.show()
        }

        val calendarEnd= Calendar.getInstance()
        val timePickerDialogEnd = TimePickerDialog(
            requireActivity(),
            { _, hourOfDay, minute ->
                calendarEnd.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarEnd.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                binding.btnTimePickerEnd.text = timeFormat.format(calendarEnd.time)
            },
            calendarEnd.get(Calendar.HOUR_OF_DAY),
            calendarEnd.get(Calendar.MINUTE),
            false
        )
        binding.btnTimePickerEnd.setOnClickListener{
            timePickerDialogEnd.show()
        }

        if(arguments?.getString("actionPostJob") == "actionPostJob"){
            binding.btnUpdateJob.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
        }else{
            binding.btnPostNow.visibility = View.GONE
            val jobId = arguments?.getString("jobId")
            if(!jobId.isNullOrEmpty()){
                fillData()
            }else{
                Toast.makeText(requireContext(), "Fail to get job details data", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
        }

        binding.btnPostNow.setOnClickListener{
            postJob()
        }

        binding.btnUpdateJob.setOnClickListener{
            validateJobData()
        }

        binding.btnCancel.setOnClickListener{
            showConfirmationDialog()
        }

        return binding.root
    }

    @Deprecated("Deprecated in Java")
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

    private fun postJob(){
        val jobTitle = binding.etEnterJobTitle.text.toString()

        val selectedJobTypes = ArrayList<String>()
        if (binding.chkJobTypeInternship.isChecked) {
            selectedJobTypes.add(binding.chkJobTypeInternship.text.toString())
        }
        if (binding.chkJobTypePartTime.isChecked) {
            selectedJobTypes.add(binding.chkJobTypePartTime.text.toString())
        }
        if (binding.chkJobTypeFullTime.isChecked) {
            selectedJobTypes.add(binding.chkJobTypeFullTime.text.toString())
        }

        val jobSalary = binding.etJobSalary.text.toString()
        val jobStart = binding.btnTimePickerStart.text.toString()
        val jobEnd = binding.btnTimePickerEnd.text.toString()
        val workplace = binding.etWorkplace.text.toString()
        val jobReq = binding.etJobRequirement.text.toString()
        val jobResp = binding.etResponsibilities.text.toString()
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
        else if(binding.btnTimePickerStart.text.toString() =="START")
        {
            binding.btnTimePickerStart.error = "Please choose the starting time"
        }
        else if(binding.btnTimePickerEnd.text.toString() =="END")
        {
            binding.btnTimePickerEnd.error = "Please select an off duty time"
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
        }
        else {
            database = FirebaseDatabase.getInstance()
            dbRef = database.getReference("Jobs")

            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

            val jobId = dbRef.push().key
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val employerImgPath = "EmployerProfileImages/$currentUser"
            val currentDate = sdf.format(Date())

            val newJob = JobData(
                currentUser,
                jobTitle,
                selectedJobTypes,
                jobSalary,
                jobStart,
                jobEnd,
                workplace,
                jobReq,
                jobResp,
                jobstatus,
                currentDate,
                jobId,
                employerImgPath
            )

            if (jobId != null) {
                dbRef.child(jobId).setValue(newJob)
                analysisCreate(jobId)
                Toast.makeText(requireContext(), "Post Successfully!!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Post Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun analysisCreate(jobId : String) {

        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(jobId)

        val clickCount = 0
        val favouriteCount = 0
        val lastClickTime = "0"

        val analysisData = hashMapOf(
            "clickCount" to clickCount,
            "favouriteCount" to favouriteCount,
            "lastClickTime" to lastClickTime
        )

        dbRef.setValue(analysisData)

    }

    private fun fillData() {

        val jobId = arguments?.getString("jobId")
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("Jobs").child(jobId.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
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
        if (binding.chkJobTypeInternship.isChecked) {
            selectedJobTypes.add(binding.chkJobTypeInternship.text.toString())
        }
        if (binding.chkJobTypePartTime.isChecked) {
            selectedJobTypes.add(binding.chkJobTypePartTime.text.toString())
        }
        if (binding.chkJobTypeFullTime.isChecked) {
            selectedJobTypes.add(binding.chkJobTypeFullTime.text.toString())
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
        else if(binding.btnTimePickerStart.text.toString() =="START")
        {
            binding.btnTimePickerStart.error = "Please choose the starting time"
        }
        else if(binding.btnTimePickerEnd.text.toString() =="END")
        {
            binding.btnTimePickerEnd.error = "Please select an off duty time"
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
            dbRef = FirebaseDatabase.getInstance().reference
            val jobId = arguments?.getString("jobId")
            val jobRef = dbRef.child("Jobs").child(jobId.toString())

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