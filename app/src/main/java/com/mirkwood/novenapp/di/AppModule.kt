package com.mirkwood.novenapp.di

import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.data.devotional.DevotionalRepositoryImpl
import com.mirkwood.novenapp.data.progress.DevotionalProgressStore
import com.mirkwood.novenapp.presentation.MainViewModel
import com.mirkwood.novenapp.presentation.screens.lyrics.LyricsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DevotionalRepository> { DevotionalRepositoryImpl(get()) }
    single { DevotionalProgressStore(get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { LyricsViewModel() }
}
