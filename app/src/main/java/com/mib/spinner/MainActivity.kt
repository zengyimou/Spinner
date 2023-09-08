package com.mib.spinner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

	private lateinit var materialSpinner: MaterialSpinner
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		findViewById()
	}

	private fun findViewById() {
		materialSpinner = findViewById(R.id.spinner)
		materialSpinner.apply {
			val items = mutableListOf<SpinnerItem>()
			items.add(SpinnerItem("1", "Test1"))
			items.add(SpinnerItem("2", "Test2"))
			setItems(items)
			setSelectedIndex(0)
			setOnItemSelectedListener(object: MaterialSpinner.OnItemSelectedListener{
				override fun onItemSelected(view: MaterialSpinner?, position: Int, id: Long, item: SpinnerItem) {
					Log.d("", "position: $position")
				}
			})
		}

	}

}