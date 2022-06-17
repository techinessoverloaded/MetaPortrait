package com.apkaproj.metaportrait.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.apkaproj.metaportrait.adapters.ImageFiltersAdapter
import com.apkaproj.metaportrait.models.ImageFilter
import com.apkaproj.metaportrait.databinding.ActivityEditImageBinding
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.helpers.hide
import com.apkaproj.metaportrait.helpers.show
import com.apkaproj.metaportrait.listeners.ImageFilterListener
import com.apkaproj.metaportrait.viewmodels.EditImageViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.options
import jp.co.cyberagent.android.gpuimage.GPUImage
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


class EditImageActivity : AppCompatActivity(), ImageFilterListener
{
    companion object {
        const val KEY_FILTERED_IMAGE_URI = "filteredImageUri"
    }
    private var tempUri: Uri? = null
    private lateinit var binding : ActivityEditImageBinding
    private val viewModel : EditImageViewModel by viewModel()
    private lateinit var gpuImage : GPUImage
    private lateinit var originalBitmap: Bitmap
    private val filteredBitmap = MutableLiveData<Bitmap>()
    private var isFromCamera: Boolean = false
    private lateinit var cropImageActivityLauncher: ActivityResultLauncher<CropImageContractOptions>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        setupObservers()
        prepareImagePreview()
        cropImageActivityLauncher = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful)
            {
                if(result.uriContent == null)
                {
                    displayToast("Error in retrieving cropped Image !")
                }
                else
                {
                    prepareCroppedImagePreview(result.uriContent!!)
                }
            }
            else
            {
                val exception = result.error
                if(exception != null)
                {
                    exception.printStackTrace()
                    displayToast("Error occurred while cropping Image !")
                }
            }
        }
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
                    finish()
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
        isFromCamera = intent.getBooleanExtra(MainActivity.IS_FROM_CAMERA, false)
        intent.getParcelableExtra<Uri>(MainActivity.KEY_IMAGE_URI)?.let { imageUri ->
            viewModel.prepareImagePreview(imageUri)
            tempUri = imageUri
        }
    }

    private fun prepareCroppedImagePreview(uri: Uri)
    {
        viewModel.prepareImagePreview(uri)
        tempUri = uri
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.imageSave.setOnClickListener {
            filteredBitmap.value?.let { bitmap ->
                if (isFromCamera)
                {
                    tempUri?.path?.let {
                        var path = it
                        path = path.substring(path.indexOf("/external_files/")+1)
                        displayToast(path)
                        File(path).delete().also { successful ->
                            if (successful)
                            {
                                displayToast("Original File deleted")
                            }
                            else
                            {
                                displayToast("Unable to delete file")
                            }
                        }
                    }
                }
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

        binding.cropButton.setOnClickListener {
            cropImageActivityLauncher.launch(
                options(uri = tempUri) {
                    setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                    setActivityTitle("Crop Image")
                    setAllowFlipping(true)
                    setAllowRotation(true)
                    setAllowCounterRotation(true)
                }
            )
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