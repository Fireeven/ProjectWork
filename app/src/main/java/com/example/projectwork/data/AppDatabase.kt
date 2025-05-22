package com.example.projectwork.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlaceEntity::class,
        GroceryItem::class,
        Category::class,
        User::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun groceryItemDao(): GroceryItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add any necessary migration code here
                // For now, we'll just recreate the tables
                database.execSQL("DROP TABLE IF EXISTS grocery_items")
                database.execSQL("DROP TABLE IF EXISTS places")
                
                // Recreate places table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS places (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        address TEXT NOT NULL,
                        category TEXT NOT NULL
                    )
                """)
                
                // Recreate grocery_items table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS grocery_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        placeId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        isChecked INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        FOREIGN KEY(placeId) REFERENCES places(id) ON DELETE CASCADE
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 