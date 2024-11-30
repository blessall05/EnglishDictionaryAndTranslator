package com.blessall05.translator.model

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.blessall05.translator.App.Companion.app
import com.blessall05.translator.R
import com.blessall05.translator.model.data.DailySentence
import com.blessall05.translator.model.data.Translation
import com.blessall05.translator.model.data.Word
import com.blessall05.translator.util.HttpUtil
import com.blessall05.translator.util.JsonUtil
import okhttp3.Response
import okio.buffer
import okio.sink
import java.io.File
import java.io.FileOutputStream

object NetworkHelper {
    fun getWord(word: String, callback: WordCallback) {
        HttpUtil.searchWord(word, object : HttpUtil.NetworkCallback {
            override fun onFailure(e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(app, app.getString(R.string.network_error), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onSuccess(response: Response) {
                callback.onCallback(JsonUtil.decodeWord(response.body?.string() ?: ""))
            }
        })
    }

    fun interface WordCallback {
        fun onCallback(word: Word?)
    }

    fun download(url: String, name: String, callback: DownloadCallback? = null) {
        HttpUtil.callRequest(url, object : HttpUtil.NetworkCallback {
            override fun onFailure(e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(app, app.getString(R.string.network_error), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onSuccess(response: Response) {
                val file = File(app.cacheDir, name)
                Log.d("Download", file.absolutePath)
                val sink = FileOutputStream(file).sink().buffer()

                response.body?.source()?.let { source ->
                    sink.writeAll(source)
                    sink.close()
                }
                callback?.onCallback()
            }
        })
    }

    fun interface DownloadCallback {
        fun onCallback()
    }

    fun getDailySentence(callback: DailySentenceCallback) {
        HttpUtil.dailySentence(object : HttpUtil.NetworkCallback {
            override fun onFailure(e: Exception) {
                e.printStackTrace()
            }

            override fun onSuccess(response: Response) {
                callback.onCallback(JsonUtil.decodeDailySentence(response.body?.string() ?: ""))
            }
        })
    }

    fun interface DailySentenceCallback {
        fun onCallback(dailySentence: DailySentence?)
    }

    fun getBaiduTranslation(
        text: String,
        srcLanguage: String,
        desLanguage: String,
        callback: TranslationCallback
    ) {
        HttpUtil.baiduTranslate(text, srcLanguage, desLanguage, object : HttpUtil.NetworkCallback {
            override fun onFailure(e: Exception) {
                e.printStackTrace()
            }

            override fun onSuccess(response: Response) {
                JsonUtil.decodeBaiduTranslate(response.body?.string() ?: "")
                    ?.let { callback.onCallback(it) }
            }
        })
    }

//    fun getNiuTranslation(
//        text: String,
//        srcLanguage: String,
//        desLanguage: String,
//        callback: TranslationCallback
//    ) {
//        HttpUtil.niuTranslate(text, srcLanguage, desLanguage, object : HttpUtil.NetworkCallback {
//            override fun onFailure(e: Exception) {
//                            }
//
//            override fun onSuccess(response: Response) {
//                callback.onCallback(JsonUtil.decodeNiuTranslate(response.body?.string() ?: ""))
//            }
//        })
//    }

    fun interface TranslationCallback {
        fun onCallback(translation: Translation)
    }
}