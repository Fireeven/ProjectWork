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
        Category::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun groceryItemDao(): GroceryItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create categories table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                """)

                // Insert default categories
                db.execSQL("""
                    INSERT INTO categories (name) VALUES 
                    ('Grocery Store'),
                    ('Supermarket'),
                    ('Convenience Store'),
                    ('Pharmacy'),
                    ('Hardware Store'),
                    ('Other')
                """)

                // Create temporary places table with new schema
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS places_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        address TEXT,
                        categoryId INTEGER,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL
                    )
                """)

                // Copy data from old places table to new one
                db.execSQL("""
                    INSERT INTO places_new (id, name, address)
                    SELECT id, name, address FROM places
                """)

                // Drop old table and rename new one
                db.execSQL("DROP TABLE places")
                db.execSQL("ALTER TABLE places_new RENAME TO places")

                // Create index on categoryId
                db.execSQL("CREATE INDEX index_places_categoryId ON places(categoryId)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create index on placeId in grocery_items table
                db.execSQL("CREATE INDEX IF NOT EXISTS index_grocery_items_placeId ON grocery_items(placeId)")
            }
        }
        
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add price, recipeId, and recipeTitle columns to grocery_items table
                db.execSQL("ALTER TABLE grocery_items ADD COLUMN price REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE grocery_items ADD COLUMN recipeId TEXT")
                db.execSQL("ALTER TABLE grocery_items ADD COLUMN recipeTitle TEXT")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add purchase tracking columns to grocery_items table
                db.execSQL("ALTER TABLE grocery_items ADD COLUMN isPurchased INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE grocery_items ADD COLUMN purchaseDate INTEGER")
                db.execSQL("ALTER TABLE grocery_items ADD COLUMN actualPrice REAL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 