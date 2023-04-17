package com.example.easyjob.user

import android.content.ContentValues
import android.content.Context
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
import com.example.easyjob.databinding.FragmentUserCashOutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class UserCashOut : Fragment() {

    private var _binding: FragmentUserCashOutBinding? =null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var activityContext: Context?= null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserCashOutBinding.inflate(inflater,container,false)

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
        (activity as UserHome).hideBottomNavigationView()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid.toString()
        var walletId = ""

        dbRef = database.getReference("Users").child(userId)

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserData::class.java)
                walletId = user?.walletId.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activityContext, "Failed to read Data", Toast.LENGTH_SHORT).show()
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
                        val message = "Successful withdrawal money"
                        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
                    }else{
                        Log.d("Wallet","Wallet $wallet" )
                        val message = "Your balance is insufficient to complete the withdrawal. " +
                                      "Please top up your balance."
                        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
                    }

                }else{
                    // show a message to indicate that the user does not have a wallet
                    Log.d("Wallet","wallet details； $wallet" )
                    val message = "Unable to complete the withdrawal. Please try again later."
                    Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(activityContext, "Failed to read value", Toast.LENGTH_SHORT).show()
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
//            Toast.makeText(requireContext(),getString(R.string.success_withdrawal),Toast.LENGTH_SHORT).show()
        }
    }

}