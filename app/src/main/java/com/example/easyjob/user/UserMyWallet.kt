package com.example.easyjob.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easyjob.R
import com.example.easyjob.WalletData
import com.example.easyjob.databinding.FragmentUserMyWalletBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserMyWalletBinding.inflate(inflater,container,false)


        getBalance()

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Refresh data here
            refreshData()
        }

        return binding.root
    }

    private fun refreshData() {
        getBalance()
        binding.swipeRefreshLayout.isRefreshing = false
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