package com.example.easyjob

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.employer.JobData
import com.example.easyjob.user.UserJobAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WalletAdapter(private var transactionHistoryList: ArrayList<TransactionHistoryData>) : RecyclerView.Adapter<WalletAdapter.transactionHistoryViewHolder>(){

    private lateinit var dbRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): transactionHistoryViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.transaction_history_item,
            parent, false
        )
        return transactionHistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: transactionHistoryViewHolder, position: Int) {
        val currentItem = transactionHistoryList[position]
        val title = currentItem.transactionTitle
        val detail = currentItem.paymentDetail
        val amount = currentItem.amount


        holder.transactionTitle.text = title
        holder.detail.text = detail
        holder.amount.text = amount.toString()

        val currentTimeMillis = if (currentItem.date != null) {
            currentItem.date.toLong()
        } else {
            0L
        }
        val setDate = Date(currentTimeMillis)
        val dateFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
        val currentTime = dateFormat.format(setDate)

        holder.date.text = currentTime

    }

    override fun getItemCount(): Int {
        return transactionHistoryList.size
    }

    class transactionHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val transactionTitle: TextView = itemView.findViewById(R.id.tvTransactionTitle)
        val detail: TextView = itemView.findViewById(R.id.tvDetail)
        val amount: TextView = itemView.findViewById(R.id.tvAmount)
        val date: TextView = itemView.findViewById(R.id.tvTransactionDate)
    }
}