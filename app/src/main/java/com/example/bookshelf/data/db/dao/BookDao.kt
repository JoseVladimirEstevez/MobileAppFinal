package com.example.bookshelf.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bookshelf.data.db.entities.BookEntity

@Dao
interface BookDao {
    // Replace suspend functions with non-suspend versions when having issues with code generation
    @Query("SELECT * FROM books")
    fun getAll(): List<BookEntity>
    
    @Query("SELECT * FROM books WHERE id = :id")
    fun getById(id: String): BookEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(book: BookEntity)
    
    @Update
    fun update(book: BookEntity)
    
    @Delete
    fun delete(book: BookEntity)
    
    @Query("DELETE FROM books")
    fun clearAll(): Int
}