package com.mib.spinner

import android.content.Context
import android.widget.ListAdapter

/**
 *  author : cengyimou
 *  date : 2023/9/8 15:44
 *  description :
 */
internal class MaterialSpinnerAdapterWrapper<T>(context: Context, toWrap: ListAdapter) :
	MaterialSpinnerBaseAdapter<T>(context) {
	private val listAdapter: ListAdapter

	init {
		listAdapter = toWrap
	}

	override fun getCount(): Int {
		return listAdapter.count - 1
	}

	override fun getItem(position: Int): T {
		return if (position >= getSelectedIndex()) {
			listAdapter.getItem(position + 1) as T
		} else {
			listAdapter.getItem(position) as T
		}
	}

	override operator fun get(position: Int): T {
		return listAdapter.getItem(position) as T
	}

	override fun getItems(): List<T> {
		val items: MutableList<T> = ArrayList()
		for (i in 0 until count) {
			items.add(getItem(i))
		}
		return items
	}
}