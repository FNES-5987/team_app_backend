package com.example.app_backend.publisher

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

@RestController
@RequestMapping("/api/publishers")
class PublisherController(private val jdbcTemplate: JdbcTemplate) {

    @GetMapping
    fun getPublishers(): List<Publisher> {
        val sql = "SELECT publisher, COUNT(*) as count FROM book GROUP BY publisher"

        return jdbcTemplate.query(sql, RowMapper { resultSet: ResultSet, _: Int ->
            Publisher(resultSet.getString("publisher"), resultSet.getInt("count"))
        })
    }
}
