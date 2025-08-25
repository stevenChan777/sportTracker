package com.example.myruns5

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView

data class SettingItem(val title: String, val subTitle: String, val showCheckbox: Boolean)
// Custom ArrayAdapter for setting items. This adapter is designed to display a list of settings
class SettingsAdapter(context: Context, items: List<SettingItem>) : ArrayAdapter<SettingItem>(context, 0, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.setting_listview, parent, false)
        val item = getItem(position)

        // set the title and subtitle of the setting item
        itemView.findViewById<TextView>(R.id.textViewTitle).text = item?.title
        itemView.findViewById<TextView>(R.id.textViewSubTitle).text = item?.subTitle
        // Determine if the checkbox should be visible based on the item's property
        itemView.findViewById<CheckBox>(R.id.checkBoxOption).visibility = if (item?.showCheckbox == true) View.VISIBLE else View.GONE

        return itemView
    }
}


