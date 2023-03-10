package com.example.easyjob.employer

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
import com.example.easyjob.user.UserApplicationData
import com.example.easyjob.user.UserData

class ApplicantAdapter(private val appliedDataList: ArrayList<UserApplicationData>, private val applicantList: ArrayList<UserData>) : RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.applicant_item,
            parent, false)
        return ApplicantViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {


        val (appliedDataListPosition, applicantListPosition) = getPosition(position)

        val appliedDataList = appliedDataList[appliedDataListPosition]
        val applicantList = applicantList[applicantListPosition]

        holder.applyDate.text = appliedDataList.appliedAt
        holder.applicantName.text = applicantList.name
        holder.applicantContract.text = applicantList.contact
        holder.applicantEmail.text = applicantList.email

        if(holder.applyJobStatus.text == "Pending"){
            holder.applyJobStatus.setBackgroundColor(Color.GRAY)
        }

        holder.cardView.setOnClickListener{
            val bundle = bundleOf(

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
        val cardView: CardView = itemView.findViewById(R.id.applicantCardView)

    }

}