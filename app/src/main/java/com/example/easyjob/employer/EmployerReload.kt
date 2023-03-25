package com.example.easyjob.employer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEmployerReloadBinding
import com.example.easyjob.databinding.FragmentUserMyWalletBinding


class EmployerReload : Fragment() {

    private var _binding: FragmentEmployerReloadBinding? =null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEmployerReloadBinding.inflate(inflater,container,false)

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

        binding.btnReload50.setOnClickListener {
            binding.etEnterReloadAmount.setText("50")
        }
        binding.btnReload100.setOnClickListener {
            binding.etEnterReloadAmount.setText("100")
        }
        binding.btnReload200.setOnClickListener {
            binding.etEnterReloadAmount.setText("200")
        }
        binding.btnReload300.setOnClickListener {
            binding.etEnterReloadAmount.setText("300")
        }
        binding.btnReload500.setOnClickListener {
            binding.etEnterReloadAmount.setText("500")
        }
        binding.btnReloadOther.setOnClickListener {
            val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            binding.etEnterReloadAmount.requestFocus()
            inputMethodManager.showSoftInput(binding.etEnterReloadAmount, InputMethodManager.SHOW_IMPLICIT)

        }

        return binding.root
    }

}