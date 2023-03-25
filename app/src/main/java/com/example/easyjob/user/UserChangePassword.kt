package com.example.easyjob.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentFavouriteJobBinding
import com.example.easyjob.databinding.FragmentUserChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class UserChangePassword : Fragment() {

    private var _binding : FragmentUserChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserChangePasswordBinding.inflate(inflater, container, false)

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

        binding.btnUpdatePassword.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            checkEmptyFill()
        }

        return binding.root
    }

    private fun checkEmptyFill() {
        val currentPassword = binding.etCurrentPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmNewPassword.text.toString().trim()

        val countError = 0

        if (currentPassword.isEmpty()) {
            binding.etCurrentPassword.error = getString(R.string.current_password_empty_error)
            binding.etCurrentPassword.requestFocus()
            countError +1
            binding.progressBar.visibility = View.GONE
            return
        }

        if (newPassword.isEmpty()) {
            binding.etNewPassword.error = getString(R.string.new_password_empty_error)
            binding.etNewPassword.requestFocus()
            countError +1
            binding.progressBar.visibility = View.GONE
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmNewPassword.error = getString(R.string.confirm_password_empty_error)
            binding.etConfirmNewPassword.requestFocus()
            countError +1
            binding.progressBar.visibility = View.GONE
            return
        }

        if(currentPassword == newPassword)
        {
            binding.etNewPassword.error =getString(R.string.crPass_and_newPass_same)
            binding.etNewPassword.requestFocus()
            countError +1
            binding.progressBar.visibility = View.GONE
        }else{
            if(countError == 0){
                validatePassword()
            }
        }

    }

    private fun validatePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmNewPassword.text.toString()

        // 检查当前密码是否正确
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user!!.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), getString(R.string.newPass_crPass_different), Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                if (newPassword.length < 7) {
                    Toast.makeText(requireContext(), getString(R.string.password_must_longer_than_7_char), Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                binding.progressBar.visibility = View.GONE
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), getString(R.string.password_update_failed), Toast.LENGTH_SHORT).show()
                    }

            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.current_password_incorrect), Toast.LENGTH_SHORT).show()
            }
    }


    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showConfirmationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(getString(R.string.messageDialog_confirm))
        alertDialog.setMessage(R.string.messageDialog_cancel_change_password)
        alertDialog.setPositiveButton(getString(R.string.messageDialog_yes)) { _, _ ->
            requireActivity().onBackPressed()
        }
        alertDialog.setNegativeButton(getString(R.string.messageDialog_no)) { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()
    }


}
