package com.mib.spinner

import androidx.annotation.Keep

/**
 *  author : cengyimou
 *  date : 2023/9/8 15:53
 *  description :
 */
@Keep
data class SpinnerItem(
	var key: String? = null,
	var value: String? = null
)