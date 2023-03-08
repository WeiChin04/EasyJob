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

class UserJobAdapter(private val jobList: ArrayList<UserJobData>) : RecyclerView.Adapter<UserJobAdapter.UserJobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserJobViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.job_item_user,
            parent, false)
        return UserJobViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: UserJobViewHolder, position: Int) {

        val currentItem = jobList[position]
        holder.datePosted.text = currentItem.currentDate
        holder.jobCTR.text = currentItem.ctr.toString()
        holder.jobTitle.text = currentItem.jobTitle
        holder.jobType.text = currentItem.jobType.toString()
        holder.jobStatus.text = currentItem.jobStatus

        if(holder.jobStatus.text == "Unavailable"){
            holder.jobStatus.setBackgroundColor(Color.GRAY)
        }

        holder.userCardView.setOnClickListener{
            val bundle = bundleOf(
                "job_id" to jobList[position].jobId,
                "job_title" to jobList[position].jobTitle,
                "job_salary" to jobList[position].jobSalary,
                "job_location" to jobList[position].workplace,
                "job_working_hour_start" to jobList[position].workingHourStart,
                "job_working_hour_end" to jobList[position].workingHourEnd,
                "job_requirement" to jobList[position].jobRequirement,
                "job_responsibilities" to jobList[position].jobResponsibilities,
                "job_type" to jobList[position].jobType.toString(),
                "job_status" to jobList[position].jobStatus
            )
            it.findNavController().navigate(R.id.action_userHomeFragment_to_userJobDetailFragment, bundle)
        }


    }

    override fun getItemCount(): Int {

        return jobList.size
    }

    class UserJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val datePosted : TextView = itemView.findViewById(R.id.tvPostingDate)
        val jobCTR : TextView = itemView.findViewById(R.id.tvShowViewed)
        val jobTitle : TextView = itemView.findViewById(R.id.tvShowJobTitle)
        val jobType : TextView = itemView.findViewById(R.id.tvShowJobType)
        val jobStatus : TextView = itemView.findViewById(R.id.tvShowAvailable)
        val userCardView: CardView = itemView.findViewById(R.id.userJobCardView)

    }

}