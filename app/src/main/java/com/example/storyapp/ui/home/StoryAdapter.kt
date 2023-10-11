package com.example.storyapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.data.local.entity.StoryEntity
import com.example.storyapp.databinding.StoryCardBinding
import com.example.storyapp.helper.Utils.withDateFormat
import com.example.storyapp.ui.detail.DetailActivity

class StoryAdapter :
    PagingDataAdapter<StoryEntity, StoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryAdapter.ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }


    inner class ViewHolder(private val binding: StoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StoryEntity) {
            binding.tvItemName.text = data.name
            binding.tvItemCreatedAt.text = data.createdAt.withDateFormat()
            binding.tvItemDescription.text =
                if (data.description.count() > 20) "${data.description.substring(0, 20)} ...."
                else data.description
            Glide.with(this.itemView.context).load(data.photoUrl).into(binding.ivItemPhoto)
            binding.storyCard.setOnClickListener {
                val intent = Intent(this.itemView.context, DetailActivity::class.java)
                intent.putExtra(DetailActivity.EXTRA_ID, data.id)
                ContextCompat.startActivity(this.itemView.context, intent, Bundle.EMPTY)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}