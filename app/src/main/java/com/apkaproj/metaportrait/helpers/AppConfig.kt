package com.apkaproj.metaportrait.helpers

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.apkaproj.metaportrait.dependencyinjection.repositoryModule
import com.apkaproj.metaportrait.dependencyinjection.viewModelModule

@Suppress("unused")
class AppConfig : Application()
{
    override fun onCreate()
    {
        super.onCreate()
        startKoin {
            androidContext(this@AppConfig)
            modules(listOf(repositoryModule, viewModelModule))
        }
    }
}