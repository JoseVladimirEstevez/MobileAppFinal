package com.example.bookshelf.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.bookshelf.data.db.dao.BookDao
import com.example.bookshelf.data.db.dao.OrderDao
import com.example.bookshelf.data.db.entities.BookEntity
import com.example.bookshelf.data.db.entities.OrderEntity

@Database(entities = [BookEntity::class, OrderEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun orderDao(): OrderDao
    
    companion object {
        // Migration from version 1 to 2
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add quantity column with default value of 1
                database.execSQL("ALTER TABLE books ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}