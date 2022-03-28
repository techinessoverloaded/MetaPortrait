package com.apkaproj.metaportrait.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.apkaproj.metaportrait.adapters.ImageFiltersAdapter
import com.apkaproj.metaportrait.data.ImageFilter
import com.apkaproj.metaportrait.databinding.ActivityEditImageBinding
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.helpers.hide
import com.apkaproj.metaportrait.helpers.show
import com.apkaproj.metaportrait.listeners.ImageFilterListener
import com.apkaproj.metaportrait.viewmodels.EditImageViewModel
import jp.co.cyberagent.android.gpuimage.GPUImage
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditImageActivity : AppCompatActivity(), ImageFilterListener
{

    companion object {
        const val KEY_FILTERED_IMAGE_URI = "filteredImageUri"
    }
    private lateinit var binding : ActivityEditImageBinding
    private val viewModel : EditImageViewModel by viewModel()
    private lateinit var gpuImage : GPUImage
    // Image Bitmaps
    private lateinit var originalBitmap: Bitmap
    private val filteredBitmap = MutableLiveData<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        setupObservers()
        prepareImagePreview()
    }

    private fun setupObservers()
    {
        viewModel.imagePreviewUiState.observe(this) {
            val dataState = it ?: return@observe
            binding.previewProgressBar.visibility =
                if (dataState.isLoading) View.VISIBLE else View.GONE
            dataState.bitmap?.let { bitmap ->
                // for the first time 'filtered image = original'
                originalBitmap = bitmap
                filteredBitmap.value = bitmap

                with(originalBitmap) {
                    gpuImage.setImage(this)
                    binding.imagePreview.show()
                    viewModel.loadImageFilters(this)
                }
            } ?: kotlin.run {
                dataState.error?.let { error ->
                    displayToast(error)
                }
            }
        }
        viewModel.imageFiltersUiState.observe(this) {
            val imageFiltersDataState = it ?: return@observe
            binding.imageFiltersProgressBar.visibility =
                if(imageFiltersDataState.isLoading) View.VISIBLE else View.GONE
            imageFiltersDataState.imageFilters?.let { imageFilters ->
                ImageFiltersAdapter(imageFilters,this).also { adapter->
                    binding.filtersRecyclerView.adapter = adapter
                }
            } ?: run {
                imageFiltersDataState.error?.let { error ->
                    displayToast(error)
                }
            }
        }
        filteredBitmap.observe(this) { bitmap ->
            binding.imagePreview.setImageBitmap(bitmap)
        }
        viewModel.saveFilteredImageUiState.observe(this) {
            val saveFilteredImageDataState = it ?: return@observe
            if(saveFilteredImageDataState.isLoading)
            {
                binding.imageSave.hide()
                binding.savingProgressBar.show()
            }
            else
            {
                binding.savingProgressBar.hide()
                binding.imageSave.show()
            }
            saveFilteredImageDataState.uri?.let { savedImageUri ->
                Intent(this@EditImageActivity,
                    FilteredImageActivity::class.java).also { filteredImageIntent ->
                    filteredImageIntent.putExtra(KEY_FILTERED_IMAGE_URI,savedImageUri)
                    startActivity(filteredImageIntent)
                }
            } ?: run {
                saveFilteredImageDataState.error?.let { error ->
                    displayToast(error)
                }
            }
        }
    }

    private fun prepareImagePreview()
    {
        gpuImage = GPUImage(applicationContext)
        intent.getParcelableExtra<Uri>(MainActivity.KEY_IMAGE_URI)?.let { imageUri ->
            viewModel.prepareImagePreview(imageUri)
        }
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.imageSave.setOnClickListener {
            filteredBitmap.value?.let { bitmap ->
                viewModel.saveFilteredImageBitmap(bitmap)
            }
        }
        binding.imagePreview.setOnLongClickListener {
            binding.imagePreview.setImageBitmap(originalBitmap)
            return@setOnLongClickListener false
        }
        binding.imagePreview.setOnClickListener {
            binding.imagePreview.setImageBitmap(filteredBitmap.value)
        }
    }

    override fun onFilterSelected(imageFilter: ImageFilter)
    {
        with(imageFilter) {
            with(gpuImage) {
                setFilter(filter)
                filteredBitmap.value = bitmapWithFilterApplied
            }
        }
    }
}