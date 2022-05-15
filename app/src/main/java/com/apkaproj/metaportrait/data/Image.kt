package com.apkaproj.metaportrait.data

import android.graphics.Bitmap
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

data class Image(val bitmap: Bitmap, val file: File, var isChecked: Boolean = false)
{
    fun getLastModifiedTime() : String?
    {
        if(file.exists())
        {
            val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(attributes.lastModifiedTime().toMillis())
        }
        return null
    }

    fun getCreatedTime() : String?
    {
        if(file.exists())
        {
            val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(attributes.creationTime().toMillis())
        }
        return null
    }
}