package com.example.bookshelf.di

import android.content.Context
import androidx.room.Room
import com.example.bookshelf.data.BookshelfRepository
import com.example.bookshelf.data.DefaultBookshelfRepository
import com.example.bookshelf.data.db.AppDatabase
import com.example.bookshelf.BuildConfig
import com.example.bookshelf.data.db.dao.BookDao
import com.example.bookshelf.network.BookshelfApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class DefaultAppContainer(private val context: Context) : AppContainer {
//    private val json = Json {
//        ignoreUnknownKeys = true
//        explicitNulls = false
//    }

 override val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java, "AppDatabase"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2) // Add the migration
        .fallbackToDestructiveMigration() // Only if you're okay with losing data when migration fails
        .build()
    }

     override val bookDao: BookDao by lazy {
        appDatabase.bookDao()
    }

private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("X-RapidAPI-Key", BuildConfig.RAPID_API_KEY)
                .header("X-RapidAPI-Host", "beer9.p.rapidapi.com")
                .build()
            chain.proceed(request)
        }
        .build()

   override val bookshelfApiService: BookshelfApiService by lazy {
    Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BookshelfApiService.BASE_URL)
        .client(okHttpClient)  // Change this to use okHttpClient with the API key
        .build()
        .create()
}

    override val bookshelfRepository: BookshelfRepository by lazy {
        DefaultBookshelfRepository(bookshelfApiService)
    }
}