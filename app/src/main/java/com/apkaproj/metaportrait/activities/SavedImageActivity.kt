package com.apkaproj.metaportrait.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.FileProvider
import com.apkaproj.metaportrait.adapters.SavedImagesAdapter
import com.apkaproj.metaportrait.databinding.ActivitySavedImageBinding
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.helpers.hide
import com.apkaproj.metaportrait.helpers.show
import com.apkaproj.metaportrait.listeners.SavedImagesListener
import com.apkaproj.metaportrait.viewmodels.SavedImagesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SavedImageActivity : AppCompatActivity(), SavedImagesListener
{
    private lateinit var binding : ActivitySavedImageBinding
    private val viewModel : SavedImagesViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupObserver()
        viewModel.loadSavedImages()
        setListeners()
    }

    private fun setupObserver()
    {
        viewModel.savedImagesUiState.observe(this) {
            val savedImageDataState = it ?: return@observe
            if(savedImageDataState.isLoading)
                binding.savedImagesProgressBar.show()
            else
                binding.savedImagesProgressBar.hide()

            savedImageDataState.savedImages?.let { savedImages ->
                SavedImagesAdapter(savedImages, this).also { adapter ->
                    with(binding.savedImagesRecyclerView) {
                        this.adapter = adapter
                        this.show()
                    }
                }
            } ?: run {
                savedImageDataState.error?.let { error ->
                    displayToast(error)
                }
            }
        }
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onImageClicked(file: File)
    {
        val fileUri = FileProvider.getUriForFile(
            applicationContext,
            "${packageName}.provider",
            file
        )
        Intent(
            this@SavedImageActivity,
            FilteredImageActivity::class.java
        ).also { filteredImageIntent ->
            filteredImageIntent.putExtra(EditImageActivity.KEY_FILTERED_IMAGE_URI, fileUri)
            startActivity(filteredImageIntent)
        }
    }
}