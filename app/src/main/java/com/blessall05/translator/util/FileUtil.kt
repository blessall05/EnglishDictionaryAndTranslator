package com.blessall05.translator.util

import com.blessall05.translator.App.Companion.app
import java.io.File

object FileUtil {

    fun isExistInCache(name: String): Boolean {
        val file = File(app.cacheDir.path, name)
        return file.exists()
    }

    fun getCacheFilePath(name: String): String {
        return File(app.cacheDir.path, name).absolutePath
    }
}