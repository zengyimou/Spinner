package com.mib.spinner

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.PopupWindow
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

/**
 *  author : cengyimou
 *  date : 2023/9/8 15:41
 *  description :
 */
class MaterialSpinner : AppCompatTextView {
	private var onNothingSelectedListener: OnNothingSelectedListener? = null
	private var onItemSelectedListener: OnItemSelectedListener? = null
	private var adapter: MaterialSpinnerBaseAdapter<SpinnerItem>? = null //listView的适配
	private var popupWindow: PopupWindow? = null //使用popupWindow控件 样式
	private var listView: ListView? = null //布局
	private var arrowDrawable: Drawable? = null //箭头布局 视图
	private var hideArrow = false //xml中arrow是否隐藏，默认不隐藏false
	private var nothingSelected = false
	private var popupWindowMaxHeight = 0 //spinner下拉框整体高度 最大高度
	private var popupWindowHeight = 0 //spinner下拉框整体高度
	private var selectedIndex = 0
	private var backgroundColor = 0
	private var backgroundSelector = 0
	private var arrowColor = 0
	private var arrowColorDisabled = 0
	private var textColor = 0 //字体颜色
	private var context: Context

	constructor(context: Context) : super(context) {
		this.context = context
		init(context, null)
	}

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
		this.context = context
		init(context, attrs)
	}

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		this.context = context
		init(context, attrs)
	}

	/**
	 * 构建
	 *
	 * @param context
	 * @param attrs
	 */
	private fun init(context: Context, attrs: AttributeSet?) {
		//获取attrs.xml中设置的参数
		val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner)

		//默认色设置
		val defaultColor = textColors.defaultColor
		val rtl: Boolean = Utils.isRtl(context)
		try {
			//背景色 默认白色
			backgroundColor = typedArray.getColor(R.styleable.MaterialSpinner_ms_background_color, Color.WHITE)
			//背景 选中色
			backgroundSelector = typedArray.getResourceId(R.styleable.MaterialSpinner_ms_background_selector, 0)
			textColor = typedArray.getColor(R.styleable.MaterialSpinner_ms_text_color, defaultColor)
			arrowColor = typedArray.getColor(R.styleable.MaterialSpinner_ms_arrow_tint, textColor)
			hideArrow = typedArray.getBoolean(R.styleable.MaterialSpinner_ms_hide_arrow, false)
			popupWindowMaxHeight = typedArray.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_popupwindow_maxheight, 0)
			popupWindowHeight =
				typedArray.getLayoutDimension(R.styleable.MaterialSpinner_ms_popupwindow_height, WindowManager.LayoutParams.WRAP_CONTENT)
			arrowColorDisabled = Utils.lighter(arrowColor, 0.8f)
		} finally {
			typedArray.recycle()
		}

		//设置字体显示 居中
		val resources = resources
		var left = resources.getDimensionPixelSize(R.dimen.ms_padding_left)
		var right = resources.getDimensionPixelSize(R.dimen.ms_padding_right)
		if (rtl) {
			right = resources.getDimensionPixelSize(R.dimen.ms_padding_left)
		} else {
			left = resources.getDimensionPixelSize(R.dimen.ms_padding_right)
		}
		gravity = Gravity.CENTER_VERTICAL or Gravity.START
		isClickable = true
		setPadding(left, 0, right, 0)

		//设置 在界面中的显示样式，以及点击样式
		setBackgroundResource(R.drawable.ms_selector_style)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && rtl) {
			layoutDirection = LAYOUT_DIRECTION_RTL
			textDirection = TEXT_DIRECTION_RTL
		}
		/**设置箭头布局
		 *
		 */
		if (!hideArrow) {
			arrowDrawable = ContextCompat.getDrawable(context, R.drawable.ms_arrow)!!.mutate()
			arrowDrawable!!.setColorFilter(arrowColor, PorterDuff.Mode.SRC_IN)
			if (rtl) {
				setCompoundDrawablesWithIntrinsicBounds(arrowDrawable, null, null, null)
			} else {
				setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null)
			}
		}

		//创建布局 使用listView控件展示items
		listView = ListView(context)
		listView!!.id = id
		listView!!.divider = null
		listView!!.itemsCanFocus = true
		listView!!.onItemClickListener =
			OnItemClickListener { parent, view, position, id -> //                if (position >= selectedIndex && position < adapter.getCount()) {
				//                    position++;
				//                }
				selectedIndex = position
				nothingSelected = false
				val item: SpinnerItem? = adapter?.get(position)
				adapter?.notifyItemSelected(position)
				text = item?.value.toString()
				collapse()
				if (onItemSelectedListener != null && item != null) {
					onItemSelectedListener!!.onItemSelected(this@MaterialSpinner, position, id, item)
				}
			}

		//使用PopupWindow控件承载数据
		popupWindow = PopupWindow(context)
		popupWindow!!.contentView = listView //
		popupWindow!!.isOutsideTouchable = true
		popupWindow!!.isFocusable = true

		//设置背景色
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			popupWindow!!.elevation = 16f //设置阴影
			popupWindow!!.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ms_popwindow_bg)) // R.drawable.ms__drawable
		} else {
			popupWindow!!.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ms_popwindow_bg))
		}

		//设置背景
		if (backgroundColor != Color.WHITE) { // default color is white
			setBackgroundColor(backgroundColor)
		} else if (backgroundSelector != 0) {
			//改变最底层颜色
			setBackgroundResource(backgroundSelector)
		}
		//数据显示颜色
		if (textColor != defaultColor) {
			setTextColor(textColor)
		}
		popupWindow!!.setOnDismissListener {
			if (nothingSelected && onNothingSelectedListener != null) {
				onNothingSelectedListener!!.onNothingSelected(this@MaterialSpinner)
			}
			if (!hideArrow) {
				animateArrow(false)
			}
		}
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		popupWindow!!.width = MeasureSpec.getSize(widthMeasureSpec)
		popupWindow!!.height = calculatePopupWindowHeight()
		if (adapter != null) {
			val currentText = text
			var longestItem = currentText.toString()
			val count = adapter?.count?: 0
			for (i in 0 until count) {
				val itemText: String = adapter?.getItemText(i)?: ""
				if (itemText.length > longestItem.length) {
					longestItem = itemText
				}
			}
			text = longestItem
			super.onMeasure(widthMeasureSpec, heightMeasureSpec)
			text = currentText
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		}
	}

	/**
	 * 处理分发，该处处理简单，只对ACTION_UP做简单处理
	 *
	 * @param event
	 * @return
	 */
	override fun onTouchEvent(event: MotionEvent): Boolean {
		if (event.action == MotionEvent.ACTION_UP) {
			if (isEnabled && isClickable) {
				if (!popupWindow!!.isShowing) {
					expand()
				} else {
					collapse()
				}
			}
		}
		return super.onTouchEvent(event)
	}

	/**
	 * 重写
	 *
	 * @param color
	 */
	override fun setBackgroundColor(color: Int) {
		backgroundColor = color
		val background = background
		if (background is StateListDrawable) { // pre-L
			try {
				val getStateDrawable = StateListDrawable::class.java.getDeclaredMethod("getStateDrawable", Int::class.javaPrimitiveType)
				if (!getStateDrawable.isAccessible) getStateDrawable.isAccessible = true
				val colors = intArrayOf(Utils.darker(color, 0.85f), color)
				for (i in colors.indices) {
					val drawable = getStateDrawable.invoke(background, i) as ColorDrawable
					drawable.color = colors[i]
				}
			} catch (e: Exception) {
				Log.e("MaterialSpinner", "Error setting background color", e)
			}
		} else background?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
		popupWindow!!.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
	}

	/**
	 * 重写
	 *
	 * @param color
	 */
	override fun setTextColor(color: Int) {
		textColor = color
		super.setTextColor(color)
	}

	/**
	 * 保存状态
	 *
	 * @return
	 */
	override fun onSaveInstanceState(): Parcelable {
		val bundle = Bundle()
		bundle.putParcelable("state", super.onSaveInstanceState())
		bundle.putInt("selected_index", selectedIndex)
		if (popupWindow != null) {
			bundle.putBoolean("is_popup_showing", popupWindow!!.isShowing)
			collapse()
		} else {
			bundle.putBoolean("is_popup_showing", false)
		}
		return bundle
	}

	/**
	 * 获取状态
	 *
	 * @param savedState
	 */
	override fun onRestoreInstanceState(savedState: Parcelable?) {
		var savedState = savedState
		if (savedState is Bundle) {
			val bundle = savedState
			selectedIndex = bundle.getInt("selected_index")
			if (adapter != null) {
				text = adapter?.get(selectedIndex)?.value.toString()
				adapter?.notifyItemSelected(selectedIndex)
			}
			if (bundle.getBoolean("is_popup_showing")) {
				if (popupWindow != null) {
					// Post the show request into the looper to avoid bad token exception
					post { expand() }
				}
			}
			savedState = bundle.getParcelable("state")
		}
		super.onRestoreInstanceState(savedState)
	}

	override fun setEnabled(enabled: Boolean) {
		super.setEnabled(enabled)
		if (arrowDrawable != null) {
			arrowDrawable!!.setColorFilter(if (enabled) arrowColor else arrowColorDisabled, PorterDuff.Mode.SRC_IN)
		}
	}

	/**
	 * @return the selected item position
	 */
	fun getSelectedIndex(): Int {
		return selectedIndex
	}

	/**
	 * Set the default spinner item using its index
	 * 初始界面 常用
	 *
	 * @param position the item's position
	 */
	fun setSelectedIndex(position: Int) {
		if (adapter != null) {
			val count = adapter?.count ?: 0
			if (position in 0..count) {
				adapter?.notifyItemSelected(position)
				selectedIndex = position
				text = adapter?.get(position)?.value
			} else {
				throw IllegalArgumentException("Position must be lower than adapter count!")
			}
		}
	}

	/**
	 * Set the dropdown items
	 *
	 * @param items A list of items
	 * @param <T>   The item type
	</T> */
	fun <T> setItems(vararg items: T) {
		setItems(listOf(*items))
	}

	/**
	 * Set the dropdown items
	 *
	 * @param items A list of items
	 * @param <T>   The item type
	</T> */
	fun setItems(items: List<SpinnerItem>) {
		adapter = MaterialSpinnerAdapter(getContext(), items)
			.setBackgroundSelector(backgroundSelector)
			.setTextColor(textColor)
		setAdapterInternal(adapter!!)
	}

	/**
	 * Get the list of items in the adapter
	 *
	 * @param <T> The item type
	 * @return A list of items or `null` if no items are set.
	</T> */
	fun getItems(): List<SpinnerItem>? {
		return if (adapter == null) {
			null
		} else adapter?.getItems()
	}

	/**
	 * Set a custom adapter for the dropdown items
	 *
	 * @param adapter The list adapter
	 */
	fun setAdapter(adapter: ListAdapter) {
		this.adapter = MaterialSpinnerAdapterWrapper<SpinnerItem>(getContext(), adapter).setBackgroundSelector(backgroundSelector)
			.setTextColor(textColor)
		setAdapterInternal(this.adapter!!)
	}

	/**
	 * Set the custom adapter for the dropdown items
	 *
	 * @param adapter The adapter
	 * @param <T>     The type
	</T> */
	fun <T> setAdapter(adapter: MaterialSpinnerAdapter<SpinnerItem>) {
		this.adapter = adapter
		this.adapter?.setTextColor(textColor)
		this.adapter?.setBackgroundSelector(backgroundSelector)
		setAdapterInternal(adapter)
	}

	/**
	 * 数据绑定+显示
	 *
	 * @param adapter
	 */
	private fun setAdapterInternal(adapter: MaterialSpinnerBaseAdapter<SpinnerItem>) {
		listView!!.adapter = adapter
		if (selectedIndex >= adapter.count) {
			selectedIndex = 0
		}
		text = if (adapter.count > 0) {
			adapter[selectedIndex].value
		} else {
			""
		}
	}

	/**
	 * 展开
	 */
	fun expand() {
		if (!hideArrow) {
			animateArrow(true)
		}
		nothingSelected = true
		popupWindow!!.showAsDropDown(this)
	}

	/**
	 * 收起
	 */
	fun collapse() {
		if (!hideArrow) {
			animateArrow(false)
		}
		popupWindow!!.dismiss()
	}

	/**
	 * Set the tint color for the dropdown arrow
	 *
	 * @param color the color value
	 */
	fun setArrowColor(@ColorInt color: Int) {
		arrowColor = color
		arrowColorDisabled = Utils.lighter(arrowColor, 0.8f)
		if (arrowDrawable != null) {
			arrowDrawable!!.setColorFilter(arrowColor, PorterDuff.Mode.SRC_IN)
		}
	}

	@SuppressLint("ObjectAnimatorBinding")
	private fun animateArrow(shouldRotateUp: Boolean) {
		val start = if (shouldRotateUp) 0 else 10000
		val end = if (shouldRotateUp) 10000 else 0
		val animator = ObjectAnimator.ofInt(arrowDrawable, "level", start, end)
		animator.start()
	}

	/**
	 * 计算popupWindow控件 弹窗的高度
	 *
	 * @return
	 */
	private fun calculatePopupWindowHeight(): Int {
		if (adapter == null) {
			return WindowManager.LayoutParams.WRAP_CONTENT
		}
		//计算出listView的总高度
		val listViewHeight: Float = adapter?.count?.times(resources.getDimension(R.dimen.ms_item_height)) ?: 0F

		//如果xml布局中设置了最高高度，且listViewHeight高度满足，优先使用最高高度
		if (popupWindowMaxHeight > 0 && listViewHeight > popupWindowMaxHeight) {
			return popupWindowMaxHeight
		} else if (popupWindowHeight != WindowManager.LayoutParams.MATCH_PARENT && popupWindowHeight != WindowManager.LayoutParams.WRAP_CONTENT && popupWindowHeight <= listViewHeight) {
			return popupWindowHeight
		}
		return WindowManager.LayoutParams.WRAP_CONTENT
	}

	/**
	 * Get the [PopupWindow].
	 *
	 * @return The [PopupWindow] that is displayed when the view has been clicked.
	 */
	fun getPopupWindow(): PopupWindow? {
		return popupWindow
	}

	/**
	 * Get the [ListView] that is used in the dropdown menu
	 *
	 * @return the ListView shown in the PopupWindow.
	 */
	fun getListView(): ListView? {
		return listView
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this view has been selected.
	 *
	 * @param <T> Adapter item type
	</T> */
	interface OnItemSelectedListener {
		/**
		 *
		 * Callback method to be invoked when an item in this view has been selected. This callback is invoked only when
		 * the newly selected position is different from the previously selected position or if there was no selected
		 * item.
		 *
		 * @param view     The [MaterialSpinner] view
		 * @param position The position of the view in the adapter
		 * @param id       The row id of the item that is selected
		 * @param item     The selected item
		 */
		fun onItemSelected(view: MaterialSpinner?, position: Int, id: Long, item: SpinnerItem)
	}

	/**
	 * Register a callback to be invoked when an item in the dropdown is selected.
	 *
	 * @param onItemSelectedListener The callback that will run
	 */
	fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener?) {
		this.onItemSelectedListener = onItemSelectedListener
	}

	/**
	 * Interface definition for a callback to be invoked when the dropdown is dismissed and no item was selected.
	 */
	interface OnNothingSelectedListener {
		/**
		 * Callback method to be invoked when the [PopupWindow] is dismissed and no item was selected.
		 *
		 * @param spinner the [MaterialSpinner]
		 */
		fun onNothingSelected(spinner: MaterialSpinner?)
	}

	/**
	 * Register a callback to be invoked when the [PopupWindow] is shown but the user didn't select an item.
	 *
	 * @param onNothingSelectedListener the callback that will run
	 */
	fun setOnNothingSelectedListener(onNothingSelectedListener: OnNothingSelectedListener?) {
		this.onNothingSelectedListener = onNothingSelectedListener
	}
}