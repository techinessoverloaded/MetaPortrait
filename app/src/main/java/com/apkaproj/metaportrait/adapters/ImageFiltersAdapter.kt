package com.apkaproj.metaportrait.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.data.ImageFilter
import com.apkaproj.metaportrait.databinding.ItemContainerFilterBinding
import com.apkaproj.metaportrait.listeners.ImageFilterListener

class ImageFiltersAdapter(
    private val imageFilters : List<ImageFilter>,
    private val imageFilterListener: ImageFilterListener) :
RecyclerView.Adapter<ImageFiltersAdapter.ImageFiltersViewHolder>()
{
    private var selectedFilterPosition = 0
    private var previouslySelectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFiltersViewHolder {
        val binding = ItemContainerFilterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageFiltersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageFiltersViewHolder, position: Int) {
        with(holder) {
            with(imageFilters[adapterPosition]) {
                binding.imageFilterPreview.setImageBitmap(filterPreview)
                binding.textFilterName.text = name
                binding.root.setOnClickListener{
                    if(position != selectedFilterPosition)
                    {
                        imageFilterListener.onFilterSelected(this)
                        previouslySelectedPosition = selectedFilterPosition
                        selectedFilterPosition = adapterPosition
                        with(this@ImageFiltersAdapter) {
                            notifyItemChanged(previouslySelectedPosition, Unit)
                            notifyItemChanged(selectedFilterPosition, Unit)
                        }
                    }
                }
            }
            binding.textFilterName.setTextColor(
                ContextCompat.getColor(binding.textFilterName.context,
                if(selectedFilterPosition == position)
                    R.color.primaryDark
                else
                    R.color.primaryText
                )
            )
        }
    }

    override fun getItemCount() = imageFilters.size

    inner class ImageFiltersViewHolder(val binding: ItemContainerFilterBinding) :
            RecyclerView.ViewHolder(binding.root)
}