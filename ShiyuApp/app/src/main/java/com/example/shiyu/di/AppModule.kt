package com.example.shiyu.di

import android.content.Context
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.sync.AutoSyncManager
import com.example.shiyu.util.RoleManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRoleManager(@ApplicationContext context: Context): RoleManager {
        return RoleManager(context)
    }

    @Provides
    @Singleton
    fun provideAutoSyncManager(
        @ApplicationContext context: Context,
        backendRepository: BackendRepository
    ): AutoSyncManager {
        return AutoSyncManager(context, backendRepository)
    }
}
