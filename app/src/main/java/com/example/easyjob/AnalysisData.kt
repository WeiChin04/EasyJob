package com.example.easyjob

data class AnalysisData (

        var clickCount: Int? = null,
        var favouriteCount: Int? = null,
        var lastClickTime: String? = null,
        val lastApplyAt: String? = null,
        val totalApply: Int? =null,
        val totalCancel: Int? =null,
        val totalApproved: Int? =null,
        val totalRejected: Int? =null

)