package com.example.easyjob.user

import android.content.ContentValues
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

        walletRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val wallet = mutableData.getValue(WalletData::class.java)

                if(wallet == null) {
                    // 如果钱包为空，返回事务成功，不做任何修改
                    return Transaction.success(mutableData)
                }
                Log.d("Wallet", "Wallet $wallet")

                val balance = wallet.total_balance

                if ((balance ?: 0f) >= (amount ?: 0f)) {
                    val newBalance = balance!! - amount!!
                    wallet.total_balance = newBalance
                    mutableData.value = wallet
                    return Transaction.success(mutableData)
                }
                return Transaction.success(mutableData)
            }
            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    // 事务失败
                    Log.e(ContentValues.TAG, "Transaction failed: ${error.message}")
                    if (error.code == DatabaseError.OVERRIDDEN_BY_SET) {
                        // 数据被覆盖，需要重新运行事务
                        Log.w(ContentValues.TAG, "Transaction was overridden by a subsequent set, retrying...")
                        // 重新运行事务
                    }
                } else if (committed) {
                    // 事务成功，更新UI或执行其他操作
                    Log.d(ContentValues.TAG, "Transaction successful")

                }
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
            generateTransactionHistory(walletId)
            requireActivity().onBackPressed()
            Toast.makeText(requireContext(),getString(R.string.success_withdrawal),Toast.LENGTH_SHORT).show()
        }
    }
}