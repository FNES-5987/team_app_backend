package com.example.app_backend.manager.rabbitMQ.pub

//NewBookController.kt

import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/manager/newBooks")
class NewBookController(
    private val jdbcTemplate: JdbcTemplate
) {
    @GetMapping
    fun getBooks(): List<BookMessageRequest> {
        return jdbcTemplate.query(
            "SELECT * FROM new_book",
            BeanPropertyRowMapper(BookMessageRequest::class.java)
        )
    }

    @PostMapping
    fun addBook(@RequestBody newBook: BookMessageRequest?): BookMessageRequest? {
        return newBook
    }
}
