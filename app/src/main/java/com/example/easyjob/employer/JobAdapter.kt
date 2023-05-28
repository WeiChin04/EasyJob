package com.example.easyjob.employer

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easyjob.AnalysisData
import com.example.easyjob.R
import com.example.easyjob.user.UserApplicationData
import com.example.easyjob.user.UserData
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class JobAdapter(private val jobList: ArrayList<JobData>) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

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

        val currentTimeMillis = currentItem.currentDate!!.toLong()
        val date = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
        val currentTime = dateFormat.format(date)
        holder.datePosted.text = currentTime
        holder.jobTitle.text = currentItem.jobTitle
        holder.jobType.text = currentItem.jobType.toString().replace("[", "").replace("]", "")
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

        if(holder.jobStatus.text == "Unavailable"){
            holder.jobStatus.setBackgroundColor(Color.GRAY)
        }

        val status = mutableListOf<String>()
        dbRef = FirebaseDatabase.getInstance().getReference("Applications")
        dbRef.orderByChild("jobId").equalTo(currentItem.jobId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (applicantSnapshot in snapshot.children) {
                            val applyDetail = applicantSnapshot.getValue(UserApplicationData::class.java)
                            val applicantStatus = applyDetail!!.status
                            if (applicantStatus == "Pending") {
                                applicantStatus.let { status.add(it) }
                            }
                        }
                        for (applicantStatus in status) {
                            val jobQuery = dbRef.orderByChild("status").equalTo(applicantStatus)
                            jobQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        holder.redDot.visibility = View.VISIBLE
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                                }
                            })
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }
            })

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
                "job_type" to jobList[position].jobType.toString().replace("[", "").replace("]", ""),
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
        val redDot: View = itemView.findViewById(R.id.redDot)

    }

}