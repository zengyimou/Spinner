package com.mib.spinner

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View

/**
 *  author : cengyimou
 *  date : 2023/9/8 15:45
 *  description :
 */
object Utils {
	/**
	 * Darkens a color by a given factor.
	 *
	 * @param color  the color to darken
	 * @param factor The factor to darken the color.
	 * @return darker version of specified color.
	 */
	fun darker(color: Int, factor: Float): Int {
		return Color.argb(
			Color.alpha(color), Math.max((Color.red(color) * factor).toInt(), 0),
			Math.max((Color.green(color) * factor).toInt(), 0),
			Math.max((Color.blue(color) * factor).toInt(), 0)
		)
	}

	/**
	 * Lightens a color by a given factor.
	 *
	 * @param color  The color to lighten
	 * @param factor The factor to lighten the color. 0 will make the color unchanged. 1 will make the
	 * color white.
	 * @return lighter version of the specified color.
	 */
	fun lighter(color: Int, factor: Float): Int {
		val red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255).toInt()
		val green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255).toInt()
		val blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255).toInt()
		return Color.argb(Color.alpha(color), red, green, blue)
	}

	/**
	 * Check if layout direction is RTL
	 *
	 * @param context the current context
	 * @return `true` if the layout direction is right-to-left
	 */
	fun isRtl(context: Context): Boolean {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
				context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
	}
}