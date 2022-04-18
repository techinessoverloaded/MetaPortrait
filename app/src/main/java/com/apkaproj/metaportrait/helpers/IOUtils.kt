package com.apkaproj.metaportrait.helpers

import android.content.Context
import android.os.Environment
import java.io.File

class IOUtils(private val context: Context)
{
    private lateinit var listOfFileNameMillis: ArrayList<String>

    private fun loadFileNames()
    {
        listOfFileNameMillis = ArrayList()
        val dir = getImagesDirectoryAsFile()
        dir.listFiles()?.let { data ->
            data.forEach { file ->
                val fileNameWithoutExtension = file.nameWithoutExtension
                val millisInFileName = fileNameWithoutExtension.substring(fileNameWithoutExtension.indexOf("METAPORTRAIT_IMG_")+1)
                listOfFileNameMillis.add(millisInFileName)
            }
        }
    }

    fun getUniqueFileName(): String
    {
        loadFileNames()
        var uniqueTimeMillis = System.currentTimeMillis()
        while(listOfFileNameMillis.contains(uniqueTimeMillis.toString()))
        {
            uniqueTimeMillis = System.currentTimeMillis()
        }
        return "MP_IMG_${uniqueTimeMillis}.png"
    }

    fun getImagesDirectoryAsFile(): File
    {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MetaPortrait Saved Images")
    }

}