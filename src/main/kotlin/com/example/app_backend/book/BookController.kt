package com.example.app_backend.book

import com.example.app_backend.api.SimplifiedBooks
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books")
// Service 주입
class BookController(private val bookService: BookService) {
    @GetMapping
    fun getBooks(): List<SimplifiedBookDTO> {
        val books = bookService.getBooks()
        println("Books 응답 성공")  // 로그 출력
        return books
    }
}