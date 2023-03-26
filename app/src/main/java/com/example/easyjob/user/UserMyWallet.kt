package com.example.easyjob.user

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.R
import com.example.easyjob.TransactionHistoryData
import com.example.easyjob.WalletAdapter
import com.example.easyjob.WalletData
import com.example.easyjob.databinding.FragmentUserMyWalletBinding
import com.example.easyjob.employer.EmployerData
import com.example.easyjob.employer.JobData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class UserMyWallet : Fragment() {

    private var _binding: FragmentUserMyWalletBinding? =null
    private val binding get() = _binding!!
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbWalletRef : DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var WalletHistoryRecyclerView: RecyclerView
    private lateinit var walletHistoryAdapter: WalletAdapter
    private lateinit var jobHistoryArrayList: ArrayList<TransactionHistoryData>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WalletHistoryRecyclerView = binding.walletHistoryList
        WalletHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        WalletHistoryRecyclerView.setHasFixedSize(true)
        jobHistoryArrayList = arrayListOf<TransactionHistoryData>()
        getHistoryData()
        walletHistoryAdapter = WalletAdapter(jobHistoryArrayList)
        WalletHistoryRecyclerView.adapter = walletHistoryAdapter

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserMyWalletBinding.inflate(inflater,container,false)

        (activity as UserHome).showBottomNavigationView()

        getBalance()

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Refresh data here
            refreshData()
        }

        binding.btnCashOut.setOnClickListener {
            it.findNavController().navigate(R.id.action_userMyWallet_to_userCashOut)
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshData() {
        getBalance()
        jobHistoryArrayList.clear()
        WalletHistoryRecyclerView.adapter?.notifyDataSetChanged()
        getHistoryData()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun getHistoryData() {

        val currentId = FirebaseAuth.getInstance().currentUser!!.uid
        Log.d("UserId","UserId details； $currentId")
        val applicantRef = FirebaseDatabase.getInstance().getReference("Users").child(currentId)
        var walletId = ""

        applicantRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserData::class.java)
                if(user != null){
                    walletId = user.walletId.toString()
                    Log.d("WalletId","wallet details； $walletId")
                    val walletRef = FirebaseDatabase.getInstance().getReference("WalletHistory/$walletId")
                    walletRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (historySnapshot in snapshot.children) {
                                    val history = historySnapshot.getValue(TransactionHistoryData::class.java)
                                    Log.d("history","history details； $history")
                                    jobHistoryArrayList.add(history!!)
                                }
                                WalletHistoryRecyclerView.adapter = WalletAdapter(jobHistoryArrayList)
                            } else {
                                binding.etNoHistoryShow.visibility = View.VISIBLE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                            Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getBalance() {

        auth = FirebaseAuth.getInstance()
        val currentId = auth.currentUser!!.uid


        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("Wallets").orderByChild("holder_id").equalTo(currentId).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var wallet: WalletData? = null
                for (data in snapshot.children) {
                    val temp = data.getValue(WalletData::class.java)
                    if (temp?.holder_id == currentId) {
                        wallet = temp
                        break
                    }
                }

                if (wallet != null) {
                    binding.tvShowTotalBalance.text = String.format("%.2f", wallet.total_balance!!.toFloat())
                } else {
                    dbWalletRef = FirebaseDatabase.getInstance().getReference("Wallets")
                    val walletId = UUID.randomUUID().toString()
                    val walletData = WalletData(0f, currentId)
                    dbWalletRef.child(walletId).setValue(walletData)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

}