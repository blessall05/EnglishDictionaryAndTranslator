package com.blessall05.translator.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.blessall05.translator.App.Companion.app
import com.blessall05.translator.R
import com.blessall05.translator.model.AppData.md5
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

object HttpUtil {
    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(2, TimeUnit.SECONDS)
        .build()

    // 使用 金山词霸 API
    fun searchWord(word: String, callback: NetworkCallback) {
        val url =
            "https://dict-co.iciba.com/api//dictionary.php?w=$word&type=json&key=1F9CA812CB18FFDFC95FC17E9C57A5E1"
        callRequest(url, null, callback)
    }

    // 下载MP3
    fun callRequest(url: String, callback: NetworkCallback) {
        callRequest(url, null, callback)
    }

    // 每日一句
    fun dailySentence(callback: NetworkCallback) {
        val url = "https://open.iciba.com/dsapi"
        callRequest(url, null, callback)
    }

    // 使用 百度翻译 API
    fun baiduTranslate(
        query: String,
        from: String,
        to: String,
        callback: NetworkCallback
    ) {
        val appId = "20240419002028638"
        val appKey = "gx1NbEJFWpInraf8ryE8"
        // 生成随机数
        val salt = UUID.randomUUID().toString()
        // 生成签名
        val sign = md5("$appId$query$salt$appKey").lowercase(Locale.ROOT)

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("fanyi-api.baidu.com")
            .addPathSegments("api/trans/vip/translate")
            .addQueryParameter("q", query)
            .addQueryParameter("from", from)
            .addQueryParameter("to", to)
            .addQueryParameter("appid", appId)
            .addQueryParameter("salt", salt)
            .addQueryParameter("sign", sign)
            .build().toString()

        callRequest(url, null, callback)
    }


    // 使用 小牛翻译 API
    fun niuTranslate(
        text: String,
        srcLanguage: String,
        desLanguage: String,
        callback: NetworkCallback
    ) {
        val url = "https://api.niutrans.com/NiuTransServer/translation"
        val formBody = FormBody.Builder()
            .add("src_text", text)
            .add("from", srcLanguage)
            .add("to", desLanguage)
            .add("apikey", "00bb14eb2d36c5fb34da0a8f270da4f6")
            .build()
        callRequest(url, formBody, callback)
    }

    private fun callRequest(url: String, postBody: RequestBody?, callback: NetworkCallback) {
        val request = Request.Builder().url(url).apply {
            postBody?.let { post(it) }
        }.build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                when (e) {
                    is SocketTimeoutException -> { //超时
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                app, app.getString(R.string.timeout), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is ConnectException -> { // 连接失败
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                app, app.getString(R.string.connect_fail), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    else -> { // 其他错误
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                app, app.getString(R.string.network_error), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(response)
            }
        })
    }

    interface NetworkCallback {
        fun onSuccess(response: Response)
        fun onFailure(e: Exception)
    }
}