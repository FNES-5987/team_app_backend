// PublisherController.kt

package com.example.app_backend.manager.publisher

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/publishers")
class PublisherController(private val jdbcTemplate: JdbcTemplate) {

    @GetMapping
    fun getPublishers(): List<Publisher> {
        val sql = "SELECT publisher, COUNT(*) as count FROM book GROUP BY publisher"

        return jdbcTemplate.query(sql, RowMapper { resultSet, _ ->
            Publisher(resultSet.getString("publisher"), resultSet.getInt("count"))
        })
    }

    @GetMapping("/{publisherName}")
    fun getBooksByPublisher(@PathVariable publisherName: String): List<Book> {
        val sql = "SELECT itemId, title, ISBN, stockStatus FROM inventory WHERE publisher = ?"

        return jdbcTemplate.query(sql, arrayOf(publisherName)) { resultSet, _ ->
            Book(
                    resultSet.getString("itemId"),
                    resultSet.getString("title"),
                    resultSet.getString("ISBN"),
                    resultSet.getString("stockStatus"),
            )
        }
    }
}
