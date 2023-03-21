package com.example.easyjob.employer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.google.firebase.storage.FirebaseStorage

class JobAdapter(private val jobList: ArrayList<JobData>) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.job_item,
        parent, false)
        return JobViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {

        val currentItem = jobList[position]

        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "EmployerProfileImages/"+currentItem.employerId
        val imageRef = storageRef.child(filePathAndName)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(holder.itemView.context)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.employerImg)
        }.addOnFailureListener {}

        holder.datePosted.text = currentItem.currentDate
        holder.jobTitle.text = currentItem.jobTitle
        holder.jobType.text = currentItem.jobType.toString()
        holder.jobStatus.text = currentItem.jobStatus

        if(holder.jobStatus.text == "Unavailable"){
            holder.jobStatus.setBackgroundColor(Color.GRAY)
        }

        holder.cardView.setOnClickListener{
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
            it.findNavController().navigate(R.id.action_jobView_to_jobDetailFragment, bundle)
        }


    }

    override fun getItemCount(): Int {

        return jobList.size
    }

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val datePosted : TextView = itemView.findViewById(R.id.tvPostingDate)
        val jobCTR : TextView = itemView.findViewById(R.id.tvShowViewed)
        val jobTitle : TextView = itemView.findViewById(R.id.tvShowJobTitle)
        val jobType : TextView = itemView.findViewById(R.id.tvShowJobType)
        val jobStatus : TextView = itemView.findViewById(R.id.tvShowAvailable)
        val cardView: CardView = itemView.findViewById(R.id.jobCardView)
        val employerImg: ImageView = itemView.findViewById(R.id.imgEmployerForUserHome)

    }

}