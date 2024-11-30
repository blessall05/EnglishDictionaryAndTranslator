package com.blessall05.translator.view.wordbook

import androidx.lifecycle.ViewModel
import com.blessall05.translator.App.Companion.database
import com.blessall05.translator.model.AppData
import com.blessall05.translator.model.data.Word

class WordViewModel : ViewModel() {
    val list = mutableListOf<Word.Book>()
    var word: Word? = null


    var filter = ""
        set(value) {
            field = value
            refreshList()
        }
    var sortBy = SORT_BY_DEFAULT
        set(value) {
            field = value
            refreshList()
        }
    var isDescending = false
        set(value) {
            field = value
            refreshList()
        }

    fun refreshList() {
        list.clear()
        list.addAll(database.getAllWord(AppData.userId).reversed())
        when (sortBy) {
            SORT_BY_LETTER -> {
                list.sortWith { o1, o2 -> o1.word.compareTo(o2.word, ignoreCase = true) }
            }

            SORT_BY_LENGTH -> {
                list.sortBy { it.word.length }
            }
        }
        if (isDescending) {
            list.reverse()
        }
        if (filter.isNotBlank()) {
            list.retainAll { it.word.contains(filter, true) }
        }
    }

    companion object {
        const val SORT_BY_DEFAULT = 0
        const val SORT_BY_LETTER = 1
        const val SORT_BY_LENGTH = 2
    }
}