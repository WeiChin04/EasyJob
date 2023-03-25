package com.example.easyjob.user

data class UserData(
    var name: String? = null,
    var gender: String? = null,
    var email: String? = null,
    var contact: String? = null,
    var jobsalary: String? = null,
    var address: String? = null,
    var education_level: String? = null,
    var about_me: String? = null,
    var resume: String? = null,
    var profile_image: String? =null,
    val profile_status: String? = null,
    val userId: String? = null,
    val deviceToken: String? =null,
    val walletId: String? =null,
)
