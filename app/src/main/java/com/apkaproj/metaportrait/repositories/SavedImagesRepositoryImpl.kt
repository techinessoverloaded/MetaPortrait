package com.apkaproj.metaportrait.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.apkaproj.metaportrait.models.Image
import com.apkaproj.metaportrait.helpers.IOUtils
import com.google.firebase.storage.StorageReference
import java.io.File

class SavedImagesRepositoryImpl(private val context : Context) : SavedImagesRepository
{
    override suspend fun loadSavedImages(): List<Image>?
    {
        val ioUtils = IOUtils(context)
        val savedImages = ArrayList<Image>()
        val dir = ioUtils.getImagesDirectoryAsFile()
        dir.listFiles()?.let { data ->
            data.forEach { file ->
                savedImages.add(Image(file = file, bitmap = getPreviewBitmap(file)))
            }
            return savedImages
        } ?: return null
    }

    override suspend fun syncSavedImages(userFolderReference: StorageReference) : Boolean
    {

    }

    private fun getPreviewBitmap(file : File) : Bitmap
    {
        val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
        val width = 150
        val height = ((originalBitmap.height * width) / originalBitmap.width)
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    }
}