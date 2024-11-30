package com.blessall05.translator.model

import androidx.preference.PreferenceManager
import com.blessall05.translator.App.Companion.app
import java.security.MessageDigest

object AppData {
    private val sp = PreferenceManager.getDefaultSharedPreferences(app)
    private val database = DatabaseHelper(app)
    val userId
        get() = sp.getLong("userId", -1L).takeIf { it != -1L && database.isUserExist(it) } ?: let {
            setUserId(-1L)
            -1
        }
    val userName get() = database.getUserName(userId)

    fun setUserId(value: Long) {
        sp.edit().putLong("userId", value).apply()
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        val sb = StringBuilder()
        for (b in digest) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    fun getSavedUser(): Pair<String, String> {
        val username = sp.getString("userName", "") ?: ""
        val password = sp.getString("password", "") ?: ""
        return Pair(username, decryptedInsecure(password))
    }

    fun saveUser(name: String, password: String) {
        sp.edit().putString("userName", name).apply()
        sp.edit().putString("password", encryptedInsecure(password)).apply()
    }

    private fun encryptedInsecure(input: String): String {
        return input.map { it + 1 }.joinToString("").reversed()
    }

    private fun decryptedInsecure(input: String): String {
        return input.reversed().map { it - 1 }.joinToString("")
    }
}
