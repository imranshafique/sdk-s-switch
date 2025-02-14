package com.msn.dataselectionviewpager.SelectedURis

import android.content.Context
import android.content.SharedPreferences

class ConstantVariables(context: Context)  {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefrences", Context.MODE_PRIVATE)


    var permissionCounter: Int
        get() = sharedPreferences.getInt("permissionscounter", 0)
        set(value) = sharedPreferences.edit().putInt("permissionscounter", value).apply()
}