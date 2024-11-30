package com.blessall05.translator.view.dictionary

import androidx.lifecycle.ViewModel
import com.blessall05.translator.model.data.Word

class DictionaryViewModel : ViewModel() {
    var word: Word? = null
    var showWord = false
    val list: MutableList<Word.History> = mutableListOf()
}