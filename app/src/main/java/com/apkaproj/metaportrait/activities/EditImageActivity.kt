package com.apkaproj.metaportrait.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var cropImageActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        setupObservers()
        prepareImagePreview()
        cropImageActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            with(result)
            {
                if(resultCode == RESULT_OK)
                {
                    val croppedBitmap = data?.extras?.getParcelable<Bitmap>("data")
                    binding.imagePreview.setImageBitmap(croppedBitmap)
                    originalBitmap = croppedBitmap ?: originalBitmap
                    filteredBitmap.value = croppedBitmap ?: filteredBitmap.value
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
                        Thread.sleep(5000)
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
            cropImage(tempUri!!)
        }
    }

    private fun cropImage(uri: Uri)
    {
        try
        {
            val cropIntent = Intent("com.android.camera.action.CROP")
            cropIntent.setDataAndType(uri,"image/*")
            cropIntent.putExtra("crop","true")
            cropIntent.putExtra("outputX",180)
            cropIntent.putExtra("outputY",180)
            cropIntent.putExtra("aspectX",3)
            cropIntent.putExtra("aspectY",4)
            cropIntent.putExtra("scaleUpIfNeeded",true)
            cropIntent.putExtra("return-data",true)
            cropIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            cropImageActivityLauncher.launch(cropIntent)
        }
        catch(e: ActivityNotFoundException)
        {
            displayToast("Unable to Crop Image ! An error occurred !")
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