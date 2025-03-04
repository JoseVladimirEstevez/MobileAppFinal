package com.example.bookshelf.ui.screens.beer_inventory_screen

import android.media.CamcorderProfile.getAll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookshelf.BookshelfApplication
import com.example.bookshelf.data.BookshelfRepository

import com.example.bookshelf.data.db.dao.BookDao
import com.example.bookshelf.data.db.dao.OrderDao
import com.example.bookshelf.data.db.entities.BookEntity
import com.example.bookshelf.data.db.entities.OrderEntity
import com.example.bookshelf.model.Book
import com.example.bookshelf.model.Volume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

class QueryViewModel(
    private val bookshelfRepository: BookshelfRepository,
    var bookDao: BookDao,
    var orderDao: OrderDao
) : ViewModel() {
    private val _uiState = MutableStateFlow<QueryUiState>(QueryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    var selectedBookId by mutableStateOf("")

    private val _uiStateSearch = MutableStateFlow(SearchUiState())
    val uiStateSearch = _uiStateSearch.asStateFlow()


    // Notes: Question: I would like this to go to a separate viewModel since it belangs to a
    //  different screen. but at moment I am not sure how to get it donem because to
    //  select a favority book I would need to pass both view models
    // Logic for Favorite books -- Beg
    var favoriteBooks: MutableList<Book> by mutableStateOf(mutableListOf<Book>())
        private set


    var favoritesfUiState: QueryUiState by mutableStateOf(QueryUiState.Loading)
        private set


    private val _books = MutableStateFlow<List<BookEntity>>(emptyList())
    val books: StateFlow<List<BookEntity>> = _books.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()


    suspend fun isBookFavorite(bookId: String): Boolean {
        return withContext(Dispatchers.IO) {
            bookDao.getById(bookId) != null  // Change getBookById to getById
        }
    }

    fun addFavoriteBook(book: Book): Boolean {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val price = book.price // or your price calculation logic
                val bookEntity = BookEntity(
                    id = book.id,
                    name = book.name,
                    imageUrl = book.image_url,
                    description = book.description,
                    price = price,
                    volumeValue = book.volume.value,
                    volumeUnit = book.volume.unit,
                    quantity = 1 // Default quantity is 1
                )
                bookDao.insert(bookEntity)
                favoritesUpdated()
            }
        }
        return true
    }

    suspend fun removeFavoriteBook(book: Book): Boolean {
        return withContext(Dispatchers.IO) {
            val bookEntity = bookDao.getById(book.id)  // Change getBookById to getById
            if (bookEntity != null) {
                bookDao.delete(bookEntity)
                favoritesUpdated()
                true
            } else {
                false
            }
        }
    }

    fun getBeers() {
        viewModelScope.launch(Dispatchers.IO) {
            updateSearchStarted(true)
            try {
                _books.value = bookDao.getAll()
            } catch (e: IOException) {
                // Handle IOException
            } catch (e: HttpException) {
                // Handle HttpException
            } finally {
                withContext(Dispatchers.Unconfined) {
                    updateSearchStarted(false)
                }
            }
        }
    }

 private fun favoritesUpdated() {
    viewModelScope.launch(Dispatchers.IO) {
        favoritesfUiState = QueryUiState.Loading
        val favoriteBooksEntities = bookDao.getAll()
        val favoriteBooks = favoriteBooksEntities.map {
            Book(
                id = it.id, 
                name = it.name, 
                image_url = it.imageUrl,  // Use imageUrl from BookEntity
                description = it.description, 
                price = it.price, 
                volume = Volume(it.volumeValue, it.volumeUnit)  // Use volumeValue and volumeUnit
            )
        }
        withContext(Dispatchers.Main) {
            favoritesfUiState = QueryUiState.Success(favoriteBooks)
        }
    }
}

    fun updateQuery(query: String) {
        _uiStateSearch.update { currentState ->
            currentState.copy(
                query = query
            )
        }
    }

    fun updateSearchStarted(searchStarted: Boolean) {
        _uiStateSearch.update { currentState ->
            currentState.copy(
                searchStarted = searchStarted
            )
        }
    }

    // Only update this function if needed - most likely it won't need changes
    fun getBooks(query: String = "") {
        updateSearchStarted(true)
        viewModelScope.launch {
            _uiState.value = QueryUiState.Loading

            _uiState.value = try {
                val books = bookshelfRepository.getBooks(query)
                if (books == null) {
                    QueryUiState.Error
                } else if (books.isEmpty()) {
                    QueryUiState.Success(emptyList())
                } else {
                    QueryUiState.Success(books)
                }
            } catch (e: IOException) {
                QueryUiState.Error
            } catch (e: HttpException) {
                QueryUiState.Error
            }
        }
    }

    fun submitOrder(newOrder: OrderEntity) {
        viewModelScope.launch {
            insertOrder(newOrder)
        }
    }



    // Fix this function - calling suspend functions requires a coroutine context
    fun addBeer(book: BookEntity) {
        viewModelScope.launch {
            bookDao.insert(book)
            _books.value = bookDao.getAll()
        }
    }

    // Fix this function too
    fun getOrders() {
        viewModelScope.launch {
            _orders.value = orderDao.getAll()
        }
    }

    suspend fun insertOrder(order: OrderEntity) {
        withContext(Dispatchers.IO) {
            orderDao.insert(order)
            bookDao.clearAll()
            _books.value = emptyList()
            _orders.value = orderDao.getAll()
        }
    }
    suspend fun deleteBook(book: BookEntity) {
        withContext(Dispatchers.IO) {
            bookDao.delete(book)
            _books.value = bookDao.getAll()
        }
    }

 // Example of how to use the non-suspend DAO methods:
fun updateBookQuantity(id: String, quantity: Int) {
    viewModelScope.launch(Dispatchers.IO) {
        // Find the book by ID
        val book = bookDao.getById(id)
        
        // Update the book with the new quantity
        book?.let {
            val updatedBook = it.copy(quantity = quantity)
            bookDao.update(updatedBook)
            
            // Update the UI state with the new book list
            val updatedBooks = bookDao.getAll()
            withContext(Dispatchers.Main) {
                _books.value = updatedBooks
            }
        }
    }
}

    // Make sure this function is also properly calling suspend functions
    fun calculateOrderTotal(): Double {
        // Cannot directly access books.value here if it depends on suspend calls
        // Either make this a suspend function or use a local cache
        return books.value.sumOf { it.price.toDouble() * it.quantity }
    }

    // Similarly, ensure removeFromCart properly updates the UI state
    fun removeFromCart(item: BookEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                bookDao.delete(item)
                
                // Update the UI state with the new book list - IMPORTANT!
                _books.value = bookDao.getAll()
                
                // Log for debugging
                //Log.d("CART", "Item removed, books count: ${_books.value.size}")
            }
        }
    }

    // Notes: Question: At moment this is chuck of code is repeated in two files
    //  in QueryViewModel and in DetailsViewModel.
    //  what can I do/ place it so as not to have repeat code? I tried but I got a bunch of errors
    /**
     * Factory for BookshelfViewModel] that takes BookshelfRepository] as a dependency
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BookshelfApplication)
                val bookshelfRepository = application.container.bookshelfRepository
                val bookDao = application.container.appDatabase.bookDao()
                val orderDao = application.container.appDatabase.orderDao()
                QueryViewModel(bookshelfRepository = bookshelfRepository, bookDao = bookDao, orderDao = orderDao)
            }
        }
    }
}