package com.blessall05.translator.view.wordbook

import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blessall05.translator.App.Companion.database
import com.blessall05.translator.R
import com.blessall05.translator.databinding.ActivityWordBinding
import com.blessall05.translator.model.AppData
import com.blessall05.translator.model.NetworkHelper
import com.blessall05.translator.model.data.Word
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class WordActivity : AppCompatActivity(),
    WordAdapter.OnItemClickListener, WordAdapter.OnItemLongClickListener {
    lateinit var binding: ActivityWordBinding
    val viewModel: WordViewModel by viewModels()

    private val adapter by lazy { WordAdapter() }
    val mediaPlayer = MediaPlayer()
    private val fragment = WordDetailFragment()

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = getString(R.string.has_selected_item, adapter.selectedList.size)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.delete -> {
                    MaterialAlertDialogBuilder(this@WordActivity)
                        .setTitle(R.string.remove)
                        .setMessage(R.string.delete_selected_item)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            adapter.selectedList.forEach {
                                database.deleteWord(AppData.userId, it.word)
                            }
                            updateUI()
                            mode.finish()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            val list = adapter.selectedList.toList()
            adapter.selectedList.clear()
            list.forEach {
                adapter.notifyItemChanged(adapter.currentList.indexOf(it))
            }
            actionMode = null
            binding.toolbar.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.refreshList()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@WordActivity.adapter.apply {
                setOnItemClickListener(this@WordActivity)
                setOnItemLongClickListener(this@WordActivity)
                setOnPnCardClickListener { word, isBritish ->
                    playLocal(word, isBritish)
                }
            }
        }
        binding.indexBar.setOnIndexChangeListener {
            var position = 0
            for ((index, item) in viewModel.list.withIndex()) {
                if (item.word[0].uppercaseChar() <= it[0].uppercaseChar()) position = index
            }
            binding.recyclerView.scrollToPosition(position)
        }
        updateUI()
    }

    fun updateUI() {
        viewModel.refreshList()
        adapter.submitList(viewModel.list.toList())

        binding.emptyView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        if (AppData.userId == -1L) {
            binding.emptyView.text = getString(R.string.please_login_to_use_word_book)
        } else if (viewModel.list.isEmpty()) {
            binding.emptyView.text = getString(R.string.empty_word_book)
        } else {
            binding.emptyView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun playLocal(word: String, isBritish: Boolean) {
        val url =
            if (isBritish) File("${cacheDir.absolutePath}/$word-uk.mp3").path
            else File("${cacheDir.absolutePath}/$word-us.mp3").path
        mediaPlayer.apply {
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

    override fun onItemClick(item: Word.Book, position: Int) {
        if (adapter.selectedList.isEmpty()) {
            NetworkHelper.getWord(item.word) {
                viewModel.word = it
                fragment.show(supportFragmentManager, WordDetailFragment.tag)
            }
        } else {
            onItemLongClick(item, position)
        }
    }

    override fun onItemLongClick(item: Word.Book, position: Int) {
        if (item in adapter.selectedList) adapter.selectedList.remove(item)
        else adapter.selectedList.add(item)
        if (actionMode == null) {
            binding.toolbar.visibility = View.GONE
            actionMode = startSupportActionMode(actionModeCallback)
        }
        if (adapter.selectedList.isEmpty()) actionMode?.finish()
        else actionMode?.invalidate()
        adapter.notifyItemChanged(position, Unit)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.word_book, menu)
        if (menu != null) {
            val searchView =
                menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.filter = newText
                    adapter.submitList(viewModel.list.toList())
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean = true
            })
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.sort_by_default -> {
                item.isChecked = true
                viewModel.sortBy = (WordViewModel.SORT_BY_DEFAULT)
            }

            R.id.sort_by_letter -> {
                item.isChecked = true
                viewModel.sortBy = (WordViewModel.SORT_BY_LETTER)
            }

            R.id.sort_by_length -> {
                item.isChecked = true
                viewModel.sortBy = (WordViewModel.SORT_BY_LENGTH)
            }

            R.id.sort_descending -> {
                item.isChecked = !item.isChecked
                viewModel.isDescending = (item.isChecked)
            }
        }
        if (item.groupId == R.id.sort_group || item.itemId == R.id.sort_descending) {
            adapter.submitList(viewModel.list.toList())
        }
        showOrHideIndexBar()
        return true
    }

    private fun showOrHideIndexBar() {
        if (viewModel.sortBy == WordViewModel.SORT_BY_LETTER && !viewModel.isDescending) {
            binding.indexBar.visibility = View.VISIBLE
        } else {
            binding.indexBar.visibility = View.GONE
        }
    }
}