package com.example.bookshelf.model

import kotlinx.serialization.Serializable
import com.example.bookshelf.network.BeerResponse
import com.example.bookshelf.network.toBook

@Serializable
data class QueryResponse(
    val items: List<Book>?,
    val totalItems: Int,
    val kind: String,
)

/**
 * Extension function to convert a list of BeerResponse to QueryResponse
 */
fun List<BeerResponse>.toQueryResponse(): QueryResponse {
    return QueryResponse(
        items = this.map { it.toBook() },
        totalItems = this.size,
        kind = "beer#volumeList"
    )
}
