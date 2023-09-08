package com.mib.spinner

import android.content.Context

/**
 *  author : cengyimou
 *  date : 2023/9/8 15:43
 *  description :
 */
class MaterialSpinnerAdapter<T>(context: Context, items: List<T>) : MaterialSpinnerBaseAdapter<T>(context) {
	private val items: List<T>

	init {
		this.items = items
	}

	override fun getCount(): Int {
		return items.size
	}

	override fun getItem(position: Int): T {
//        if (position >= getSelectedIndex()) {
//            return items.get(position + 1);
//        } else {
//            return items.get(position);
//        }
		return items[position]
	}

	override operator fun get(position: Int): T {
		return items[position]
	}

	override fun getItems(): List<T> {
		return items
	}
}