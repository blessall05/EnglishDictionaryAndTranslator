package com.blessall05.translator.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.blessall05.translator.model.data.Translation
import com.blessall05.translator.model.data.Word
import java.security.MessageDigest

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "UserDatabase"

        private const val TABLE_USERS = "users"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"

        private const val TABLE_WORD_HISTORY = "word_history"
        private const val KEY_WORD_ID = "word_id"
        private const val KEY_SEARCH_WORD = "search_word"
        private const val KEY_WORD_PART = "word_part"
        private const val KEY_WORD_MEAN = "word_mean"

        private const val TABLE_TRANSLATION_HISTORY = "translation_history"
        private const val KEY_TRANSLATION_ID = "translation_id"
        private const val KEY_TRANSLATION_SRC_LANGUAGE = "translation_src_language"
        private const val KEY_TRANSLATION_SRC = "translation_src"
        private const val KEY_TRANSLATION_DST_LANGUAGE = "translation_dst_language"
        private const val KEY_TRANSLATION_RESULT = "translation_result"

        private const val TABLE_WORD_BOOK = "word_book"
        private const val KEY_BOOK_ID = "book_id"
        private const val KEY_WORD = "word"
        private const val KEY_AMERICAN_PRONUNCIATION = "american_pronunciation"
        private const val KEY_BRITISH_PRONUNCIATION = "british_pronunciation"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable =
            ("CREATE TABLE IF NOT EXISTS $TABLE_USERS($KEY_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USERNAME TEXT, $KEY_PASSWORD TEXT)")
        val createSearchHistoryTable =
            ("CREATE TABLE IF NOT EXISTS $TABLE_WORD_HISTORY($KEY_WORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USER_ID INTEGER, $KEY_SEARCH_WORD TEXT, $KEY_WORD_PART TEXT, $KEY_WORD_MEAN TEXT, FOREIGN KEY($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_USER_ID))")
        val createTranslationHistoryTable =
            ("CREATE TABLE IF NOT EXISTS $TABLE_TRANSLATION_HISTORY($KEY_TRANSLATION_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USER_ID INTEGER, $KEY_TRANSLATION_SRC_LANGUAGE TEXT, $KEY_TRANSLATION_SRC TEXT, $KEY_TRANSLATION_DST_LANGUAGE TEXT, $KEY_TRANSLATION_RESULT TEXT, FOREIGN KEY($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_USER_ID))")
        val createWordBookTable =
            ("CREATE TABLE IF NOT EXISTS $TABLE_WORD_BOOK($KEY_BOOK_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_USER_ID INTEGER, $KEY_WORD TEXT, $KEY_AMERICAN_PRONUNCIATION TEXT, $KEY_BRITISH_PRONUNCIATION TEXT, $KEY_WORD_PART TEXT, $KEY_WORD_MEAN TEXT, FOREIGN KEY($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_USER_ID))")
        db.execSQL(createUserTable)
        db.execSQL(createSearchHistoryTable)
        db.execSQL(createTranslationHistoryTable)
        db.execSQL(createWordBookTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_SEARCH_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSLATION_HISTORY")
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORD_BOOK")
        onCreate(db)
    }

    fun addUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USERNAME, username)
        val encryptedPassword = hashString(password)
        values.put(KEY_PASSWORD, encryptedPassword)
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    fun getUser(userId: Long): String {
        var username = ""
        val selectQuery = "SELECT * FROM $TABLE_USERS WHERE $KEY_USER_ID=$userId"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            cursor.getColumnIndex(KEY_USERNAME).takeIf { it != -1 }?.let {
                username = cursor.getString(it)
            }
        }
        cursor.close()
        return username
    }

    fun getUserId(username: String): Long {
        var userId = -1L
        val selectQuery = "SELECT * FROM $TABLE_USERS WHERE $KEY_USERNAME='$username'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            cursor.getColumnIndex(KEY_USER_ID).takeIf { it != -1 }?.let {
                userId = cursor.getLong(it)
            }
        }
        cursor.close()
        return userId
    }

    fun getUserName(userId: Long): String {
        var username = ""
        val selectQuery = "SELECT * FROM $TABLE_USERS WHERE $KEY_USER_ID=$userId"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            cursor.getColumnIndex(KEY_USERNAME).takeIf { it != -1 }?.let {
                username = cursor.getString(it)
            }
        }
        cursor.close()
        return username
    }

    fun isUserExist(userId: Long): Boolean {
        val selectQuery = "SELECT * FROM $TABLE_USERS WHERE $KEY_USER_ID=$userId"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val isExist = cursor.count > 0
        cursor.close()
        return isExist
    }

    fun isUserExist(username: String): Boolean {
        val selectQuery = "SELECT * FROM $TABLE_USERS WHERE $KEY_USERNAME='$username'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val isExist = cursor.count > 0
        cursor.close()
        return isExist
    }

    fun isPasswordCorrect(username: String, password: String): Boolean {
        val encryptedPassword = hashString(password)
        val selectQuery =
            "SELECT * FROM $TABLE_USERS WHERE $KEY_USERNAME='$username' AND $KEY_PASSWORD='$encryptedPassword'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val isRight = cursor.count > 0
        cursor.close()
        return isRight
    }

    fun addWordBook(
        userId: Long, word: String,
        phAm: String, phEn: String,
        means: List<Word.Mean>
    ) {
        if (isInWordBook(userId, word)) return
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USER_ID, userId)
        values.put(KEY_WORD, word)
        values.put(KEY_AMERICAN_PRONUNCIATION, phAm)
        values.put(KEY_BRITISH_PRONUNCIATION, phEn)
        makeMeansString(means).let {
            values.put(KEY_WORD_PART, it.first)
            values.put(KEY_WORD_MEAN, it.second)
        }
        db.insert(TABLE_WORD_BOOK, null, values)
        db.close()
    }

    fun deleteWord(userId: Long, word: String) {
        val db = this.writableDatabase
        db.delete(
            TABLE_WORD_BOOK,
            "$KEY_USER_ID=? AND $KEY_WORD=?",
            arrayOf(userId.toString(), word)
        )
        db.close()
    }

    fun getAllWord(userId: Long): ArrayList<Word.Book> {
        val wordList = ArrayList<Word.Book>()
        val selectQuery = "SELECT * FROM $TABLE_WORD_BOOK WHERE $KEY_USER_ID=$userId"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val word = cursor.getColumnIndex(KEY_WORD).takeIf { it != -1 }?.let {
                    cursor.getString(it)
                } ?: ""
                val phAm =
                    cursor.getColumnIndex(KEY_AMERICAN_PRONUNCIATION).takeIf { it != -1 }?.let {
                        cursor.getString(it)
                    } ?: ""
                val phEn =
                    cursor.getColumnIndex(KEY_BRITISH_PRONUNCIATION).takeIf { it != -1 }?.let {
                        cursor.getString(it)
                    } ?: ""
                val wordPart = cursor.getColumnIndex(KEY_WORD_PART).takeIf { it != -1 }?.let {
                    cursor.getString(it)
                } ?: ""
                val wordMean = cursor.getColumnIndex(KEY_WORD_MEAN).takeIf { it != -1 }?.let {
                    cursor.getString(it)
                } ?: ""
                wordList.add(Word.Book(word, phEn, phAm, decodeMeansString(wordPart, wordMean)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return wordList
    }

    fun isInWordBook(userId: Long, word: String): Boolean {
        val selectQuery =
            "SELECT * FROM $TABLE_WORD_BOOK WHERE $KEY_USER_ID=$userId AND $KEY_WORD='$word'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        val isExist = cursor.count > 0
        cursor.close()
        return isExist
    }

    fun addSearchHistory(userId: Long, word: String, part: String, means: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_USER_ID, userId)
        values.put(KEY_SEARCH_WORD, word)
        values.put(KEY_WORD_PART, part)
        values.put(KEY_WORD_MEAN, means)
        db.insert(TABLE_WORD_HISTORY, null, values)
        db.close()
    }

    fun deleteSearchHistory(userId: Long, word: String) {
        val db = this.writableDatabase
        db.delete(
            TABLE_WORD_HISTORY,
            "$KEY_USER_ID=? AND $KEY_SEARCH_WORD=?",
            arrayOf(userId.toString(), word)
        )
        db.close()
    }

    fun getAllSearchHistory(userId: Long): ArrayList<Word.History> {
        val searchHistoryList = ArrayList<Word.History>()
        val selectQuery = "SELECT * FROM $TABLE_WORD_HISTORY WHERE $KEY_USER_ID=$userId"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val word = cursor.getColumnIndex(KEY_SEARCH_WORD).takeIf { it != -1 }?.let {
                    cursor.getString(it)
                } ?: ""
                val wordPart = cursor.getColumnIndex(KEY_WORD_PART).takeIf { it != -1 }?.let {
                    cursor.getString(it)
                } ?: ""
                val wordMean = cursor.getColumnIndex(KEY_WORD_MEAN).takeIf { it != -1 }?.let {
                    cursor.getString(it)
                } ?: ""
                searchHistoryList.add(Word.History(word, decodeMeansString(wordPart, wordMean)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return searchHistoryList
    }

    fun addTranslationHistory(
        userId: Long,
        srcLanguage: Translation.Language,
        src: String,
        dstLanguage: Translation.Language,
        dst: String
    ) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_ID, userId)
            put(KEY_TRANSLATION_SRC_LANGUAGE, srcLanguage.code)
            put(KEY_TRANSLATION_SRC, src)
            put(KEY_TRANSLATION_DST_LANGUAGE, dstLanguage.code)
            put(KEY_TRANSLATION_RESULT, dst)
        }
        db.insert(TABLE_TRANSLATION_HISTORY, null, values)
        db.close()
    }

    fun deleteTranslationHistory(userId: Long, translationId: Long) {
        val db = this.writableDatabase
        db.delete(
            TABLE_TRANSLATION_HISTORY,
            "$KEY_USER_ID=? AND $KEY_TRANSLATION_ID=?",
            arrayOf(userId.toString(), translationId.toString())
        )
        db.close()
    }

    fun getAllTranslationHistory(userId: Long): ArrayList<Translation.History> {
        val translationHistoryList = ArrayList<Translation.History>()
        val selectQuery = "SELECT * FROM $TABLE_TRANSLATION_HISTORY WHERE $KEY_USER_ID=$userId"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getColumnIndex(KEY_TRANSLATION_ID).takeIf { it != -1 }?.let {
                    cursor.getLong(it)
                } ?: continue
                val srcLanguage =
                    cursor.getColumnIndex(KEY_TRANSLATION_SRC_LANGUAGE).takeIf { it != -1 }?.let {
                        cursor.getString(it)
                    } ?: continue
                val src = cursor.getColumnIndex(KEY_TRANSLATION_SRC).takeIf { it != -1 }?.let {
                    cursor.getString(it)
                } ?: continue
                val dstLanguage =
                    cursor.getColumnIndex(KEY_TRANSLATION_DST_LANGUAGE).takeIf { it != -1 }?.let {
                        cursor.getString(it)
                    } ?: continue
                val dst = cursor.getColumnIndex(KEY_TRANSLATION_RESULT).takeIf { it != -1 }?.let {
                    cursor.getString(it)
                } ?: continue
                translationHistoryList.add(
                    Translation.History(
                        id,
                        Translation.Language.fromCode(srcLanguage)!!, src,
                        Translation.Language.fromCode(dstLanguage)!!, dst
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return translationHistoryList
    }

    private fun hashString(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * 将单词的词性和意思转换为数据库存储的字符串
     ******************
     * adj. 好的，优秀的
     * n. 好处，优点
     ******  to  ******
     * Pair("adj./n.", "好的，优秀的/好处，优点")
     */
    private fun makeMeansString(means: List<Word.Mean>): Pair<String, String> {
        val parts = means.joinToString("/") { it.part }
        val meanList = means.joinToString("/") { it.means.joinToString(", ") }
        return Pair(parts, meanList)
    }

    private fun decodeMeansString(parts: String, means: String): List<Word.Mean> {
        val partList = parts.split("/")
        val meanList = means.split("/")
        return partList.zip(meanList).map { Word.Mean(it.first, it.second.split(", ")) }
    }
}