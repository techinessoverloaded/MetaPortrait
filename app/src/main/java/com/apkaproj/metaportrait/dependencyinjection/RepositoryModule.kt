package com.apkaproj.metaportrait.dependencyinjection

import com.apkaproj.metaportrait.repositories.EditImageRepository
import com.apkaproj.metaportrait.repositories.EditImageRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    factory<EditImageRepository> { EditImageRepositoryImpl(androidContext()) }
}
