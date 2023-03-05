package com.camera.finder

import android.app.Application
import com.camera.finder.di.commonModule
import com.camera.finder.di.networkModule
import com.camera.finder.di.viewModelModule
import com.mufe.mvvm.library.BaseApplication

import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class MyApplication() : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        loadKoinModules(
            arrayListOf(
                viewModelModule,
                networkModule,
                commonModule,
            )
        )

    }


}