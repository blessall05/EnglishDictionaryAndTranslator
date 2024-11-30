package com.blessall05.translator.util

import com.blessall05.translator.model.data.DailySentence
import com.blessall05.translator.model.data.Translation
import com.blessall05.translator.model.data.Word
import org.json.JSONObject

object JsonUtil {
    fun decodeWord(jsonString: String): Word? {
        try {
            val jsonObject = JSONObject(jsonString)
            val wordName = jsonObject.getString("word_name")

            // 解析词性和变化
            val exchangeObject = jsonObject.getJSONObject("exchange")
            val wordPlList = exchangeObject.optJSONArray("word_pl")?.toStringList() ?: listOf()
            val wordThirdList =
                exchangeObject.optJSONArray("word_third")?.toStringList() ?: listOf()
            val wordPastList = exchangeObject.optJSONArray("word_past")?.toStringList() ?: listOf()
            val wordDoneList = exchangeObject.optJSONArray("word_done")?.toStringList() ?: listOf()
            val wordIngList = exchangeObject.optJSONArray("word_ing")?.toStringList() ?: listOf()
            val wordErList = exchangeObject.optJSONArray("word_er")?.toStringList() ?: listOf()
            val wordEstList = exchangeObject.optJSONArray("word_est")?.toStringList() ?: listOf()

            // 解析音标和发音
            val symbolObject = jsonObject.getJSONArray("symbols").getJSONObject(0)
            val phEn = symbolObject.optString("ph_en")
            val phAm = symbolObject.optString("ph_am")
            val phEnMp3 = symbolObject.optString("ph_en_mp3")
            val phAmMp3 = symbolObject.optString("ph_am_mp3")

            // 解析词义
            val parts = symbolObject.getJSONArray("parts")
            val meansList = mutableListOf<Word.Mean>()
            for (i in 0 until parts.length()) {
                val partObject = parts.getJSONObject(i)
                val part = partObject.getString("part")
                val meansArray = partObject.getJSONArray("means")
                val means = meansArray.toStringList()
                meansList.add(Word.Mean(part, means))
            }
            return Word(
                wordName, wordPlList, wordThirdList, wordPastList, wordDoneList, wordIngList,
                wordErList, wordEstList, phEn, phAm, phEnMp3, phAmMp3, meansList
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decodeDailySentence(jsonString: String): DailySentence? {
        try {
            val jsonObject = JSONObject(jsonString)
            val note = jsonObject.optString("note")
            val content = jsonObject.optString("content")
            val audioUrl = jsonObject.optString("tts")
            val pictureUrl = jsonObject.optString("fenxiang_img")
            if (note.isNotEmpty() && content.isNotEmpty() && pictureUrl.isNotEmpty())
                return DailySentence(note, content, audioUrl, pictureUrl)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decodeNiuTranslate(jsonString: String): String {
        try {
            val jsonObject = JSONObject(jsonString)
            val errorCode = jsonObject.optInt("error_code", 0)
            if (errorCode == 0)
                return jsonObject.getString("tgt_text")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    //{"from":"zh","to":"zh","trans_result":[{"src":"\u4f60\u597d","dst":"\u4f60\u597d"}]}
    fun decodeBaiduTranslate(jsonString: String): Translation? {
        try {
            val jsonObject = JSONObject(jsonString)
            val from = jsonObject.getString("from")
            val to = jsonObject.getString("to")
            val result = jsonObject.getJSONArray("trans_result").getJSONObject(0)
            val src = result.getString("src")
            val dst = result.getString("dst")
            return Translation(
                Translation.Language.fromCode(from)!!, src,
                Translation.Language.fromCode(to)!!,
                dst
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
