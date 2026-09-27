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
import com.platform.app.data.local.dao.ItemDao
import com.platform.app.data.remote.PlatformApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideItemDao(db: PlatformDatabase): ItemDao {
        return db.itemDao
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: PlatformDatabase): com.platform.app.data.local.dao.CategoryDao {
        return db.categoryDao
    }

    @Provides
    @Singleton
    fun provideSubcategoryDao(db: PlatformDatabase): com.platform.app.data.local.dao.SubcategoryDao {
        return db.subcategoryDao
    }

    @Provides
    @Singleton
    fun provideContactDao(db: PlatformDatabase): com.platform.app.data.local.dao.ContactDao {
        return db.contactDao
    }

    @Provides
    @Singleton
    fun provideFinancialAccountDao(db: PlatformDatabase): com.platform.app.data.local.dao.FinancialAccountDao {
        return db.financialAccountDao
    }

    @Provides
    @Singleton
    fun providePaymentMethodDao(db: PlatformDatabase): com.platform.app.data.local.dao.PaymentMethodDao {
        return db.paymentMethodDao
    }

    @Provides
    @Singleton
    fun provideBillDao(db: PlatformDatabase): com.platform.app.data.local.dao.BillDao {
        return db.billDao
    }

    @Provides
    @Singleton
    fun provideBillInstallmentDao(db: PlatformDatabase): com.platform.app.data.local.dao.BillInstallmentDao {
        return db.billInstallmentDao
    }

    @Provides
    @Singleton
    fun provideGoalDao(db: PlatformDatabase): com.platform.app.data.local.dao.GoalDao {
        return db.goalDao
    }

    @Provides
    @Singleton
    fun provideBudgetDao(db: PlatformDatabase): com.platform.app.data.local.dao.BudgetDao {
        return db.budgetDao
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

    @Provides
    @Singleton
    fun providePlatformApiService(okHttpClient: OkHttpClient): PlatformApiService {
        return Retrofit.Builder()
            .baseUrl(PlatformApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlatformApiService::class.java)
    }
}
