package com.mib.spinner

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/**
 *  author : cengyimou
 *  date : 2023/9/8 15:43
 *  description :
 */
abstract class MaterialSpinnerBaseAdapter<T>(context: Context) : BaseAdapter() {
	private val context: Context
	private var selectedIndex = 0
	private var textColor = 0
	private var backgroundSelector = 0

	init {
		this.context = context
	}

	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
		var convertView = convertView
		val textView: TextView
		val tvContentView: TextView
		if (convertView == null) {
			val inflater = LayoutInflater.from(context)
			convertView = inflater.inflate(R.layout.ms__list_item, parent, false)
			textView = convertView.findViewById<View>(R.id.tv_tinted_spinner) as TextView
			textView.setTextColor(textColor)
			if (backgroundSelector != 0) {
				textView.setBackgroundResource(backgroundSelector)
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				val config = context.resources.configuration
				if (config.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
					textView.textDirection = View.TEXT_DIRECTION_RTL
				}
			}
			convertView.tag = ViewHolder(textView)
		} else {
			textView = (convertView.tag as ViewHolder).textView
			tvContentView = (convertView.tag as ViewHolder).textView
		}
		textView.text = getItemText(position)
		return convertView
	}

	fun getItemText(position: Int): String {
		return (getItem(position) as SpinnerItem).value.toString()
	}

	fun getSelectedIndex(): Int {
		return selectedIndex
	}

	fun notifyItemSelected(index: Int) {
		selectedIndex = index
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	abstract override fun getItem(position: Int): T
	abstract override fun getCount(): Int
	abstract operator fun get(position: Int): T
	abstract fun getItems(): List<T>?
	fun setTextColor(@ColorInt textColor: Int): MaterialSpinnerBaseAdapter<T> {
		this.textColor = textColor
		return this
	}

	fun setBackgroundSelector(@DrawableRes backgroundSelector: Int): MaterialSpinnerBaseAdapter<T> {
		this.backgroundSelector = backgroundSelector
		return this
	}

	private class ViewHolder(val textView: TextView)
}