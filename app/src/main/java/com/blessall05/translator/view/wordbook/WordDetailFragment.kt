package com.blessall05.translator.view.wordbook

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.blessall05.translator.App.Companion.database
import com.blessall05.translator.R
import com.blessall05.translator.databinding.ItemWordDetailBinding
import com.blessall05.translator.model.AppData
import com.blessall05.translator.model.NetworkHelper
import com.blessall05.translator.util.FileUtil
import com.blessall05.translator.view.dictionary.WordMeansAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WordDetailFragment : BottomSheetDialogFragment() {
    companion object {
        val tag = "WordDetailFragment"
    }

    private var _binding: ItemWordDetailBinding? = null
    private val binding get() = _binding!!
    private var activity: WordActivity? = null
    private val word get() = activity?.viewModel?.word
    private val mediaPlayer get() = activity?.mediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemWordDetailBinding.inflate(inflater, container, false)
        binding.pnAmCard.root.setOnClickListener {
            word?.phAmMp3?.let { playSound(it) }
        }
        binding.pnEnCard.root.setOnClickListener {
            word?.phEnMp3?.let { playSound(it) }
        }
        return binding.root
    }

    private fun playSound(url: String) {
        if (url.isBlank()) return
        mediaPlayer?.apply {
            reset()
            try {
                setDataSource(url)
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as WordActivity
    }

    private fun updateUi() {
        word?.let { word ->
            binding.pnAmCard.apply {
                pnLabel.text = getString(R.string.pronunciation_American)
                pnText.text = word.phAm
            }
            binding.pnEnCard.apply {
                pnLabel.text = getString(R.string.pronunciation_English)
                pnText.text = word.phEn
            }
            // 生词本
            binding.addToWordBook.apply {
                isChecked = word.isInWordBook
                setOnClickListener {
                    val userId = AppData.userId
                    if (userId == -1L) {
                        Toast.makeText(
                            activity,
                            getString(R.string.please_login_to_use_word_book),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    if (isChecked) {
                        if (word.phAmMp3.isNotBlank() && !FileUtil.isExistInCache(word.name + "-us.mp3"))
                            NetworkHelper.download(word.phAmMp3, word.name + "-us.mp3")
                        if (word.phEnMp3.isNotBlank() && !FileUtil.isExistInCache(word.name + "-uk.mp3"))
                            NetworkHelper.download(word.phEnMp3, word.name + "-uk.mp3")
                        if (database.isInWordBook(userId, word.name))
                            return@setOnClickListener //已存在
                        database.addWordBook(userId, word.name, word.phEn, word.phAm, word.means)
                    } else {
                        database.deleteWord(userId, word.name)
                    }
                    isChecked = database.isInWordBook(userId, word.name)
                }
                //词性变化
                binding.wordVariation.apply {
                    if (word.wordPl.isEmpty() && word.wordThird.isEmpty() && word.wordPast.isEmpty() &&
                        word.wordDone.isEmpty() && word.wordIng.isEmpty() && word.wordEr.isEmpty() &&
                        word.wordEst.isEmpty()
                    ) {
                        root.visibility = View.GONE
                        return@apply
                    }
                    root.visibility = View.VISIBLE
                    showOrHideVariation(wordPlLayout, wordPl, word.wordPl)
                    showOrHideVariation(wordThirdLayout, wordThird, word.wordThird)
                    showOrHideVariation(wordPastLayout, wordPast, word.wordPast)
                    showOrHideVariation(wordDoneLayout, wordDone, word.wordDone)
                    showOrHideVariation(wordIngLayout, wordIng, word.wordIng)
                    showOrHideVariation(wordErLayout, wordEr, word.wordEr)
                    showOrHideVariation(wordEstLayout, wordEst, word.wordEst)
                }
                //单词释义
                binding.meansRecyclerView.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = WordMeansAdapter(word)
                }
            }
        }
    }

    private fun showOrHideVariation(layout: LinearLayout, textView: TextView, words: List<String>) {
        if (words.isEmpty()) {
            layout.visibility = View.GONE
        } else {
            layout.visibility = View.VISIBLE
            textView.text = words.joinToString("; ")
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}