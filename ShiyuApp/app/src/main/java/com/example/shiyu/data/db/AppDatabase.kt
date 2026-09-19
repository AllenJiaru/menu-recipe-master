package com.example.shiyu.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.shiyu.data.db.dao.*
import com.example.shiyu.data.db.entity.*
import com.example.shiyu.util.Constants

@Database(
    entities = [
        CoupleConfigEntity::class,
        RecipeEntity::class,
        RecipeMaterialEntity::class,
        RecipeStepEntity::class,
        OrderEntity::class,
        GalleryImageEntity::class,
        InventoryEntity::class,
        SupplierEntity::class,
        OperationLogEntity::class,
        RecipeVersionEntity::class,
        RecipeReviewEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coupleConfigDao(): CoupleConfigDao
    abstract fun recipeDao(): RecipeDao
    abstract fun orderDao(): OrderDao
    abstract fun galleryImageDao(): GalleryImageDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun operationLogDao(): OperationLogDao
    abstract fun recipeVersionDao(): RecipeVersionDao
    abstract fun recipeReviewDao(): RecipeReviewDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipe ADD COLUMN sync_id INTEGER")
                db.execSQL("ALTER TABLE order_record ADD COLUMN sync_id INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE order_record ADD COLUMN reject_reason TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE order_record ADD COLUMN user_id INTEGER")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipe ADD COLUMN status INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recipe nutrition/cost fields
                db.execSQL("ALTER TABLE recipe ADD COLUMN calories REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipe ADD COLUMN protein REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipe ADD COLUMN fat REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipe ADD COLUMN carbs REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipe ADD COLUMN fiber REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipe ADD COLUMN cost REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE recipe ADD COLUMN price REAL NOT NULL DEFAULT 0")
                // Inventory table
                db.execSQL("CREATE TABLE IF NOT EXISTS inventory (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL DEFAULT '', quantity REAL NOT NULL DEFAULT 0, unit TEXT NOT NULL DEFAULT '', threshold REAL NOT NULL DEFAULT 0, last_updated INTEGER NOT NULL DEFAULT 0)")
                // Supplier table
                db.execSQL("CREATE TABLE IF NOT EXISTS supplier (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, contact TEXT NOT NULL DEFAULT '', phone TEXT NOT NULL DEFAULT '', rating INTEGER NOT NULL DEFAULT 3, category TEXT NOT NULL DEFAULT '', address TEXT NOT NULL DEFAULT '', create_time INTEGER NOT NULL DEFAULT 0)")
                // Operation log table
                db.execSQL("CREATE TABLE IF NOT EXISTS operation_log (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, username TEXT NOT NULL, action TEXT NOT NULL, target TEXT NOT NULL DEFAULT '', target_id INTEGER NOT NULL DEFAULT 0, detail TEXT NOT NULL DEFAULT '', ip TEXT NOT NULL DEFAULT '', create_time INTEGER NOT NULL DEFAULT 0)")
                // Recipe version table
                db.execSQL("CREATE TABLE IF NOT EXISTS recipe_version (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, recipe_id INTEGER NOT NULL, version INTEGER NOT NULL DEFAULT 1, name TEXT NOT NULL, data TEXT NOT NULL DEFAULT '', create_time INTEGER NOT NULL DEFAULT 0)")
                // Recipe review table
                db.execSQL("CREATE TABLE IF NOT EXISTS recipe_review (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, recipe_id INTEGER NOT NULL, recipe_name TEXT NOT NULL, status INTEGER NOT NULL DEFAULT 0, reviewer TEXT NOT NULL DEFAULT '', comment TEXT NOT NULL DEFAULT '', create_time INTEGER NOT NULL DEFAULT 0, review_time INTEGER NOT NULL DEFAULT 0)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Gallery sync fields
                db.execSQL("ALTER TABLE gallery_img ADD COLUMN sync_id TEXT")
                db.execSQL("ALTER TABLE gallery_img ADD COLUMN sync_time INTEGER")
                db.execSQL("ALTER TABLE gallery_img ADD COLUMN image_url TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gallery_img ADD COLUMN backend_id INTEGER")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DB_NAME
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
