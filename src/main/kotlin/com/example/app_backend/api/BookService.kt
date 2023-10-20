package com.example.app_backend.api

import com.example.app_backend.book.Book
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class BookService (){

    fun bookList() {
        transaction {
            val result = BookByPublisher
                .slice(BookByPublisher.id,BookByPublisher.createdDate, BookByPublisher.publisher, BookByPublisher.title, BookByPublisher.author, BookByPublisher.isbn, BookByPublisher.isbn13, BookByPublisher.priceSales, BookByPublisher.priceStandard,BookByPublisher.categoryName,BookByPublisher.adult)
                .selectAll()
                .toList()

            for (row in result) {
                val id = row[BookByPublisher.id]
                val createdDate = row[BookByPublisher.createdDate]
                val publisher = row[BookByPublisher.publisher]
                val title = row[BookByPublisher.title]
                val author = row[BookByPublisher.author]
                val isbn = row[BookByPublisher.isbn]
                val isbn13 = row[BookByPublisher.isbn13]
                val priceSales = row[BookByPublisher.priceSales]
                println("Created Date: $createdDate, Publisher: $publisher,Title: $title")
                println("Created Date: $createdDate, Publisher: $publisher,Title: $title, Author: $author,isbn: $isbn,isbn13: $isbn13,priceSales: $priceSales")
            }
        }
    }

}