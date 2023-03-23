package com.example.easyjob.employer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

        val currentTimeMills = currentAnalysis.lastClickTime!!.toLong()
        val date = Date(currentTimeMills)
        val dateFormat = SimpleDateFormat("HH:mm , dd/MMM/yyyy", Locale.getDefault())
        val currentTime = dateFormat.format(date)

        holder.lastViewAt.text = currentTime
        holder.totalView.text = currentAnalysis.clickCount.toString()
        holder.jobAnalysisTitle.text = currentJob.jobTitle.toString()


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


    }
}