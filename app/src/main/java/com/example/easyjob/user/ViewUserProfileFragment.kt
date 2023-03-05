package com.example.easyjob.user

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.R
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
    private lateinit var navController: NavController
    private lateinit var userDataViewModel: UserDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewUserProfileBinding.inflate(inflater, container, false)
        //action bar
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController, appBarConfiguration)

        //hide bottom navigation bar
        (activity as UserHome).hideBottomNavigationView()

        auth = FirebaseAuth.getInstance()

        //get user data from view model
        userDataViewModel = ViewModelProvider(requireActivity())[UserDataViewModel::class.java]

        userDataViewModel.getData(auth.currentUser!!.uid)
        userDataViewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.tvUserFullname.text = userData.name
            binding.tvUserEmail.text = userData.email
            binding.tvUserContact.text = userData.contact
            binding.tvUserJobSalary.text = userData.jobsalary
            binding.tvUserAddress.text = userData.address
            binding.tvEducationLevel.text = userData.education_level
            binding.tvUserAboutMe.text = userData.about_me
        }

        //back to forward page
        binding.btnCancel.setOnClickListener {
            navController.navigateUp()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
//        auth = FirebaseAuth.getInstance()
//        dbref = FirebaseDatabase.getInstance().getReference("Users")
//        uid = auth.currentUser?.uid.toString()
//
//        dbref.child(uid).addValueEventListener(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot){
//                user = snapshot.getValue(UserData::class.java)!!
//                binding.tvUserFullname.text = user.name
//                binding.tvUserEmail.text = user.email
//                binding.tvUserContact.text = user.contact
//                binding.tvUserJobSalary.text = user.jobsalary
//                binding.tvUserAddress.text = user.address
//                binding.tvEducationLevel.text = user.education_level
//                binding.tvUserAboutMe.text = user.about_me
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })

        binding.btnUserEditProfile.setOnClickListener {
            navController = findNavController(binding.btnUserEditProfile)
            navController.navigate(R.id.action_viewUserProfileFragment_to_editUserProfileFragment)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}