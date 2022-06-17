package com.apkaproj.metaportrait.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.apkaproj.metaportrait.adapters.SavedImagesAdapter
import com.apkaproj.metaportrait.databinding.ActivitySavedImageBinding
import com.apkaproj.metaportrait.helpers.*
import com.apkaproj.metaportrait.listeners.ImageSelectionListener
import com.apkaproj.metaportrait.listeners.SavedImagesListener
import com.apkaproj.metaportrait.viewmodels.SavedImagesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SavedImageActivity : AppCompatActivity(), SavedImagesListener, ImageSelectionListener
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
                SavedImagesAdapter(savedImages, this, this, this).also { savedImagesAdapter ->
                    with(binding.savedImagesRecyclerView)
                    {
                        this.adapter = savedImagesAdapter
                        this.show()
                    }
                }
            } ?:
            run {
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
        binding.deleteImageButton.setOnClickListener {
            val deleteConfirmationDialog: AlertDialog = AlertDialog.Builder(this)
                .setTitle("Confirm Deletion of Images")
                .setMessage("Are you sure that you want to delete the Selected Images ?")
                .setPositiveButton("Yes") { dialog, _ ->
                    (binding.savedImagesRecyclerView.adapter as SavedImagesAdapter).deleteSelectedImages()
                    binding.noOfImgSelectedText.hide()
                    binding.deleteImageButton.hide()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){ dialog, _ ->
                    (binding.savedImagesRecyclerView.adapter as SavedImagesAdapter).clearSelection()
                    dialog.dismiss()
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                .create()
            deleteConfirmationDialog.show()
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

    @SuppressLint("SetTextI18n")
    override fun onImageSelected(isSelected: Boolean, noOfSelectedImages: Int)
    {
        if(isSelected)
        {
            if(binding.noOfImgSelectedText.isGone())
                binding.noOfImgSelectedText.show()
            if(binding.deleteImageButton.isGone())
                binding.deleteImageButton.show()
            binding.noOfImgSelectedText.text = "$noOfSelectedImages Items Selected"
        }
        else
        {
            binding.noOfImgSelectedText.text = "$noOfSelectedImages Items Selected"
            if(noOfSelectedImages == 0)
            {
                binding.noOfImgSelectedText.hide()
                binding.deleteImageButton.hide()
            }
        }
    }
}