package com.example.bookshelf.di

import android.content.Context
import com.example.bookshelf.BuildConfig // Add this import
import com.example.bookshelf.data.BookshelfRepository
import com.example.bookshelf.data.DefaultBookshelfRepository
import com.example.bookshelf.data.db.AppDatabase
import com.example.bookshelf.data.db.dao.BookDao
import com.example.bookshelf.network.BookshelfApiService
import kotlinx.serialization.json.Json // Add this import
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val bookshelfApiService: BookshelfApiService
    val bookshelfRepository: BookshelfRepository
    val appDatabase: AppDatabase
    val bookDao: BookDao
}

// In DefaultAppContainer or similar implementation class
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("X-RapidAPI-Key", BuildConfig.RAPID_API_KEY)
            .build()
        chain.proceed(request)
    }
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BookshelfApiService.BASE_URL)
    .client(okHttpClient)
    .build()