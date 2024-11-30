package com.blessall05.translator.view.translate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blessall05.translator.R
import com.blessall05.translator.databinding.FragmentTranslateBinding
import com.blessall05.translator.model.AppData
import com.blessall05.translator.model.NetworkHelper
import com.blessall05.translator.model.data.Translation
import com.blessall05.translator.view.BaseFragment

class TranslateFragment : BaseFragment() {
    private var _binding: FragmentTranslateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TranslateViewModel by viewModels()
    private val adapter by lazy { TranslateHistoryAdapter() }
    private var showTranslationState = false
        get() = viewModel.showTranslation
        set(value) {
            _binding?.turnBack?.visibility = if (value) View.VISIBLE else View.GONE
            onBackPressedCallback.isEnabled = value
            field = value
        }
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (showTranslationState) {
                    showTranslationState = false
                    updateView()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslateBinding.inflate(inflater, container, false)
        //历史记录
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TranslateFragment.adapter.apply {
                setOnItemClickListener {
                    viewModel.text = it.src
                    viewModel.translatedText = it.dst
                    viewModel.sourceLanguage = it.srcLanguage
                    viewModel.destinationLanguage = it.dstLanguage
                    updateView(true)
                }
                setDeleteClickListener {
                    databaseHelper.deleteTranslationHistory(AppData.userId, currentList[it].id)
                    updateHistoryView()
                }
            }
        }
        //交换按钮
        binding.translateSwap.setOnClickListener {
            if (viewModel.sourceLanguage == Translation.Language.AUTO) {
                Toast.makeText(
                    activity,
                    getString(R.string.auto_language_cannot_swap),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            val temp1 = viewModel.sourceLanguage
            viewModel.sourceLanguage = viewModel.destinationLanguage
            viewModel.destinationLanguage = temp1
            binding.translateFrom.setSelection(viewModel.sourceLanguage.ordinal)
            binding.translateTo.setSelection(viewModel.destinationLanguage.ordinal - 1)

            val temp2 = binding.textInput.text.toString()
            binding.textInput.setText(binding.translateResult.text)
            binding.translateResult.text = temp2
        }
        binding.translateFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.sourceLanguage = Translation.Language.entries[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.translateTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.destinationLanguage = Translation.Language.entries[position + 1]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        //文本输入
        binding.textInput.setOnEditorActionListener { _, _, _ ->
            viewModel.text = binding.textInput.text.toString() //记录
            false
        }
        //返回按钮
        binding.turnBack.setOnClickListener {
            showTranslationState = false
            updateView()
        }
        //翻译按钮
        binding.translateButton.setOnClickListener {
            val text = binding.textInput.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(activity, getString(R.string.please_input_text), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            viewModel.text = text
            binding.translateResult.text = getString(R.string.loading)

            NetworkHelper.getBaiduTranslation( //网络翻译
                viewModel.text,
                viewModel.sourceLanguage.code,
                viewModel.destinationLanguage.code
            ) {
                viewModel.translatedText = it.dst
                databaseHelper.addTranslationHistory(
                    AppData.userId,
                    it.srcLanguage,
                    it.src,
                    it.dstLanguage,
                    it.dst
                )
                activity.runOnUiThread {
                    updateView(true) // 异步显示翻译
                }
            }
        }
        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        updateView(false)
        return binding.root
    }

    private fun updateView(showTranslation: Boolean? = null) {
        if (showTranslation ?: showTranslationState) {
            binding.historyLayout.visibility = View.GONE
            binding.translateResultCard.visibility = View.VISIBLE
            updateTranslateView()
        } else {
            binding.historyLayout.visibility = View.VISIBLE
            binding.translateResultCard.visibility = View.GONE
            updateHistoryView()
        }
    }

    private fun updateTranslateView() {
        binding.textInput.setText(viewModel.text)
        binding.translateResult.text = viewModel.translatedText
        binding.translateFrom.setSelection(viewModel.sourceLanguage.ordinal)
        binding.translateTo.setSelection(viewModel.destinationLanguage.ordinal - 1)
        showTranslationState = true
    }

    private fun updateHistoryView() {
        val userId = AppData.userId
        if (userId == -1L) {
            binding.historyText.visibility = View.VISIBLE
            binding.historyText.text = getString(R.string.please_login_to_use_history)
            return
        }
        val list = viewModel.historyList
        list.clear()
        list.addAll(databaseHelper.getAllTranslationHistory(AppData.userId))
        if (list.isEmpty()) {
            binding.historyText.visibility = View.VISIBLE
            binding.historyText.text = getString(R.string.history_empty)
            return
        }
        binding.historyText.visibility = View.GONE
        adapter.submitList(list.reversed())
    }

    override fun onResume() {
        super.onResume()
        toolbar.title = getString(R.string.translate)
        if (showTranslationState) onBackPressedCallback.isEnabled = true
        updateView()
    }

    override fun onPause() {
        super.onPause()
        onBackPressedCallback.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}