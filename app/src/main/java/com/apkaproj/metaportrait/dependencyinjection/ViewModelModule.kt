package com.apkaproj.metaportrait.dependencyinjection

import com.apkaproj.metaportrait.viewmodels.EditImageViewModel
import com.apkaproj.metaportrait.viewmodels.SavedImagesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { EditImageViewModel(editImageRepository = get()) }
    viewModel { SavedImagesViewModel(savedImagesRepository = get()) }
}