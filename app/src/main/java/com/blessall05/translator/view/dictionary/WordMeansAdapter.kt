package com.blessall05.translator.view.dictionary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blessall05.translator.databinding.ItemWordMeanBinding
import com.blessall05.translator.model.data.Word

class WordMeansAdapter(private val word: Word) :
    RecyclerView.Adapter<WordMeansAdapter.ViewHolder>() {
    class ViewHolder(binding: ItemWordMeanBinding) : RecyclerView.ViewHolder(binding.root) {
        val part = binding.part
        val means = binding.means
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemWordMeanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val means = word.means[position]
        holder.part.text = means.part
        holder.means.text = means.means.joinToString("；")
    }

    override fun getItemCount(): Int {
        return word.means.size
    }
}