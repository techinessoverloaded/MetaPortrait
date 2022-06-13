package com.apkaproj.metaportrait.repositories

import com.apkaproj.metaportrait.models.Image
import com.google.firebase.storage.StorageReference

interface SavedImagesRepository
{
    suspend fun loadSavedImages() : List<Image>?
    suspend fun syncSavedImages(userFolderReference: StorageReference) : Boolean
}