package com.example.easyjob.user

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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

class UserJobAppliedAdapter(private val jobList: ArrayList<UserApplicationData>, private val jobData: ArrayList<JobData>) : RecyclerView.Adapter<UserJobAppliedAdapter.UserJobAppliedViewHolder>() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

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

        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = currentJobItem.imgPath
        val imageRef = storageRef.child(filePathAndName.toString())

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(holder.itemView.context)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.employerImage)
        }.addOnFailureListener {
        }

        val currentTimeMillis = currentItem.appliedAt!!.toLong()
        val date = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
        val currentTime = dateFormat.format(date)
        holder.dateApplied.text = currentTime
        holder.jobTitle.text = currentJobItem.jobTitle
        holder.jobType.text = currentJobItem.jobType.toString().replace("[", "").replace("]", "")
        holder.jobAppliedStatus.text = currentItem.status

        val approvedTimeMillis = currentItem.approvedAt
        if (approvedTimeMillis.isNullOrEmpty()) {
            holder.lyApproved.visibility =View.GONE
        } else {
            holder.lyApproved.visibility =View.VISIBLE
            val approvedDate = Date(approvedTimeMillis.toLong())
            val approvedDateFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
            val approvedCurrentTime = approvedDateFormat.format(approvedDate)
            holder.dateApproved.text = approvedCurrentTime
        }

        val rejectedTimeMillis = currentItem.rejectAt
        if (rejectedTimeMillis.isNullOrEmpty()) {
            holder.lyRejected.visibility =View.GONE
        } else {
            holder.lyRejected.visibility =View.VISIBLE
            val rejectedDate = Date(rejectedTimeMillis.toLong())
            val rejectedDateFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
            val rejectedCurrentTime = rejectedDateFormat.format(rejectedDate)
            holder.dateRejected.text = rejectedCurrentTime
        }


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

        when (holder.jobAppliedStatus.text) {
            "Pending" -> {
                holder.jobAppliedStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
            }
            "Approved" -> {
                holder.jobAppliedStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            }
            else -> {
                holder.jobAppliedStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            }
        }

        holder.userCardView.setOnClickListener{
            val bundle = bundleOf(
                "application_id" to jobList[position].applicationId,
                "job_id" to jobList[position].jobId,
                "job_title" to jobData[position].jobTitle,
                "job_salary" to jobData[position].jobSalary,
                "job_location" to jobData[position].workplace,
                "job_working_hour_start" to jobData[position].workingHourStart,
                "job_working_hour_end" to jobData[position].workingHourEnd,
                "job_requirement" to jobData[position].jobRequirement,
                "job_responsibilities" to jobData[position].jobResponsibilities,
                "job_type" to jobData[position].jobType.toString().replace("[", "").replace("]", ""),
                "job_status" to jobData[position].jobStatus,
                "employer_id" to jobData[position].employerId,
                "apply_status" to jobList[position].status,
                "fromAppliedJob" to "yes"
            )

            it.findNavController().navigate(R.id.action_userMyJob_to_userJobDetailFragment, bundle)

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
        val jobCTR : TextView = itemView.findViewById(R.id.tvShowViewed)
        val jobAppliedStatus : TextView = itemView.findViewById(R.id.tvShowJobAppliedStatus)
        val employerImage : ImageView = itemView.findViewById(R.id.employer_imageView)
        val userCardView: CardView = itemView.findViewById(R.id.userJobAppliedCardView)
        val dateApproved : TextView = itemView.findViewById(R.id.tvApprovedDate)
        val dateRejected : TextView = itemView.findViewById(R.id.tvRejectedDate)
        val lyApproved: LinearLayout = itemView.findViewById(R.id.lyApprovedDate)
        val lyRejected: LinearLayout = itemView.findViewById(R.id.lyRejectedDate)
    }

}