package com.blessall05.translator.model.data

import com.blessall05.translator.App.Companion.app
import com.blessall05.translator.model.AppData
import com.blessall05.translator.model.DatabaseHelper

data class Word(
    val name: String,
    val wordPl: List<String>,
    val wordThird: List<String>,
    val wordPast: List<String>,
    val wordDone: List<String>,
    val wordIng: List<String>,
    val wordEr: List<String>,
    val wordEst: List<String>,
    val phEn: String,
    val phAm: String,
    val phEnMp3: String,
    val phAmMp3: String,
    val means: List<Mean>
) {
    val isInWordBook get() = DatabaseHelper(app).isInWordBook(AppData.userId, name)

    data class Mean(
        val part: String,
        val means: List<String>
    )

    data class History(
        val word: String,
        val means: List<Mean>
    ) {
        val isInWordBook get() = DatabaseHelper(app).isInWordBook(AppData.userId, word)
    }

    data class Book(
        val word: String,
        val pnEn: String,
        val pnAm: String,
        val means: List<Mean>
    ) {
        val isInWordBook get() = DatabaseHelper(app).isInWordBook(AppData.userId, word)
    }
}
