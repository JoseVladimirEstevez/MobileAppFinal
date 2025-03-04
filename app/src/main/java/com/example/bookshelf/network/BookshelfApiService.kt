package com.example.bookshelf.network

import com.example.bookshelf.BuildConfig
import com.example.bookshelf.model.Book
import com.example.bookshelf.model.Volume
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Headers

/**
 * A public interface that exposes the beer API methods
 */
interface BookshelfApiService {

    companion object {
        const val BASE_URL = "https://beer9.p.rapidapi.com/"
    }

    /**
     * Returns details about a specific beer
     */
    @Headers(
        "X-RapidAPI-Host: beer9.p.rapidapi.com",
        "X-RapidAPI-Key: ${BuildConfig.RAPID_API_KEY}"
    )
    @GET("beer/{id}")
    suspend fun getBook(@Path("id") id: String): Response<BeerSearchResponse>
    
    /**
     * Search for beers by name
     */
    @Headers(
        "X-RapidAPI-Host: beer9.p.rapidapi.com",
        "X-RapidAPI-Key: ${BuildConfig.RAPID_API_KEY}"
    )
    @GET("/")
    suspend fun getBooks(@Query("name") query: String): Response<BeerSearchResponse>
}

/**
 * Response wrapper for beer search API
 */
data class BeerSearchResponse(
    val code: Int,
    val error: Boolean,
    val data: List<BeerResponse>?
)

/**
 * Data structure for beer
 */
data class BeerResponse(
    val sku: String = "",
    val name: String = "",
    val brewery: String = "",
    val description: String = "",
    val region: String = "",
    val country: String = "",
    val abv: String = "0%",
    val ibu: String = "0",
    val category: String = "",
    val sub_category_1: String = "",
    val sub_category_2: String = "",
    val sub_category_3: String = ""
)

/**
 * Extension function to convert BeerResponse to Book
 */
fun BeerResponse.toBook(): Book {
    // Extract numeric values from strings
    val abvValue = abv.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
    val ibuValue = ibu.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f

    return Book(
        id = sku,
        name = name,
        image_url = "https://cerveceria-astilleros.com/cdn/shop/products/Keg-15.png",
        description = description.ifEmpty { "A craft beer by $brewery from $region, $country" },
        price = ibuValue,
        volume = Volume(
            value = abvValue,
            unit = "%"
        )
    )
}