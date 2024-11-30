package com.blessall05.translator.view.home

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.blessall05.translator.R
import com.blessall05.translator.databinding.FragmentHomeBinding
import com.blessall05.translator.model.AppData
import com.blessall05.translator.model.NetworkHelper
import com.blessall05.translator.util.DataUtil
import com.blessall05.translator.util.FileUtil
import com.blessall05.translator.view.BaseFragment
import com.blessall05.translator.view.login.LoginActivity
import com.bumptech.glide.Glide

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        mediaPlayer = MediaPlayer()
        binding.playSound.setOnClickListener {
            playSound(viewModel.dailySentence?.audioUrl ?: return@setOnClickListener)
        }
        val sentence = viewModel.dailySentence
        if (sentence == null) {
            Glide.with(this)
                .load("file:///android_asset/daily_sentence_cover.jpg") //默认使用asset下的图片
                .into(binding.picture)
            binding.chinese.text = getString(R.string.daily_sentence_chinese)
            binding.english.text = getString(R.string.daily_sentence_english)
        }
        return binding.root
    }

    private fun playSound(url: String) {
        mediaPlayer?.apply {
            reset()
            setDataSource(url)
            prepare()
            start()
        }
    }

    override fun onResume() {
        super.onResume()
        toolbar.title = getString(R.string.home)
        if (AppData.userId == -1L) {
            binding.userName.text = getString(R.string.click_to_login)
            binding.userName.isClickable = true
            binding.userName.setOnClickListener {
                startActivity(Intent(activity, LoginActivity::class.java))
            }
        } else {
            binding.userName.text = getString(R.string.welcome, AppData.userName)
            binding.userName.isClickable = false
            binding.userName.text = getString(R.string.welcome, AppData.userName)
            binding.userName.setOnClickListener {
                AppData.setUserId(-1L)
                startActivity(Intent(activity, LoginActivity::class.java))
            }
        }
        // 刷新图片
        val name = DataUtil.getCurrentDate() + ".jpg"
        if (viewModel.dailySentence != null) {
            val sentence = viewModel.dailySentence!!
            binding.chinese.text = sentence.chinese
            binding.english.text = sentence.english
            activity.runOnUiThread {
                Glide.with(this).load(FileUtil.getCacheFilePath(name))
                    .into(binding.picture)
            }
        } else {
            //存在图片缓存则加载缓存
            if (FileUtil.isExistInCache(name)) activity.runOnUiThread {
                Glide.with(this).load(FileUtil.getCacheFilePath(name))
                    .into(binding.picture)
            }
            //获取每日一句
            NetworkHelper.getDailySentence {
                viewModel.dailySentence = it
                if (it != null) {
                    // 中文英文
                    activity.runOnUiThread {
                        binding.chinese.text = it.chinese
                        binding.english.text = it.english
                    }
                    // 本地无图片则下载
                    if (!FileUtil.isExistInCache(name)) {
                        NetworkHelper.download(it.pictureUrl, name) {
                            activity.runOnUiThread {
                                Glide.with(this).load(FileUtil.getCacheFilePath(name))
                                    .into(binding.picture)
                            }
                        }
                    }
                } else {
                    Toast.makeText(activity, getString(R.string.api_error), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaPlayer?.reset()
        mediaPlayer = null
    }
}