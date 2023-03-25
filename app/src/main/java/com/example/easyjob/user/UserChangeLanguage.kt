package com.example.easyjob.user

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Process.killProcess
import android.os.Process.myPid
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.recreate
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.MainActivity
import com.example.easyjob.MyPreference
import com.example.easyjob.databinding.FragmentUserChangeLanguageBinding
import com.example.easyjob.R
import kotlinx.coroutines.MainScope
import java.util.*
import kotlin.collections.ArrayList


class UserChangeLanguage : Fragment() {

    lateinit var myPreference: MyPreference
    private var _binding: FragmentUserChangeLanguageBinding? =null
    private val binding get() = _binding!!
    private val languageList:Array<String> = arrayOf("English","zh")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserChangeLanguageBinding.inflate(inflater,container,false)

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

        //hide bottom navigation bar
        (activity as UserHome).hideBottomNavigationView()

        myPreference = MyPreference(requireContext())
        binding.spinner.adapter = ArrayAdapter(requireContext(),R.layout.my_simple_list_item,languageList)

        val lang = myPreference.getLoginCount()
        val index = languageList.indexOf(lang)
        if(index >= 0){
            binding.spinner.setSelection(index)
        }

        binding.btnChangeLanguage.setOnClickListener {
            val selectedLanguage = languageList[binding.spinner.selectedItemPosition]
            myPreference.setLoginCount(selectedLanguage)

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.change_language)
                .setMessage(R.string.restart_app_message)
                .setPositiveButton(R.string.messageDialog_yes) { _, _ ->
                    // Restart the app
                    startActivity(Intent(context, MainActivity::class.java))
                }
                .setNegativeButton(R.string.messageDialog_no, null)
                .show()
        }

        return binding.root
    }




}