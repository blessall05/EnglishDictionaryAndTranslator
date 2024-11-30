package com.blessall05.translator.util

import java.util.Calendar
import java.util.Locale

object DataUtil {
    fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale("zh", "CN"))
        return formatter.format(date)
    }
}