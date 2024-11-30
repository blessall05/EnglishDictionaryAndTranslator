package com.blessall05.translator.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

object ThemeUtil {
    fun getColorByAttr(context: Context, @AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}