package com.apkaproj.metaportrait.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.apkaproj.metaportrait.helpers.EncryptionUtils
import com.apkaproj.metaportrait.models.Image
import com.apkaproj.metaportrait.helpers.IOUtils
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileOutputStream

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

    private fun getPreviewBitmap(file : File) : Bitmap
    {
        val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)
        val width = 150
        val height = ((originalBitmap.height * width) / originalBitmap.width)
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    }
}