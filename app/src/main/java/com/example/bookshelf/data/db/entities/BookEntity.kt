package com.example.bookshelf.data.db.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "price")
    val price: Float,
    @ColumnInfo(name = "volume_value")
    val volumeValue: Float,
    @ColumnInfo(name = "volume_unit")
    val volumeUnit: String,
    @ColumnInfo(name = "quantity", defaultValue = "1")
    val quantity: Int = 1
)

