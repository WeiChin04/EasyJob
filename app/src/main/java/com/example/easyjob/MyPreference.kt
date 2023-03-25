package com.example.easyjob

import android.content.Context
import android.content.SharedPreferences

val PREFERENCE_NAME = "SharedPreferenceExample"
val PREFERENCE_LANGUAGE = "Language"

class MyPreference (context: Context){

    val preference: SharedPreferences? = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun getLoginCount() : String{
        return preference?.getString(PREFERENCE_LANGUAGE,"English")!!
    }

    fun setLoginCount(Language:String){
        val editor:SharedPreferences.Editor = preference!!.edit()
        editor.putString(PREFERENCE_LANGUAGE,Language)
        editor.apply()
    }

}