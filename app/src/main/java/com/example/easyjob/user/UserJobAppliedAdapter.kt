package com.example.easyjob.user

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.R

class UserJobAppliedAdapter(private val jobList: ArrayList<UserApplicationData>, private val jobData: ArrayList<UserJobData>) : RecyclerView.Adapter<UserJobAppliedAdapter.UserJobAppliedViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserJobAppliedViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.job_item_user_applied,
            parent, false)
        return UserJobAppliedViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserJobAppliedViewHolder, position: Int) {

        val (jobListPosition, jobDataPosition) = getPosition(position)

        val currentItem = jobList[jobListPosition]
        val currentJobItem = jobData[jobDataPosition]
        holder.dateApplied.text = currentItem.appliedAt
        holder.jobTitle.text = currentJobItem.jobTitle
        holder.jobType.text = currentJobItem.jobType.toString()
        holder.jobAppliedStatus.text = currentItem.status

        when (holder.jobAppliedStatus.text) {
            "Pending" -> {
                holder.jobAppliedStatus.setBackgroundColor(Color.GRAY)
            }
            "Approved" -> {
                holder.jobAppliedStatus.setBackgroundColor(Color.GREEN)
            }
            else -> {
                holder.jobAppliedStatus.setBackgroundColor(Color.RED)
            }
        }

        holder.userCardView.setOnClickListener{
            val bundle = bundleOf(
                "job_id" to jobList[position].jobId,
                "job_title" to jobData[position].jobTitle,
                "job_salary" to jobData[position].jobSalary,
                "job_location" to jobData[position].workplace,
                "job_working_hour_start" to jobData[position].workingHourStart,
                "job_working_hour_end" to jobData[position].workingHourEnd,
                "job_requirement" to jobData[position].jobRequirement,
                "job_responsibilities" to jobData[position].jobResponsibilities,
                "job_type" to jobData[position].jobType.toString(),
                "job_status" to jobData[position].jobStatus
            )
            //it.findNavController().navigate(R.id.action_jobView_to_jobDetailFragment, bundle)
        }

    }

    private fun getPosition(position: Int): Pair<Int, Int> {
        val jobListPosition = position % jobList.size
        val jobDataPosition = position % jobData.size
        return Pair(jobListPosition, jobDataPosition)
    }

    override fun getItemCount(): Int {
        return maxOf(jobList.size, jobData.size)
    }
    class UserJobAppliedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val dateApplied : TextView = itemView.findViewById(R.id.tvAppliedDate)
        val jobTitle : TextView = itemView.findViewById(R.id.tvShowJobTitle)
        val jobType : TextView = itemView.findViewById(R.id.tvShowJobType)
        val jobAppliedStatus : TextView = itemView.findViewById(R.id.tvShowJobAppliedStatus)
        val userCardView: CardView = itemView.findViewById(R.id.userJobAppliedCardView)

    }

}