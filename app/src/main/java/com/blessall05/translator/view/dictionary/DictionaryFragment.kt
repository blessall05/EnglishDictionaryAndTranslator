package com.blessall05.translator.view.dictionary

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blessall05.translator.R
import com.blessall05.translator.databinding.FragmentDictionaryBinding
import com.blessall05.translator.model.AppData
import com.blessall05.translator.model.NetworkHelper
import com.blessall05.translator.model.data.Word
import com.blessall05.translator.util.FileUtil
import com.blessall05.translator.view.BaseFragment

class DictionaryFragment : BaseFragment() {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!
    private var mediaPlayer: MediaPlayer? = null

    private val viewModel: DictionaryViewModel by viewModels()
    private val adapter by lazy { WordHistoryAdapter() }
    private var showWordState  // 显示单词详情或是搜索历史状态
        get() = viewModel.showWord
        set(value) {
            onBackPressedCallback.isEnabled = value
            viewModel.showWord = value
        }
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (showWordState) {
                    updateView(false)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        mediaPlayer = MediaPlayer()
        binding.wordLayout.meansRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DictionaryFragment.adapter.apply {
                setOnItemClickListener { history ->
                    binding.wordInput.setText(history.word)
                    NetworkHelper.getWord(history.word) {
                        viewModel.word = it
                        activity.runOnUiThread {
                            updateView(true)
                        }
                    }
                }
                setOnDeleteClickListener {
                    databaseHelper.deleteSearchHistory(AppData.userId, currentList[it].word)
                    updateHistoryView()
                }
            }
        }
        binding.turnBack.setOnClickListener {
            updateView(false)
        }
        binding.wordInput.apply {
            setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.keyCode == KeyEvent.KEYCODE_ENTER) { //回车搜索
                    val input = binding.wordInput.text.toString()
                    if (input.trim()
                            .any { it.code !in 'a'.code..'z'.code && it.code !in 'A'.code..'Z'.code }
                    ) {
                        Toast.makeText(
                            activity,
                            getString(R.string.please_input_single_word),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnEditorActionListener true
                    }

                    // 关闭输入法
                    val inputMethodManager =
                        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
                    NetworkHelper.getWord(input) {
                        viewModel.word = it
                        activity.runOnUiThread {
                            updateView(true)
                            addSearchHistory(it)
                        }
                    }
                    true
                } else {
                    if (text.toString().isNotEmpty()) //输入框有内容时显示清除按钮
                        binding.clearText.visibility = View.VISIBLE
                    else
                        binding.clearText.visibility = View.INVISIBLE
                    false
                }
            }
        }
        binding.clearText.setOnClickListener {
            binding.wordInput.setText("")
            updateView(false)
        }
        binding.wordLayout.pnAmCard.root.setOnClickListener {
            viewModel.word?.phAmMp3?.let { playSound(it) }
        }
        binding.wordLayout.pnEnCard.root.setOnClickListener {
            viewModel.word?.phEnMp3?.let { playSound(it) }
        }
        updateView(false)
        return binding.root
    }

    private fun updateView(showWord: Boolean? = null) {
        if (showWord != null) showWordState = showWord
        if (showWordState && initWordView()) {
            binding.historyLayout.visibility = View.GONE
            binding.wordLayout.root.visibility = View.VISIBLE
            binding.turnBack.visibility = View.VISIBLE
        } else {
            updateHistoryView()
            binding.historyLayout.visibility = View.VISIBLE
            binding.wordLayout.root.visibility = View.GONE
            binding.turnBack.visibility = View.GONE
        }
    }

    private fun updateHistoryView() {
        val userId = AppData.userId
        if (userId == -1L) {
            binding.historyText.visibility = View.VISIBLE
            binding.historyText.text = getString(R.string.please_login_to_use_history)
            return
        }
        viewModel.list.clear()
        viewModel.list.addAll(databaseHelper.getAllSearchHistory(AppData.userId))
        adapter.submitList(viewModel.list.reversed())
        if (viewModel.list.isEmpty()) {
            binding.historyText.visibility = View.VISIBLE
            binding.historyText.text = getString(R.string.history_empty)
        } else {
            binding.historyText.visibility = View.GONE
        }
    }

    private fun initWordView(): Boolean {
        val word = viewModel.word ?: return false
        binding.wordLayout.pnAmCard.apply {
            if (word.phAm.isEmpty()) {
                root.visibility = View.GONE
                return@apply
            }
            pnLabel.text = getString(R.string.pronunciation_American)
            pnText.text = word.phAm
            if (word.phAmMp3.isEmpty()) {
                root.isClickable = false
                playIcon.visibility = View.GONE
            } else {
                root.isClickable = true
                playIcon.visibility = View.VISIBLE
            }
        }
        binding.wordLayout.pnEnCard.apply {
            if (word.phEn.isEmpty()) {
                root.visibility = View.GONE
                return@apply
            }
            pnLabel.text = getString(R.string.pronunciation_English)
            pnText.text = word.phEn
            if (word.phEnMp3.isEmpty()) {
                root.isClickable = false
                playIcon.visibility = View.GONE
            } else {
                root.isClickable = true
                playIcon.visibility = View.VISIBLE
            }
        }
        // 生词本
        binding.wordLayout.addToWordBook.apply {
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
                    if (databaseHelper.isInWordBook(userId, word.name))
                        return@setOnClickListener //已存在
                    databaseHelper.addWordBook(userId, word.name, word.phEn, word.phAm, word.means)
                } else {
                    databaseHelper.deleteWord(userId, word.name)
                }
                isChecked = databaseHelper.isInWordBook(userId, word.name)
            }
        }
        //词性变化
        binding.wordLayout.wordVariation.apply {
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
        binding.wordLayout.meansRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = WordMeansAdapter(word)
        }
        return true
    }

    private fun showOrHideVariation(layout: LinearLayout, textView: TextView, words: List<String>) {
        if (words.isEmpty()) {
            layout.visibility = View.GONE
        } else {
            layout.visibility = View.VISIBLE
            textView.text = words.joinToString("; ")
        }
    }

    private fun addSearchHistory(word: Word?) {
        word ?: return
        val userId = AppData.userId
        if (userId == -1L) return
        val part = word.means.joinToString("/") { it.part }
        val means = word.means.joinToString("/") { it.means.joinToString(",") }
        databaseHelper.addSearchHistory(userId, word.name, part, means)
    }

    private fun playSound(url: String) {
        if (url.isBlank()) return
        mediaPlayer?.apply {
            reset()
            setDataSource(url)
            prepare()
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        toolbar.title = getString(R.string.dictionary)
        if (showWordState) onBackPressedCallback.isEnabled = true
        updateView()
    }

    override fun onPause() {
        super.onPause()
        onBackPressedCallback.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}