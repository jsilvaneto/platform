package com.platform.app.di

import com.platform.app.data.repository.FinancialRepositoryImpl
import com.platform.app.domain.repository.FinancialRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFinancialRepository(
        financialRepositoryImpl: FinancialRepositoryImpl
    ): FinancialRepository

    @Binds
    @Singleton
    abstract fun bindAddressLookupRepository(
        addressLookupRepositoryImpl: com.platform.app.data.repository.AddressLookupRepositoryImpl
    ): com.platform.app.domain.repository.AddressLookupRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        goalRepositoryImpl: com.platform.app.data.repository.GoalRepositoryImpl
    ): com.platform.app.domain.repository.GoalRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: com.platform.app.data.repository.BudgetRepositoryImpl
    ): com.platform.app.domain.repository.BudgetRepository
}
