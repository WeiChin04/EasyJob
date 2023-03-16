package com.example.easyjob.user

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.example.easyjob.R

class CustomSpinner(context: Context, private val itemList: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, itemList) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val selectedPositions = mutableSetOf<Int>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.spinner_jobtype, parent, false)
        val checkBox = view.findViewById<CheckBox>(R.id.cb_item)
        checkBox.text = itemList[position]
        checkBox.isChecked = selectedPositions.contains(position)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.spinner_jobtype, parent, false)
        val checkBox = view.findViewById<CheckBox>(R.id.cb_item)
        checkBox.text = itemList[position]
        checkBox.isChecked = selectedPositions.contains(position)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPositions.add(position)
            } else {
                selectedPositions.remove(position)
            }
        }
        return view
    }

    fun getSelectedPositions(): Set<Int> {
        return selectedPositions
    }
}