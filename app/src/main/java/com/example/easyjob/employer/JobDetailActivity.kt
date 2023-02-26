package com.example.easyjob.employer

import android.annotation.SuppressLint
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.easyjob.databinding.ActivityJobDetailBinding
import com.example.easyjob.databinding.ActivityUserHomeBinding

class JobDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailBinding


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*      return Arrow*/
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

/*        putExtra("job_id",jobList[position].jobId)
        putExtra("job_title",jobList[position].jobTitle)
        putExtra("job_salary",jobList[position].jobSalary)
        putExtra("job_location",jobList[position].workplace)
        putExtra("job_working_hour_start",jobList[position].workingHourStart)
        putExtra("job_working_hour_end",jobList[position].workingHourEnd)
        putExtra("job_requirement",jobList[position].jobRequirement)
        putExtra("job_responsibilities",jobList[position].jobResponsibilities)
        putExtra("job_type",jobList[position].jobType)
        putExtra("job_status",jobList[position].jobStatus)*/

        val title = intent.getStringExtra("job_title")
        val salary = intent.getStringExtra("job_salary")
        val location = intent.getStringExtra("job_salary")
        val jobType = intent.getStringExtra("job_type")
        val jobWorkingHourStar = intent.getStringExtra("job_working_hour_start")
        val jobWorkingHourEnd = intent.getStringExtra("job_working_hour_end")
        val requirement = intent.getStringExtra("job_requirement")
        val responsibilities = intent.getStringExtra("job_responsibilities")


        binding.tvJobDetailTitle.text = title
        binding.tvJobDetailSalary.text = "RM $salary"
        binding.tvJobDetailsWorkPlace.text = location
        binding.tvJobDetailsJobType.text = jobType
        binding.tvJobDetailsWorkingHourStart.text = jobWorkingHourStar
        binding.tvJobDetailsWorkingHourEnd.text = jobWorkingHourEnd
        binding.tvJobDetailRequirementContent.text = requirement
        binding.tvJobDetailResContent.text = responsibilities


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
}