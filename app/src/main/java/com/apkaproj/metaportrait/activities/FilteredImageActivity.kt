package com.apkaproj.metaportrait.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.apkaproj.metaportrait.databinding.ActivityFilteredImageBinding
import com.apkaproj.metaportrait.databinding.LayoutOcrResultBinding
import com.apkaproj.metaportrait.databinding.LayoutRenameImageBinding
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.helpers.hide
import com.apkaproj.metaportrait.helpers.show
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class FilteredImageActivity : AppCompatActivity()
{
    private lateinit var fileUri : Uri
    private lateinit var binding : ActivityFilteredImageBinding
    private lateinit var renameDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityFilteredImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        displayFilteredImage()
        renameDialog = getRenameDialog()
        setListeners()
    }

    private fun getRenameDialog(): AlertDialog
    {
        val binding2 = LayoutRenameImageBinding.inflate(layoutInflater)
        binding2.renameImageEditText.setText(binding.imageNameView.text.substring(0,binding.imageNameView.text.indexOf(".png")))
        return AlertDialog.Builder(this).setTitle("Rename Image File")
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
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
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

    private fun recognizeText(image: Bitmap?)
    {
        if (image == null)
        {
            displayToast("There was some error in recognizing text!")
            return
        }
//        binding.imageFilteredImage.setImageBitmap(null)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val visionImage: InputImage = InputImage.fromBitmap(image, 0)
        recognizer.process(visionImage).addOnSuccessListener { result ->
            binding.imageProcessingProgressBar.hide()
            val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)
            highlightText(result, mutableImage)
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            binding.imageProcessingProgressBar.hide()
            displayToast("An error occurred !")
        }
    }

    private fun highlightText(result: Text?, image: Bitmap?)
    {
        if (result == null || image == null)
        {
            return
        }
        val binding3 = LayoutOcrResultBinding.inflate(layoutInflater)
        val canvas = Canvas(image)
        val rectPaint = Paint()
        rectPaint.color = Color.RED
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 4F
        val textPaint = Paint()
        textPaint.color = Color.RED
        textPaint.textSize = 40F
        var index = 1
        var resultMap: HashMap<Int, String> = HashMap()
        for(block in result.textBlocks)
        {
            for(line in block.lines)
            {
                canvas.drawRect(line.boundingBox!!, rectPaint)
                canvas.drawText(index.toString(), line.cornerPoints!![2].x.toFloat(),
                    line.cornerPoints!![2].y.toFloat(), textPaint)
                resultMap[index++] = "${line.text}\n"
            }
        }
        for (pair in resultMap.entries)
        {
            binding3.ocrResultTextView.text = binding3.ocrResultTextView.text.toString() + "${pair.key} - ${pair.value}"
        }
        binding.imageFilteredImage.setImageBitmap(image)
        binding.clearLabelHighlightBtn.show()
        AlertDialog.Builder(this)
            .setTitle("Text Recognition Result")
            .setView(binding3.root)
            .setPositiveButton("Copy to Clipboard") { dialog , _ ->
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("MetaPortrait OCR Result", resultMap.values.joinToString())
                clipboardManager.setPrimaryClip(clipData)
                displayToast("Text Recognition Result copied to Clipboard successfully !")
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog , _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
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

        binding.fabOCR.setOnClickListener {
            binding.imageProcessingProgressBar.show()
            val parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
            val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
            recognizeText(bitmap)
        }

        binding.fabFacialDetection.setOnClickListener {

        }

        binding.fabImageLabel.setOnClickListener {

        }

        binding.clearLabelHighlightBtn.setOnClickListener { button ->
            binding.imageFilteredImage.setImageURI(fileUri)
            button.hide()
        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.imageRename.setOnClickListener {
            renameDialog.show()
        }
    }
}