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
import com.example.easyjob.AnalysisData
import com.example.easyjob.R
import com.example.easyjob.employer.JobData
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserFavouriteJobAdapter (private val jobDataList: ArrayList<JobData>) : RecyclerView.Adapter<UserFavouriteJobAdapter.UserJobFavouriteViewHolder>(){

    private lateinit var dbRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserJobFavouriteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.job_item_user,
            parent, false)
        return UserJobFavouriteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserJobFavouriteViewHolder, position: Int) {
        val currentItem = jobDataList[position]
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


        val currentTimeMillis = currentItem.currentDate!!.toLong()
        val date = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
        val currentTime = dateFormat.format(date)
        holder.datePosted.text = currentTime
        holder.jobTitle.text = currentItem.jobTitle
        holder.jobType.text = currentItem.jobType.toString()
        holder.jobStatus.text = currentItem.jobStatus

        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(currentItem.jobId.toString())

        dbRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val ctr = snapshot.getValue(AnalysisData::class.java)
                    if (ctr != null) {
                        val showCTR = ctr.clickCount ?: 0
                        val stringRes = R.string.viewed
                        val str = holder.itemView.context.getString(stringRes)
                        holder.jobCTR.text = "$showCTR $str"
                        Log.d("showCTR", "showCTR: $showCTR")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

        if (holder.jobStatus.text == "Unavailable") {
            holder.jobStatus.setBackgroundColor(Color.GRAY)
        }

        holder.userCardView.setOnClickListener {
            val bundle = bundleOf(
                "employer_id" to jobDataList[position].employerId,
                "job_id" to jobDataList[position].jobId,
                "job_title" to jobDataList[position].jobTitle,
                "job_salary" to jobDataList[position].jobSalary,
                "job_location" to jobDataList[position].workplace,
                "job_working_hour_start" to jobDataList[position].workingHourStart,
                "job_working_hour_end" to jobDataList[position].workingHourEnd,
                "job_requirement" to jobDataList[position].jobRequirement,
                "job_responsibilities" to jobDataList[position].jobResponsibilities,
                "job_type" to jobDataList[position].jobType.toString(),
                "job_status" to jobDataList[position].jobStatus
            )
            it.findNavController()
                .navigate(R.id.action_favouriteJobFragment_to_userJobDetailFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return jobDataList.size
    }

    class UserJobFavouriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val datePosted: TextView = itemView.findViewById(R.id.tvPostingDate)
        val jobCTR: TextView = itemView.findViewById(R.id.tvShowViewed)
        val jobTitle: TextView = itemView.findViewById(R.id.tvShowJobTitle)
        val jobType: TextView = itemView.findViewById(R.id.tvShowJobType)
        val jobStatus: TextView = itemView.findViewById(R.id.tvShowAvailable)
        val userCardView: CardView = itemView.findViewById(R.id.userJobCardView)
        val imgEmployer: ImageView = itemView.findViewById(R.id.imgEmployerForUserHome)
    }

}

