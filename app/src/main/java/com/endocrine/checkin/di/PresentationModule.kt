package com.endocrine.checkin.di

import com.endocrine.checkin.presentation.checkin.CheckinViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Presentation-layer bindings (ViewModels). */
val presentationModule: Module = module {
    viewModelOf(::CheckinViewModel)
}

/** All Koin modules, assembled here and registered in [com.endocrine.checkin.CheckInApplication]. */
val appModules: List<Module> = listOf(dataModule, domainModule, presentationModule)
