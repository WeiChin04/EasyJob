package com.example.easyjob.employer

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.R

class JobAdapter(private val jobList: ArrayList<JobData>) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.job_item,
        parent, false)
        return JobViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {

        val currentItem = jobList[position]



        holder.datePosted.text = currentItem.currentDate
        holder.jobCTR.text = currentItem.ctr.toString()
        holder.jobTitle.text = currentItem.jobTitle
        holder.jobType.text = currentItem.jobType.toString()
        holder.jobStatus.text = currentItem.jobStatus

        if(holder.jobStatus.text == "Unavailable"){
            holder.jobStatus.setBackgroundColor(Color.GRAY)
        }

        holder.cardView.setOnClickListener{
            val context = holder.itemView.context
            val intent = Intent(context, JobDetailActivity::class.java).apply{
                putExtra("job_title",jobList[position].jobTitle)
            }
            context.startActivity(intent)
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

    }

}