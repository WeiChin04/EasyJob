package com.example.easyjob.employer

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.R
import com.example.easyjob.TransactionHistoryData
import com.example.easyjob.WalletData
import com.example.easyjob.databinding.FragmentEmployerCashOutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class EmployerCashOut : Fragment() {

    private var _binding: FragmentEmployerCashOutBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmployerCashOutBinding.inflate(inflater,container,false)
        //action bar
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController, appBarConfiguration)
        //hide nav bar
        (activity as EmployerHome).hideBottomNavigationView()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val employerId = auth.currentUser?.uid.toString()
        var walletId = ""

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

        Log.d("walletID","$walletId")
        binding.btnConfirm.setOnClickListener {
            validation(walletId)
        }

        return binding.root
    }

    private fun cashOut(walletId: String) {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val amount = binding.etAmount.text.toString().toFloatOrNull()

        val walletRef = FirebaseDatabase.getInstance().getReference("Wallets/$walletId")

        walletRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val wallet = snapshot.getValue(WalletData::class.java)
                Log.d("WalletCheck","Wallet $wallet" )
                if (wallet != null) {
                    val balance = wallet.total_balance

                    if ((balance ?: 0f) >= (amount ?: 0f)) {
                        val newBalance = balance!! - amount!!
                        wallet.total_balance = newBalance
                        walletRef.setValue(wallet)
                        generateTransactionHistory(walletId)
                    }else{
                        Log.d("Wallet","Wallet $wallet" )
                        val message = "Your balance is insufficient to complete the withdrawal. Please top up your balance."
//                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }

                }else{
                    // show a message to indicate that the user does not have a wallet
                    Log.d("Wallet","wallet details； $wallet" )
                    val message = "Unable to complete the withdrawal. Please try again later."
//                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun generateTransactionHistory(walletId: String) {

        val amount = binding.etAmount.text.toString().toFloatOrNull()
        val title = "Withdrawal"
        val bankAcc = binding.etBankAccount.text.toString()

        val currentTimeInMS = System.currentTimeMillis().toString()
        val status = "Successful"
        val transactionNo = UUID.randomUUID().toString()
        val historyType = "co"

        val newTransaction = TransactionHistoryData(
            title,
            bankAcc,
            amount,
            currentTimeInMS,
            status,
            transactionNo,
            historyType
        )

        // 获取 "Wallets/$walletId/History" 节点的引用
        val historyRef = FirebaseDatabase.getInstance().getReference("WalletHistory/$walletId")

        // 在 "Wallets/$walletId/History" 节点下生成一个唯一的子节点
        val newHistoryRef = historyRef.push()

        // 设置子节点的值为新的 reload history 信息
        newHistoryRef.setValue(newTransaction)

    }

    private fun validation(walletId: String) {
        val bankAcc = binding.etBankAccount.text
        val amount = binding.etAmount.text

        if(bankAcc.isEmpty()){
            binding.etBankAccount.error = getString(R.string.enter_bank_acc)
        }
        if(amount.isEmpty()){
            binding.etAmount.error = getString(R.string.hint_enter_amount)
        }

        if(bankAcc.isEmpty()||amount.isEmpty()){
            Toast.makeText(requireContext(),getString(R.string.ensure_fill_correct),Toast.LENGTH_SHORT).show()
        }else{
            cashOut(walletId)
//            generateTransactionHistory(walletId)
            requireActivity().onBackPressed()
            Toast.makeText(requireContext(),getString(R.string.success_withdrawal),Toast.LENGTH_SHORT).show()
        }
    }

}
