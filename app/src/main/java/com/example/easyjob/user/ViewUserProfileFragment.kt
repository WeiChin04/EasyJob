package com.example.easyjob.user

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.databinding.FragmentViewUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class ViewUserProfileFragment : Fragment() {
    private var _binding: FragmentViewUserProfileBinding? = null
    private val binding get()  = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var user: UserData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewUserProfileBinding.inflate(inflater, container, false)
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Users")
        uid = auth.currentUser?.uid.toString()

        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController, appBarConfiguration)


        dbref.child(uid).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                user = snapshot.getValue(UserData::class.java)!!
                binding.tvUserFullname.text = user.name
                binding.tvUserEmail.text = user.email
                binding.tvUserContact.text = user.contact
                binding.tvUserJobSalary.text = user.jobsalary
                binding.tvUserAddress.text = user.address
                binding.tvEducationLevel.text =user.education_level
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}