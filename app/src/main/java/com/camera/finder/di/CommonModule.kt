package com.camera.finder.di


import com.camera.finder.util.GoogleUtil
import com.camera.finder.util.MyNetworkUtil
import com.camera.finder.util.PreferenceUtil
import com.camera.finder.util.RateControl
import com.camera.finder.util.ScanningUtil
import org.koin.dsl.module

val commonModule = module {
    single { PreferenceUtil(get()) }
    single { ScanningUtil(get()) }
    factory { MyNetworkUtil(get(),get()) }
    single { GoogleUtil(get(),get()) }
    factory { RateControl() }
}
