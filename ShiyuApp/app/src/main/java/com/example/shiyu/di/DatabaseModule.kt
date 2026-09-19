package com.example.shiyu.di

import android.content.Context
import androidx.room.Room
import com.example.shiyu.data.db.AppDatabase
import com.example.shiyu.data.db.dao.*
import com.example.shiyu.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DB_NAME
        ).addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8
        ).build()
    }

    @Provides
    fun provideCoupleConfigDao(database: AppDatabase): CoupleConfigDao {
        return database.coupleConfigDao()
    }

    @Provides
    fun provideRecipeDao(database: AppDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    fun provideOrderDao(database: AppDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    fun provideGalleryImageDao(database: AppDatabase): GalleryImageDao {
        return database.galleryImageDao()
    }

    @Provides
    fun provideInventoryDao(database: AppDatabase): InventoryDao {
        return database.inventoryDao()
    }

    @Provides
    fun provideSupplierDao(database: AppDatabase): SupplierDao {
        return database.supplierDao()
    }

    @Provides
    fun provideOperationLogDao(database: AppDatabase): OperationLogDao {
        return database.operationLogDao()
    }

    @Provides
    fun provideRecipeVersionDao(database: AppDatabase): RecipeVersionDao {
        return database.recipeVersionDao()
    }

    @Provides
    fun provideRecipeReviewDao(database: AppDatabase): RecipeReviewDao {
        return database.recipeReviewDao()
    }
}
