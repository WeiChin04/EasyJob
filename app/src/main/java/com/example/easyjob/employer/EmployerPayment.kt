package com.example.easyjob.employer

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.TransactionHistoryData
import com.example.easyjob.R
import com.example.easyjob.WalletData
import com.example.easyjob.databinding.FragmentEmployerPaymentBinding
import com.example.easyjob.user.UserJobAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class EmployerPayment : Fragment() {

    private var _binding: FragmentEmployerPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var walletId: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmployerPaymentBinding.inflate(inflater, container, false)

        //hide nav bar
        (activity as EmployerHome).hideBottomNavigationView()

        //action bar
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(
            activity as AppCompatActivity,
            navController,
            appBarConfiguration
        )

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        getEmployerWalletId()
        checkvalidation()

        return binding.root
    }

    private fun getEmployerWalletId() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val employerId = auth.currentUser?.uid.toString()

        dbRef = database.getReference("Employers").child(employerId)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val employer = snapshot.getValue(EmployerData::class.java)
                walletId = employer?.walletId.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to read Data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateReloadHistory(walletId: String) {

        val reload = "Reload"
        val paymentDetail = "Card"
        val currentTimeInMS = System.currentTimeMillis().toString()
        val amount = arguments?.getString("amount").toString().toFloatOrNull()
        val status = "Successful"
        val transactionNo = UUID.randomUUID().toString()

        val newTransaction = TransactionHistoryData(
            reload,
            paymentDetail,
            amount,
            currentTimeInMS,
            status,
            transactionNo,
            "ci"
        )

        // 获取 "Wallets/$walletId/History" 节点的引用
        val historyRef = FirebaseDatabase.getInstance().getReference("WalletHistory/$walletId")

        // 在 "Wallets/$walletId/History" 节点下生成一个唯一的子节点
        val newHistoryRef = historyRef.push()

        // 设置子节点的值为新的 reload history 信息
        newHistoryRef.setValue(newTransaction)
    }

    private fun totalBalance() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        getEmployerWalletId()
        val amount = arguments?.getString("amount").toString().toFloatOrNull()
        val totalDeposit = String.format("%.2f", amount)
        val walletRef = FirebaseDatabase.getInstance().getReference("Wallets/$walletId")

        walletRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val wallet = snapshot.getValue(WalletData::class.java)
                if (wallet != null) {
                    val currentBalance = wallet.total_balance ?: 0f
                    val newBalance = currentBalance + amount!!

                    wallet.total_balance = newBalance
                    Log.d("CurrentBalance", "CurrentBalance $currentBalance")
                    Log.d("deposit", "amount RM: $amount")

                    walletRef.setValue(wallet).addOnSuccessListener {
                        // Deposit successfully saved to wallet balance
                        Log.d("Wallet", "Deposit saved to wallet balance")
                        generateReloadHistory(walletId.toString())
                    }.addOnFailureListener { exception ->
                        // Failed to save deposit to wallet balance
                        Log.w("Wallet", "Error saving deposit to wallet balance", exception)
                        Toast.makeText(
                            requireContext(),
                            "Failed to save deposit to wallet balance. Please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // Wallet not found
                    Log.w("Wallet", "Wallet with ID $walletId not found")
                    Toast.makeText(
                        requireContext(),
                        "Wallet not found. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun checkvalidation() {
        getEmployerWalletId()
        binding.btnConfirm.setOnClickListener {
            if (binding.etCardNumber.text.isEmpty()) {
                binding.etCardNumber.error = getString(R.string.card_number_empty)
            }
            if (binding.etExpiryDate.text.isEmpty()) {
                binding.etExpiryDate.error = getString(R.string.expiry_empty)
            }
            if (binding.etCvv.text.isEmpty()) {
                binding.etCvv.error = getString(R.string.cvv_empty)
            }
            if (binding.etCardNumber.text.isEmpty() || binding.etExpiryDate.text.isEmpty() || binding.etCvv.text.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.ensure_fill_correct),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                totalBalance()
                it.findNavController().navigate(R.id.action_employerPayment_to_employerMyWallet)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.success_reload),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}