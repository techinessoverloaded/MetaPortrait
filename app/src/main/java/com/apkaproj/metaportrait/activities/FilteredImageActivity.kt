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
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import com.apkaproj.metaportrait.R
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

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

    private fun recognizeText(image: Bitmap)
    {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val visionImage = InputImage.fromBitmap(image, 0)
        recognizer.process(visionImage).addOnSuccessListener { result ->
            if(result.text.isEmpty())
            {
                binding.imageProcessingProgressBar.hide()
                displayToast("No text was detected in the image !")
            }
            else
            {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)
                highlightText(result, mutableImage)
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            binding.imageProcessingProgressBar.hide()
            displayToast("An error occurred while recognizing text !")
        }
    }

    private fun highlightText(result: Text?, image: Bitmap?)
    {
        if (result == null || image == null)
        {
            displayToast("Error occurred while highlighting text !")
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
        binding.clearLabelHighlightBtn.setText(R.string.clear_text_highlights)
        binding.clearLabelHighlightBtn.show()
        binding.imageProcessingProgressBar.hide()
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

    private fun callFaceRecognizer(image: Bitmap)
    {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .enableTracking()
            .build()
        val faceDetector = FaceDetection.getClient(options)
        val inputImage = InputImage.fromBitmap(image, 0)
        faceDetector.process(inputImage).addOnSuccessListener { faces ->
            if(faces.isEmpty())
            {
                binding.imageProcessingProgressBar.hide()
                displayToast("No faces were detected in the image !")
            }
            else
            {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)
                detectFaces(faces, mutableImage)
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            binding.imageProcessingProgressBar.hide()
            displayToast("An error occurred while detecting faces !")
        }
    }

    private fun detectFaces(faces: List<Face>?, image: Bitmap?)
    {
        if(faces == null || image == null)
        {
            displayToast("Error occurred while detecting faces !")
            return
        }
        val canvas = Canvas(image)
        val facePaint = Paint()
        facePaint.color = Color.RED
        facePaint.style = Paint.Style.STROKE
        facePaint.strokeWidth = 8F
        val faceTextPaint = Paint()
        faceTextPaint.color = Color.RED
        faceTextPaint.textSize = 40F
        faceTextPaint.typeface = Typeface.DEFAULT_BOLD
        for ((index, face) in faces.withIndex())
        {
            canvas.drawRect(face.boundingBox, facePaint)
            canvas.drawText("Face${index + 1}", (face.boundingBox.centerX() - face.boundingBox.width() / 2) + 8F, (face.boundingBox.centerY() + face.boundingBox.height() / 2) - 8F, faceTextPaint)
        }
        binding.imageFilteredImage.setImageBitmap(image)
        binding.clearLabelHighlightBtn.setText(R.string.clear_highlighted_faces)
        binding.clearLabelHighlightBtn.show()
        binding.imageProcessingProgressBar.hide()
    }

    private fun callImageLabelDetector(image: Bitmap)
    {
        val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val inputImage = InputImage.fromBitmap(image, 0)
        imageLabeler.process(inputImage).addOnSuccessListener { labels ->
            if(labels.isEmpty())
            {
                binding.imageProcessingProgressBar.hide()
                displayToast("Couldn't label anything in Image !")
            }
            else
            {
                labelImage(labels)
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            binding.imageProcessingProgressBar.hide()
            displayToast("An error occurred while labeling image !")
        }
    }

    private fun labelImage(labels: List<ImageLabel>?)
    {
        if(labels == null)
        {
            displayToast("Error occurred while labeling image !")
            return
        }
        var resultString = ""
        var index = 1
        for(label in labels)
        {
            resultString += "${index++} - ${label.text}\n"
        }
        val binding3 = LayoutOcrResultBinding.inflate(layoutInflater)
        binding3.ocrResultTextView.text = resultString
        binding.imageProcessingProgressBar.hide()
        AlertDialog.Builder(this)
            .setTitle("Image Labeling Result")
            .setView(binding3.root)
            .setNegativeButton("Close") { dialog , _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun clearImageHighlights()
    {
        binding.imageFilteredImage.setImageURI(fileUri)
    }

    private fun getBitmapFromUri(): Bitmap?
    {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return bitmap
    }

    private fun setListeners()
    {
        binding.fabShare.setOnClickListener {
            clearImageHighlights()
            with(Intent(Intent.ACTION_SEND)) {
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "image/*"
                startActivity(this)
            }
        }

        binding.fabOCR.setOnClickListener {
            binding.imageProcessingProgressBar.show()
            val bitmap = getBitmapFromUri()
            if(bitmap == null)
            {
                displayToast("Error in detecting text !")
            }
            else
            {
                recognizeText(bitmap)
            }
        }

        binding.fabFacialDetection.setOnClickListener {
            binding.imageProcessingProgressBar.show()
            val bitmap = getBitmapFromUri()
            if(bitmap == null)
            {
                displayToast("Error in detecting faces !")
            }
            else
            {
                callFaceRecognizer(bitmap)
            }
        }

        binding.fabImageLabel.setOnClickListener {
            binding.imageProcessingProgressBar.show()
            val bitmap = getBitmapFromUri()
            if(bitmap == null)
            {
                displayToast("Error in labeling image !")
            }
            else
            {
                callImageLabelDetector(bitmap)
            }
        }

        binding.clearLabelHighlightBtn.setOnClickListener { button ->
            clearImageHighlights()
            button.hide()
        }

        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.imageRename.setOnClickListener {
            renameDialog.show()
        }
    }

    override fun onBackPressed()
    {
        if(isTaskRoot)
        {
            Intent(this@FilteredImageActivity, SavedImageActivity::class.java).also { intent ->
                finish()
                startActivity(intent)
            }
        }
        else
        {
            super.onBackPressed()
        }
    }
}