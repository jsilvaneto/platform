package com.platform.app.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.platform.app.core.connectivity.ConnectivityNetworkMonitor
import com.platform.app.core.connectivity.NetworkMonitor
import com.platform.app.core.dispatcher.DefaultDispatcherProvider
import com.platform.app.core.dispatcher.DispatcherProvider
import com.platform.app.core.preferences.PreferencesManager
import com.platform.app.core.preferences.PreferencesManagerImpl
import com.platform.app.data.local.PlatformDatabase
import com.platform.app.data.local.dao.BillDao
import com.platform.app.data.local.dao.BillInstallmentDao
import com.platform.app.data.local.dao.BudgetDao
import com.platform.app.data.local.dao.CategoryDao
import com.platform.app.data.local.dao.ContactDao
import com.platform.app.data.local.dao.CreditCardDao
import com.platform.app.data.local.dao.ExpenseItemDao
import com.platform.app.data.local.dao.FinancialAccountDao
import com.platform.app.data.local.dao.GoalDao
import com.platform.app.data.local.dao.PaymentMethodDao
import com.platform.app.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return ConnectivityNetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManagerImpl(context)
    }

    @Provides
    @Singleton
    fun providePlatformDatabase(app: Application): PlatformDatabase {
        return Room.databaseBuilder(
            app,
            PlatformDatabase::class.java,
            PlatformDatabase.DATABASE_NAME
        )
        .addMigrations(
            PlatformDatabase.MIGRATION_4_5,
            PlatformDatabase.MIGRATION_5_6,
            PlatformDatabase.MIGRATION_6_7,
            PlatformDatabase.MIGRATION_7_8,
            PlatformDatabase.MIGRATION_8_9
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: PlatformDatabase): CategoryDao {
        return db.categoryDao
    }

    @Provides
    @Singleton
    fun provideExpenseItemDao(db: PlatformDatabase): ExpenseItemDao {
        return db.expenseItemDao
    }

    @Provides
    @Singleton
    fun provideCreditCardDao(db: PlatformDatabase): CreditCardDao {
        return db.creditCardDao
    }

    @Provides
    @Singleton
    fun provideContactDao(db: PlatformDatabase): ContactDao {
        return db.contactDao
    }

    @Provides
    @Singleton
    fun provideFinancialAccountDao(db: PlatformDatabase): FinancialAccountDao {
        return db.financialAccountDao
    }

    @Provides
    @Singleton
    fun providePaymentMethodDao(db: PlatformDatabase): PaymentMethodDao {
        return db.paymentMethodDao
    }

    @Provides
    @Singleton
    fun provideBillDao(db: PlatformDatabase): BillDao {
        return db.billDao
    }

    @Provides
    @Singleton
    fun provideBillInstallmentDao(db: PlatformDatabase): BillInstallmentDao {
        return db.billInstallmentDao
    }

    @Provides
    @Singleton
    fun provideGoalDao(db: PlatformDatabase): GoalDao {
        return db.goalDao
    }

    @Provides
    @Singleton
    fun provideBudgetDao(db: PlatformDatabase): BudgetDao {
        return db.budgetDao
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: PlatformDatabase): TransactionDao {
        return db.transactionDao
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
