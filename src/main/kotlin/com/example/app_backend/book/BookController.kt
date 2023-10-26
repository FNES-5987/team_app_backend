package com.example.app_backend.book

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
// Service 주입
class BookController(private val bookService: BookService) {
    @GetMapping("/cache")
    fun getCacheBooks(): List<SimplifiedBookDTO> {
        val cachedBooks = bookService.getCacheBooks()
//        println("cacheBooks 응답 성공")
        return cachedBooks
    }
    // 추가
    @PostMapping("/add")
    fun addBook(@RequestBody book: SimplifiedBookDTO): ResponseEntity<List<SimplifiedBookDTO>> {

        // DB 업데이트 기능 포함
        val updatedBooks = bookService.addBook(book)
        println("addBooks 응답 성공")
        println("${updatedBooks}")
        return ResponseEntity.ok(updatedBooks)
    }

    @DeleteMapping("/{bookId}")
    fun deleteBook(@PathVariable bookId: Int): ResponseEntity<List<SimplifiedBookDTO>> {
        // DB 업데이트 기능 포함
        val updatedBooks = bookService.deleteBook(bookId)
        return ResponseEntity.ok(updatedBooks)
    }

}