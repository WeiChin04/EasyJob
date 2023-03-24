package com.example.easyjob.employer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.AnalysisData
import com.example.easyjob.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class jobAnalysisAdapter (private val jobDataList: ArrayList<JobData>,
                          private val jobAnalysisDataList: ArrayList<AnalysisData>
                          ) : RecyclerView.Adapter<jobAnalysisAdapter.jobAnalysisViewHolder>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): jobAnalysisViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.job_analysis_item,
            parent,false
        )
        return jobAnalysisViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: jobAnalysisViewHolder, position: Int) {

        val (jobListPosition, jobAnalysisPosition) = getPosition(position)

        val currentJob = jobDataList[jobListPosition]
        val currentAnalysis = jobAnalysisDataList[jobAnalysisPosition]

        if(currentAnalysis.clickCount == 0){
            holder.totalView.text = "0"
        }else{
            holder.totalView.text = currentAnalysis.clickCount.toString()
        }

        holder.jobAnalysisTitle.text = currentJob.jobTitle

        val currentTimeMills = currentAnalysis.lastClickTime
        var calCurrentTime = ""

        if (currentTimeMills.isNullOrEmpty()) {
            holder.lastViewAt.text = "-"
        } else {
            val date = Date(currentTimeMills.toLong())
            val dateFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
            val currentTime = dateFormat.format(date)
            calCurrentTime = currentTime
            holder.lastViewAt.text = currentTime
        }

        holder.cardView.setOnClickListener {
            val bundle = bundleOf(
                "total_click" to currentAnalysis.clickCount.toString(),
                "total_applied" to currentAnalysis.totalApply.toString(),
                "total_approved" to currentAnalysis.totalApproved.toString(),
                "total_cancelled" to currentAnalysis.totalCancel.toString(),
                "total_rejected" to currentAnalysis.totalRejected.toString(),
                "total_favourite" to currentAnalysis.favouriteCount.toString(),
                "last_click" to calCurrentTime
            )
            it.findNavController().navigate(R.id.action_employerHomeFragment_to_jobAnalysisDetailsFragment, bundle)
        }

    }

    private fun getPosition(position: Int): Pair<Int, Int> {
        val jobListPosition = position % jobDataList.size
        val jobDataPosition = position % jobAnalysisDataList.size
        return Pair(jobListPosition, jobDataPosition)
    }

    override fun getItemCount(): Int {
        return  maxOf(jobDataList.size, jobAnalysisDataList.size)
    }

    class jobAnalysisViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){

        val jobAnalysisTitle : TextView = itemView.findViewById(R.id.tvJobAnalysisTitle)
        val totalView : TextView = itemView.findViewById(R.id.tvTotalView)
        val lastViewAt : TextView = itemView.findViewById(R.id.tvLastViewAt)
        val cardView : CardView = itemView.findViewById(R.id.cvJobAnalysis)


    }
}