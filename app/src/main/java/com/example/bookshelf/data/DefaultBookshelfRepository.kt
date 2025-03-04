package com.example.bookshelf.data

import android.util.Log
import com.example.bookshelf.model.Book
import com.example.bookshelf.network.BookshelfApiService
import com.example.bookshelf.network.BeerResponse
import com.example.bookshelf.network.toBook

/**
 * Default Implementation of repository that retrieves volumes data from underlying data source.
 */
class DefaultBookshelfRepository(
    private val bookshelfApiService: BookshelfApiService
) : BookshelfRepository {
    /** Retrieves list of Volumes from underlying data source */
    override suspend fun getBooks(query: String): List<Book>? {
        val formattedQuery = query.trim().replace(" ", "%20")
        Log.d("API_REQUEST", "Sending query: '$formattedQuery'")
        
        return try {
            val res = bookshelfApiService.getBooks(formattedQuery)
            Log.d("API_RESPONSE", "API response code: ${res.code()}")
            Log.d("API_RESPONSE", "API response body: ${res.body()}")
            
            if (res.isSuccessful) {
                // Use the data field from the response
                val beerList = res.body()?.data ?: emptyList()
                Log.d("API_BEERS", "Found ${beerList.size} beers")
                
                beerList.map { 
                    Log.d("API_BEER_ITEM", "Converting beer: ${it.name}")
                    it.toBook() 
                }
            } else {
                val errorBody = res.errorBody()?.string()
                Log.e("API_ERROR", "Error: ${res.code()} - ${res.message()}")
                Log.e("API_ERROR", "Error body: $errorBody")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Exception during API call", e)
            e.printStackTrace()
            null
        }
    }

    override suspend fun getBook(id: String): Book? {
        return try {
            val res = bookshelfApiService.getBook(id)
            if (res.isSuccessful) {
                // Use the data field from the response
                val beerList = res.body()?.data ?: emptyList()
                beerList.firstOrNull()?.toBook()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}