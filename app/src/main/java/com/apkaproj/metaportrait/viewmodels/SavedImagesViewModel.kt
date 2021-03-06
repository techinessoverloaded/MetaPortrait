package com.apkaproj.metaportrait.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apkaproj.metaportrait.models.Image
import com.apkaproj.metaportrait.helpers.Coroutines
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.apkaproj.metaportrait.repositories.SavedImagesRepository
import com.google.firebase.storage.StorageReference

class SavedImagesViewModel(private val savedImagesRepository: SavedImagesRepository) : ViewModel()
{
    private val savedImagesDataState = MutableLiveData<SavedImagesDataState>()
    val savedImagesUiState : LiveData<SavedImagesDataState> get() = savedImagesDataState

    fun loadSavedImages()
    {
        Coroutines.io {
            runCatching {
                emitSavedImagesUiState(isLoading = true)
                savedImagesRepository.loadSavedImages()
            }.onSuccess { savedImages ->
                if(savedImages.isNullOrEmpty())
                {
                    emitSavedImagesUiState(error = "No Images found")
                }
                else
                {
                    emitSavedImagesUiState(savedImages = savedImages)
                }
            }.onFailure {
                emitSavedImagesUiState(error = it.message.toString())
            }
        }
    }

    private fun emitSavedImagesUiState (
        isLoading: Boolean = false,
        savedImages: List<Image>? = null,
        error: String? = null
    ) {
        val dataState = SavedImagesDataState(isLoading, savedImages, error)
        savedImagesDataState.postValue(dataState)
    }


    data class SavedImagesDataState (
        val isLoading : Boolean,
        val savedImages : List<Image>?,
        val error : String?
    )
}