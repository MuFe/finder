package com.camera.finder.di





import com.camera.finder.MainActivityViewModel
import com.camera.finder.ui.ScanViewModel
import com.camera.finder.ui.SettingViewModel

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainActivityViewModel(get()) }
    viewModel { ScanViewModel(get(),get()) }
    viewModel { SettingViewModel() }
}

