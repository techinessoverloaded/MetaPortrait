package com.apkaproj.metaportrait.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import com.apkaproj.metaportrait.adapters.SavedImagesAdapter
import com.apkaproj.metaportrait.databinding.ActivitySavedImageBinding
import com.apkaproj.metaportrait.helpers.displayToast
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
        setListeners()
        viewModel.loadSavedImages()
    }

    private fun setupObserver()
    {
        viewModel.savedImagesUiState.observe(this) {
            val savedImageDataState = it ?: return@observe
            binding.savedImagesProgressBar.visibility =
                if(savedImageDataState.isLoading) View.VISIBLE
                else View.GONE
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