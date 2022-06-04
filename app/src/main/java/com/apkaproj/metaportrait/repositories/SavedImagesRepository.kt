package com.apkaproj.metaportrait.repositories

import com.apkaproj.metaportrait.models.Image

interface SavedImagesRepository
{
    suspend fun loadSavedImages() : List<Image>?
}