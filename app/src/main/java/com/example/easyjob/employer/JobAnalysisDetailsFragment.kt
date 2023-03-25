package com.example.easyjob.employer

import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.example.easyjob.databinding.FragmentJobAnalysisDetailsBinding
import java.text.SimpleDateFormat
import java.util.*


class JobAnalysisDetailsFragment : Fragment() {

    private var _binding: FragmentJobAnalysisDetailsBinding? =null
    private val binding get() = _binding!!
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lastAppliedTime = arguments?.getString("last_applied")?.toLongOrNull() ?: 0

        timer = object : CountDownTimer(Long.MAX_VALUE, 60000) { // 每分钟检查一次
            override fun onTick(millisUntilFinished: Long) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - lastAppliedTime
                if (elapsedTime > 604800000) { // if over 7 days
                    showReminder()
                    return
                }
                hideReminder()
            }

            override fun onFinish() {}
        }
        timer?.start()

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentJobAnalysisDetailsBinding.inflate(inflater,container,false)

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
        
        binding.tvADTotalView.text = arguments?.getString("total_click")
        binding.tvADApplied.text = arguments?.getString("total_applied")
        binding.tvADApproved.text = arguments?.getString("total_approved")
        binding.tvADCancelled.text = arguments?.getString("total_cancelled")
        binding.tvADRejected.text = arguments?.getString("total_rejected")
        binding.tvADFavourite.text = arguments?.getString("total_favourite")
        binding.tvADLastUpdate.text = arguments?.getString("last_click")

        binding.btnCloseWarning.setOnClickListener {
            hideReminder()
        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (!preferences.getBoolean("reminder_closed", false)) {
            showReminder()
        } else {
            binding.layoutWarning.visibility = View.GONE
        }
    }


    private fun showReminder() {
        binding.layoutWarning.visibility = View.VISIBLE
    }

    private fun hideReminder() {
        binding.layoutWarning.visibility = View.GONE
    }

}