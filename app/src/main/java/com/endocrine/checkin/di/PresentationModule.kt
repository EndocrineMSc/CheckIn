package com.endocrine.checkin.di

import org.koin.core.module.Module
import org.koin.dsl.module

/** Presentation-layer bindings (ViewModels). Filled by later steps. */
val presentationModule: Module = module {
}

/** All Koin modules, assembled here and registered in [com.endocrine.checkin.CheckInApplication]. */
val appModules: List<Module> = listOf(dataModule, domainModule, presentationModule)
