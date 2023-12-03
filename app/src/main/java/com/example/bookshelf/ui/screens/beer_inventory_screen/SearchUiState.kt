package com.example.bookshelf.ui.screens.beer_inventory_screen

data class SearchUiState(
    val query: String = "stout", // Notes: just because I am a lazy typer
    val searchStarted: Boolean = false
)
