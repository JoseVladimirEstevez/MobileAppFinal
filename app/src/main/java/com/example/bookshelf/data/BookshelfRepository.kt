package com.example.bookshelf.data

import com.example.bookshelf.model.Book

/**
 * Interface for book data retrieval.
 */
interface BookshelfRepository {
    /** Retrieves books based on query */
    suspend fun getBooks(query: String): List<Book>?
    
    /** Retrieves specific book by id */
    suspend fun getBook(id: String): Book?  // Changed from Int to String
}