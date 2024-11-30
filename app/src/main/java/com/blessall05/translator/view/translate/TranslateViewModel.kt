package com.blessall05.translator.view.translate

import androidx.lifecycle.ViewModel
import com.blessall05.translator.model.data.Translation

class TranslateViewModel : ViewModel() {
    var sourceLanguage: Translation.Language = Translation.Language.EN
    var destinationLanguage: Translation.Language = Translation.Language.ZH
    var text: String = ""
    var translatedText: String = ""

    var showTranslation = false
    val historyList = mutableListOf<Translation.History>()
}
