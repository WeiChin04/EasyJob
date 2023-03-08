package com.example.easyjob.user

import java.util.ArrayList


data class UserJobData(
    var employerId: String? =null,
    var jobTitle: String? =null,
    var jobType: ArrayList<String>? = null,
    var jobSalary: String? =null,
    var workingHourStart: String? = null,
    var workingHourEnd: String? = null,
    var workplace: String? = null,
    var jobRequirement: String? = null,
    var jobResponsibilities: String? =null,
    var jobStatus: String? = null,
    var ctr: Int? = null,
    var currentDate: String? =null,
    var jobId: String? =null,
)
