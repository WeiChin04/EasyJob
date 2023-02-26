package com.example.easyjob.employer

import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.easyjob.databinding.ActivityPostJobBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class PostJob : AppCompatActivity() {

    private lateinit var binding: ActivityPostJobBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var uid: String
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

/*        SET SWITCH TEXT */
        binding.swJobStatus.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                binding.swJobStatus.text = "Available"
            } else {
                binding.swJobStatus.text = "Unavailable"
            }
        }

/*        TIME PICKER FOR JOB HOUR*/
        val calendarStart = Calendar.getInstance()
        val timePickerDialogStart = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendarStart.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarStart.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                binding.btnTimePickerStart!!.text = timeFormat.format(calendarStart.time)
            },
            calendarStart.get(Calendar.HOUR_OF_DAY),
            calendarStart.get(Calendar.MINUTE),
            false
        )
        binding.btnTimePickerStart!!.setOnClickListener{
            timePickerDialogStart.show()
        }

        val calendarEnd= Calendar.getInstance()
        val timePickerDialogEnd = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendarEnd.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendarEnd.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                binding.btnTimePickerEnd!!.text = timeFormat.format(calendarEnd.time)
            },
            calendarEnd.get(Calendar.HOUR_OF_DAY),
            calendarEnd.get(Calendar.MINUTE),
            false
        )
        binding.btnTimePickerEnd!!.setOnClickListener{
            timePickerDialogEnd.show()
        }

        binding.btnPostNow.setOnClickListener{
            postJob()
        }

/*      return Arrow*/
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // 如果用户按下了 Toolbar 的返回键，那么关闭当前 Activity
                finish()
                return true
            }
            // 如果还有其他菜单项，可以在这里添加处理代码
            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun postJob(){
        val jobTitle = binding.etEnterJobTitle.text.toString()

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

        val jobSalary = binding.etJobSalary.text.toString()
        val jobStart = binding.btnTimePickerStart?.text.toString()
        val jobEnd = binding.btnTimePickerEnd?.text.toString()
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
        }
        else {
            database = FirebaseDatabase.getInstance()
            dbRef = database.getReference("Jobs")

            val currentUser = FirebaseAuth.getInstance().currentUser!!.uid

            val jobId = dbRef.push().key
            val jobCtr = 0
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
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
                jobCtr,
                currentDate,
                jobId
            )

            if (jobId != null) {
                dbRef.child(jobId).setValue(newJob)
                Toast.makeText(this, "Post Success!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, EmployerHome::class.java))
            } else {
                Toast.makeText(this, "Post Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }




}