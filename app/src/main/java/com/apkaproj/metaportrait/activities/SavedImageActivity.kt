package com.apkaproj.metaportrait.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.apkaproj.metaportrait.databinding.ActivitySavedImageBinding
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.viewmodels.SavedImagesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SavedImageActivity : AppCompatActivity()
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
                displayToast("${savedImages.size} Images loaded")
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
}