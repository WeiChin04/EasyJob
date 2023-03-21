package com.example.easyjob.user

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Color
import android.util.Log
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
import com.example.easyjob.employer.JobData
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class UserSearchJobAdapter(private var jobList: ArrayList<JobData>) : RecyclerView.Adapter<UserJobAdapter.UserJobViewHolder>(){

    private lateinit var dbRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserJobAdapter.UserJobViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.job_item_user,
            parent, false
        )
        return UserJobAdapter.UserJobViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserJobAdapter.UserJobViewHolder, position: Int) {

        val currentItem = jobList[position]
        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = currentItem.imgPath
        val imageRef = storageRef.child(filePathAndName.toString())

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(holder.itemView.context)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.imgEmployer)
        }.addOnFailureListener {
        }


        holder.datePosted.text = currentItem.currentDate
        holder.jobTitle.text = currentItem.jobTitle
        holder.jobType.text = currentItem.jobType.toString()
        holder.jobStatus.text = currentItem.jobStatus

        if (holder.jobStatus.text == "Unavailable") {
            holder.jobStatus.setBackgroundColor(Color.GRAY)
        }

        holder.userCardView.setOnClickListener {
            val bundle = bundleOf(
                "employer_id" to jobList[position].employerId,
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
            clickThroughRate(this.jobList[position].jobId.toString())
            it.findNavController()
                .navigate(R.id.action_userSearchResult_to_userJobDetailFragment, bundle)
        }
    }

    private fun clickThroughRate(jobId: String) {

        Log.d("jobId", "status: $jobId")
        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(jobId)
        val currentTime = System.currentTimeMillis().toString()
        val clickCountRef = dbRef.child("clickCount")
        val lastClickTimeRef = dbRef.child("lastClickTime")
        clickCountRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var clickCount = currentData.getValue(Int::class.java)
                if (clickCount == null) {
                    clickCount = 0
                }
                currentData.value = clickCount + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }
                if (committed) {
                    lastClickTimeRef.setValue(currentTime)
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredList(filteredList: ArrayList<JobData>){
        this.jobList = filteredList
        notifyDataSetChanged()
    }

    class UserJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val datePosted: TextView = itemView.findViewById(R.id.tvPostingDate)
        val jobCTR: TextView = itemView.findViewById(R.id.tvShowViewed)
        val jobTitle: TextView = itemView.findViewById(R.id.tvShowJobTitle)
        val jobType: TextView = itemView.findViewById(R.id.tvShowJobType)
        val jobStatus: TextView = itemView.findViewById(R.id.tvShowAvailable)
        val userCardView: CardView = itemView.findViewById(R.id.userJobCardView)
        val imgEmployer: ImageView = itemView.findViewById(R.id.imgEmployerForUserHome)

    }
}