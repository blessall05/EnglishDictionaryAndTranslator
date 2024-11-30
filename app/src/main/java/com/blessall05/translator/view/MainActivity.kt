package com.blessall05.translator.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.blessall05.translator.R
import com.blessall05.translator.databinding.ActivityMainBinding
import com.blessall05.translator.view.wordbook.WordActivity

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val viewPager = binding.contentMain.viewPager2
        viewPager.apply {
            adapter = MainPagerAdapter(supportFragmentManager, lifecycle)
            offscreenPageLimit = 1
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding.contentMain.navigationBar.menu.getItem(position).isChecked = true
                }
            })
        }

        binding.contentMain.navigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> viewPager.currentItem = 0
                R.id.dictionary -> viewPager.currentItem = 1
                R.id.translate -> viewPager.currentItem = 2
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.word_book -> {
                startActivity(Intent(this, WordActivity::class.java))
            }
        }
        return true
    }
}