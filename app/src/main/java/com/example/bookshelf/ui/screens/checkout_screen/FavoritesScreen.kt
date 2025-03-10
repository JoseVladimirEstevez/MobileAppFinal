package com.example.bookshelf.ui.screens.checkout_screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.SemanticsProperties.Selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bookshelf.data.db.entities.BookEntity
import com.example.bookshelf.data.db.entities.OrderEntity
import com.example.bookshelf.ui.screens.beer_inventory_screen.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: QueryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState(initial = QueryUiState.Loading)
    val (email, setEmail) = remember { mutableStateOf("") }
    val minDaysFromNow = 3
    var selectedDate by remember { mutableStateOf(LocalDate.now())
}
    val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = LocalDate.now().atStartOfDay(ZoneId.of("America/New_York")).toInstant().toEpochMilli()
)
    var isDatePickerDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.getBeers()
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        val dateMillis = datePickerState.selectedDateMillis ?: return@LaunchedEffect

        selectedDate = LocalDate.ofEpochDay(dateMillis / (24 * 60 * 60 * 1000))

        if (selectedDate == LocalDate.now()){
            selectedDate = LocalDate.now().plusDays(1 + minDaysFromNow.toLong())
        }

    }

    // Get the current books state - this will recompose when books change
    val books by viewModel.books.collectAsState()
    
    // Calculate the order total directly from the books list
    // This will recalculate every time the books list changes
    val orderTotal = books.sumOf { it.price.toDouble() * it.quantity }
    
    val isButtonEnabled = verifyEmail(email) && selectedDate.isAfter(LocalDate.now()) && books.isNotEmpty()

    Column {
        if (isDatePickerDialogOpen) {
            Dialog(onDismissRequest = { isDatePickerDialogOpen = false }) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        DatePicker(
                            datePickerState = datePickerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            dateFormatter = remember { DatePickerFormatter() },
                            dateValidator = { selectedDate -> 
                                // Your existing validation code
                                val minDate = LocalDate.now().plusDays(minDaysFromNow.toLong())
                                val minDateMillis: Long =
                                    minDate.atStartOfDay(ZoneId.of("America/New_York")).toInstant()
                                        .toEpochMilli()
                                selectedDate >= minDateMillis
                            },
                            title = { 
                                Text(
                                    text = "Select a Pick-Up Date",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) 
                            },
                            headline = { }, // Your custom headline composable
                            colors = DatePickerDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                dayContentColor = MaterialTheme.colorScheme.onSurface,
                                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                                selectedDayContainerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Button(
                            onClick = { isDatePickerDialogOpen = false },
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Text("Confirm Date")
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Ready to Checkout?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 20.dp)

            )
            EmailInput(email, setEmail)

            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pick up date: $selectedDate",
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(1f)
                )
                Button(
                    onClick = { isDatePickerDialogOpen = !isDatePickerDialogOpen },
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(5.dp),
                ) {
                    Text("Select a date")
                }
            }

            OrderTotal(orderTotal)

            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val selectedBooks = buildString {
                            books.forEachIndexed { index, book ->
                                append(book.id)
                                if (index < books.size - 1) {
                                    append(", ")
                                }
                            }
                        }

                        val newOrder = OrderEntity(
                            customerEmail = email,
                            pickupDate = selectedDate.toEpochDay(),
                            beerIds = selectedBooks,
                            totalPrice = orderTotal,
                            orderStatus = "Pending",
                        )

                        viewModel.submitOrder(newOrder)

                        setEmail("")
                        selectedDate = LocalDate.now().plusDays(1 + minDaysFromNow.toLong())
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(5.dp),
                    enabled = isButtonEnabled
                ) {
                    Text("Submit Order!")
                }
            }
            Text(
                text = "Order Summary",
                modifier = Modifier.padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            )
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)) {
                items(books.size) { index ->
                    FavoritesCard(books[index], viewModel)
                }
            }
        }

        
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailInput(email: String, setEmail: (String) -> Unit) {

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Enter email: ",
            modifier = Modifier
                .weight(1f)

        )
        OutlinedTextField(
            value = email,
            onValueChange = { newEmail -> setEmail(newEmail) },
            placeholder = { Text("youremail@gmail.com") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                capitalization = KeyboardCapitalization.None
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "emailIcon"
                )
            },
            singleLine = true,
            modifier = Modifier
                .weight(2f)
                .height(60.dp)
        )
    }
}

@Composable
fun OrderTotal(total: Double) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
    ) {
        Text(
            text = "Total Amount: ",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .weight(2f)
        )

        Text(
            text = "$total $",
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
fun FavoritesCard(item: BookEntity, viewModel: QueryViewModel) {
    var buttonClicked by remember { mutableStateOf(false) }
    val quantity = item.quantity
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp),
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start-aligned column
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 10.dp, top = 10.dp)
                    .weight(2f),
            ) {
                // Name label
                Text(
                    text = item.name,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "ID: " + item.id,
                )
                
                // Add quantity selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Quantity: ")
                    
                    Button(
                        onClick = { 
                            if (quantity > 1) {
                                viewModel.updateBookQuantity(item.id, quantity - 1)
                            }
                        },
                        modifier = Modifier.height(30.dp).padding(horizontal = 2.dp),
                        shape = RoundedCornerShape(5.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text("-")
                    }
                    
                    Text(
                        text = quantity.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Button(
                        onClick = { 
                            viewModel.updateBookQuantity(item.id, quantity + 1)
                        },
                        modifier = Modifier.height(30.dp).padding(horizontal = 2.dp),
                        shape = RoundedCornerShape(5.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text("+")
                    }
                }
            }

            // End-aligned column
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp, bottom = 10.dp, top = 10.dp, start = 10.dp)
            ) {
                Text(
                    text = "${item.price * item.quantity} $", // Multiply by quantity
                    modifier = Modifier.padding(bottom = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )

                Button(
                    onClick = { buttonClicked = true },
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(5.dp),
                    contentPadding = PaddingValues(5.dp),
                ) {
                    Text(
                        text = "Remove",
                    )
                }
            }
        }
    }

    LaunchedEffect(buttonClicked) {
        if (buttonClicked) {    
            viewModel.removeFromCart(item)
            buttonClicked = false
        }
    }
}

fun verifyEmail(email: String): Boolean{
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
    return emailRegex.matches(email)
}