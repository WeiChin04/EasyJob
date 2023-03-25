package com.example.easyjob.employer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.user.UserApplicationData
import com.example.easyjob.user.UserData
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ApplicantAdapter(private val childId: String,
                       private val appliedDataList: ArrayList<UserApplicationData>,
                       private val applicantList: ArrayList<UserData>
                       ) : RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.applicant_item,
            parent, false)
        return ApplicantViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {


        val (appliedDataListPosition, applicantListPosition) = getPosition(position)

        val appliedDataList = appliedDataList[appliedDataListPosition]
        val applicantList = applicantList[applicantListPosition]

        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "UsersProfileImages/"+applicantList.userId
        val imageRef = storageRef.child(filePathAndName)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(holder.itemView.context)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.imgApplicant)
        }.addOnFailureListener {}

        Log.d("filePath","path$filePathAndName")

        val currentTimeMillis = appliedDataList.appliedAt!!.toLong()
        val date = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
        val currentTime = dateFormat.format(date)
        holder.applyDate.text = currentTime

        holder.applicantName.text = applicantList.name
        holder.applicantContract.text = applicantList.contact
        holder.applicantEmail.text = applicantList.email
        holder.applyJobStatus.text = appliedDataList.status

        when (holder.applyJobStatus.text) {
            "Pending" -> {
                holder.applyJobStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
            }
            "Approved" -> {
                holder.applyJobStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            }
            else -> {
                holder.applyJobStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            }
        }

        holder.cardView.setOnClickListener{
            val bundle = bundleOf(
                "application_id" to appliedDataList.applicationId,
                "deviceToken" to applicantList.deviceToken,
                "applicant_id" to applicantList.userId,
                "name" to applicantList.name,
                "contact" to applicantList.contact,
                "email" to applicantList.email,
                "education_level" to applicantList.education_level,
                "expected_salary" to applicantList.jobsalary,
                "address" to applicantList.address,
                "about_me" to applicantList.about_me,
                "apply_status" to appliedDataList.status,
                "job_id" to appliedDataList.jobId,
                "applicant_wallet_id" to applicantList.walletId
            )
            it.findNavController().navigate(R.id.action_applicantManagementFragment_to_applicantDetailsFragment, bundle)
        }
    }

    private fun getPosition(position: Int): Pair<Int, Int> {
        val appliedDataListPosition = position % appliedDataList.size
        val applicantListPosition = position % applicantList.size
        return Pair(appliedDataListPosition, applicantListPosition)
    }

    override fun getItemCount(): Int {
        return maxOf(appliedDataList.size, applicantList.size)
    }

    class ApplicantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val applyDate : TextView = itemView.findViewById(R.id.tvShowApplyDate)
        val applicantName : TextView = itemView.findViewById(R.id.tvShowApplicantName)
        val applicantContract : TextView = itemView.findViewById(R.id.tvShowApplicantContact)
        val applicantEmail : TextView = itemView.findViewById(R.id.tvShowApplicantEmail)
        val applyJobStatus : TextView = itemView.findViewById(R.id.tvShowStatus)
        val imgApplicant : ImageView = itemView.findViewById(R.id.imgApplicant)
        val cardView: CardView = itemView.findViewById(R.id.applicantCardView)
    }

}