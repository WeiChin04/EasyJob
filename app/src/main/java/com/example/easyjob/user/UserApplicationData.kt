package com.example.easyjob.user


data class UserApplicationData(
    var jobId: String? =null,
    var applicantId: String? =null,
    var status: String? = null,
    var appliedAt: String? =null,
    var rejectAt: String? = null,
    var approvedAt: String? =null,
    var applicationId: String? = null
)
