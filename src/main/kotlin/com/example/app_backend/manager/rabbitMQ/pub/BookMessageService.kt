package com.example.app_backend.manager.rabbitMQ.pub

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class BookMessageService(private val jdbcTemplate: JdbcTemplate) {

    fun saveBook(bookMessageRequest: BookMessageRequest) {
        val sql = """
            INSERT INTO new_book (id, publisher, title, author, pubDate, isbn, categoryName, priceStandard, quantity, imageUuidName)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """

        jdbcTemplate.update(
            sql,
            bookMessageRequest.id,
            bookMessageRequest.publisher,
            bookMessageRequest.title,
            bookMessageRequest.author,
            bookMessageRequest.pubDate,
            bookMessageRequest.isbn,
            bookMessageRequest.categoryName,
            bookMessageRequest.priceStandard,
            bookMessageRequest.quantity,
            bookMessageRequest.imageUuidName
        )
    }
}
