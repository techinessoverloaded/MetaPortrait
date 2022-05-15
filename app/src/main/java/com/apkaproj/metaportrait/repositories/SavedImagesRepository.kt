package com.apkaproj.metaportrait.repositories

import com.apkaproj.metaportrait.data.Image

interface SavedImagesRepository
{
    suspend fun loadSavedImages() : List<Image>?
}