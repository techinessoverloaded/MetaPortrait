package com.apkaproj.metaportrait.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apkaproj.metaportrait.databinding.ItemContainerSavedImageBinding
import com.apkaproj.metaportrait.listeners.SavedImagesListener
import java.io.File

class SavedImagesAdapter(private val savedImages: List<Pair<File, Bitmap>>,
                         private val savedImagesListener : SavedImagesListener) :
RecyclerView.Adapter<SavedImagesAdapter.SavedImageViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedImageViewHolder {
        val binding = ItemContainerSavedImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SavedImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedImageViewHolder, position: Int) {
        with(holder) {
            with(savedImages[position]) {
                binding.imageSaved.setImageBitmap(second)
                binding.imageSaved.setOnClickListener {
                    savedImagesListener.onImageClicked(first)
                }
            }
        }
    }

    override fun getItemCount() = savedImages.size

    inner class SavedImageViewHolder(val binding: ItemContainerSavedImageBinding) :
            RecyclerView.ViewHolder(binding.root)
}