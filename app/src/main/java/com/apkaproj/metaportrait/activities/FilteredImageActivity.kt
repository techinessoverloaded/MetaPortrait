package com.apkaproj.metaportrait.activities

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.apkaproj.metaportrait.databinding.ActivityFilteredImageBinding
import com.apkaproj.metaportrait.databinding.LayoutRenameImageBinding
import com.apkaproj.metaportrait.helpers.displayToast
import java.io.File

class FilteredImageActivity : AppCompatActivity()
{
    private lateinit var fileUri : Uri
    private lateinit var binding : ActivityFilteredImageBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityFilteredImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        displayFilteredImage()
        setListeners()
    }

    private fun displayFilteredImage()
    {
        intent.getParcelableExtra<Uri>(EditImageActivity.KEY_FILTERED_IMAGE_URI)?.let { imageUri ->
            fileUri = imageUri
            binding.imageFilteredImage.setImageURI(imageUri)
            contentResolver.query(fileUri,null,
                null,null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                binding.imageNameView.text = cursor.getString(nameIndex)
            }
        }
    }

    private fun setListeners()
    {
        binding.fabShare.setOnClickListener {
            with(Intent(Intent.ACTION_SEND)) {
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "image/*"
                startActivity(this)
            }
        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.imageRename.setOnClickListener {
            val binding2 = LayoutRenameImageBinding.inflate(layoutInflater)
            binding2.renameImageEditText.setText(binding.imageNameView.text.substring(0,binding.imageNameView.text.indexOf(".png")))
            AlertDialog.Builder(this).setTitle("Rename Image File")
                .setView(binding2.root)
                .setPositiveButton("Rename") { dialog, _ ->
                    val newNameFile = File(File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MetaPortrait Saved Images"),
                        "${binding2.renameImageEditText.text}.png")

                    val existingFile = File(File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MetaPortrait Saved Images"),
                        binding.imageNameView.text.toString())

                    existingFile.renameTo(newNameFile).also {
                        if(it)
                        {
                            fileUri = FileProvider.getUriForFile(applicationContext, "${packageName}.provider", newNameFile)
                            binding.imageNameView.text = newNameFile.name
                            dialog.dismiss()
                        }
                        else
                        {
                            dialog.dismiss()
                            displayToast("An error Occurred ! Unable to rename file !")
                        }
                    }
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.setCancelable(false).create().also {
                    it.show()
                }
        }
    }
}