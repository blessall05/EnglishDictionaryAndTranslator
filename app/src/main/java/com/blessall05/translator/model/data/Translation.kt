package com.blessall05.translator.model.data

import com.blessall05.translator.App.Companion.app
import com.blessall05.translator.R

data class Translation(
    val srcLanguage: Language,
    val src: String,
    val dstLanguage: Language,
    val dst: String,
) {
    enum class Language(val code: String) {
        AUTO("auto"), //自动检测
        ZH("zh"), //中文
        EN("en"), //英语
        JP("jp"), //日语
        KR("kr"), //韩语
        FR("fr"), //法语
        RU("ru"), //俄语
        ES("es"), //西班牙语
        PT("pt"), //葡萄牙语
        DE("de"), //德语
        AR("ar"), //阿拉伯语
        TR("tr"), //土耳其语
        IT("it"), //意大利语
        VI("vi"); //越南语

        fun entry() = app.resources.getStringArray(R.array.language_from_entries)[ordinal]

        companion object {
            fun fromCode(code: String) =
                entries.firstOrNull { it.code == code }
        }

    }

    data class History(
        val id: Long,
        val srcLanguage: Language,
        val src: String,
        val dstLanguage: Language,
        val dst: String
    )
}