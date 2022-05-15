package com.apkaproj.metaportrait.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.apkaproj.metaportrait.data.Image
import com.apkaproj.metaportrait.databinding.ItemContainerSavedImageBinding
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.listeners.ImageSelectionListener
import com.apkaproj.metaportrait.listeners.SavedImagesListener

class SavedImagesAdapter(private val savedImages: List<Image>,
                         private val savedImagesListener : SavedImagesListener,
                         private val context: Context,
                         private val imageSelectionListener: ImageSelectionListener):
RecyclerView.Adapter<SavedImagesAdapter.SavedImageViewHolder>()
{
    private val noOfSelectedImages: Int
        get() = selectedImages.size

    private val selectedImages: List<Image>
        get()
        {
            return savedImages.filter { image ->
                image.isChecked
            }
        }

    init
    {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedImageViewHolder
    {
        val binding = ItemContainerSavedImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return SavedImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedImageViewHolder, position: Int)
    {
        with(holder) {
            with(savedImages[position]) {
                binding.imageSaved.setImageBitmap(bitmap)
                if(isChecked)
                {
                    binding.imageSaved.setColorFilter(context.getColor(R.color.img_selector), PorterDuff.Mode.SRC_ATOP)
                }
                else
                {
                    binding.imageSaved.colorFilter = null
                }
                binding.root.setOnLongClickListener {
                    if(noOfSelectedImages == 0)
                    {
                        isChecked = true
                        binding.imageSaved.setColorFilter(context.getColor(R.color.img_selector), PorterDuff.Mode.SRC_ATOP)
                        imageSelectionListener.onImageSelected(true, noOfSelectedImages)
                        return@setOnLongClickListener true
                    }
                    return@setOnLongClickListener false
                }
                binding.root.setOnClickListener {
                    if(noOfSelectedImages > 0)
                    {
                        if(isChecked)
                        {
                            isChecked = false
                            binding.imageSaved.colorFilter = null
                            if(selectedImages.isEmpty())
                            {
                                imageSelectionListener.onImageSelected(false, 0)
                            }
                            else
                            {
                                imageSelectionListener.onImageSelected(false, noOfSelectedImages)
                            }
                        }
                        else
                        {
                            isChecked = true
                            binding.imageSaved.setColorFilter(
                                context.getColor(R.color.img_selector),
                                PorterDuff.Mode.SRC_ATOP
                            )
                            imageSelectionListener.onImageSelected(true, noOfSelectedImages)
                        }
                    }
                    else
                    {
                        savedImagesListener.onImageClicked(file)
                    }
                }
            }
        }
    }

    override fun getItemCount() = savedImages.size

    override fun getItemId(position: Int): Long = position.toLong()

    fun deleteSelectedImages()
    {
        if(noOfSelectedImages > 0)
        {
            selectedImages.forEach { image ->
                if(image.file.delete())
                {
                    (savedImages as ArrayList).remove(image)
                    notifyDataSetChanged()
                }
                else
                {
                    Toast.makeText(context, "Failed to delete ${image.file.name} !", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun clearSelection()
    {
        savedImages.forEach { image ->
            if(image.isChecked)
                image.isChecked = false
        }
    }

    inner class SavedImageViewHolder(val binding: ItemContainerSavedImageBinding) : RecyclerView.ViewHolder(binding.root)
}
