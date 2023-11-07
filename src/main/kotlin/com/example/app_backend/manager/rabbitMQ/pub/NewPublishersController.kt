package com.example.app_backend.manager.rabbitMQ.pub

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NewPublishersController(private val jdbcTemplate: JdbcTemplate) {

    @GetMapping("/api/new-publishers")
    fun getNewPublishers(): Set<String> {
        val sql = "SELECT DISTINCT publisher FROM new_book"

        return jdbcTemplate.queryForList(sql, String::class.java).toSet()
    }
}
