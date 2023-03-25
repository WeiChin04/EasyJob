package com.example.easyjob.employer

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.MainActivity
import com.example.easyjob.MyPreference
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEmployerChangeLanguageBinding
import com.example.easyjob.databinding.FragmentUserChangeLanguageBinding
import com.example.easyjob.user.UserHome


class EmployerChangeLanguage : Fragment() {

    lateinit var myPreference: MyPreference
    private var _binding: FragmentEmployerChangeLanguageBinding? =null
    private val binding get() = _binding!!
    private val languageList:Array<String> = arrayOf("English","zh")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEmployerChangeLanguageBinding.inflate(inflater,container,false)

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
        (activity as EmployerHome).hideBottomNavigationView()

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