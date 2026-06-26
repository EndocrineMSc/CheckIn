package com.endocrine.checkin.di

import com.endocrine.checkin.domain.usecase.DeleteCheckinUseCase
import com.endocrine.checkin.domain.usecase.DeleteReminderUseCase
import com.endocrine.checkin.domain.usecase.GetCheckinUseCase
import com.endocrine.checkin.domain.usecase.ObserveHistoryUseCase
import com.endocrine.checkin.domain.usecase.ObserveRemindersUseCase
import com.endocrine.checkin.domain.usecase.RescheduleRemindersUseCase
import com.endocrine.checkin.domain.usecase.SaveCheckinUseCase
import com.endocrine.checkin.domain.usecase.SetReminderEnabledUseCase
import com.endocrine.checkin.domain.usecase.UpsertReminderUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Domain-layer bindings (use cases — stateless, so `factory`). */
val domainModule: Module = module {
    // Check-in
    factoryOf(::SaveCheckinUseCase)
    factoryOf(::ObserveHistoryUseCase)
    factoryOf(::GetCheckinUseCase)
    factoryOf(::DeleteCheckinUseCase)

    // Reminders
    factoryOf(::ObserveRemindersUseCase)
    factoryOf(::UpsertReminderUseCase)
    factoryOf(::DeleteReminderUseCase)
    factoryOf(::SetReminderEnabledUseCase)
    factoryOf(::RescheduleRemindersUseCase)
}
