package com.example.app_backend.book

import org.springframework.http.HttpStatus
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
        val books = bookService.addBook(book)
        return if (books.isNotEmpty()) {
            println("AddBooks: 도서 추가됨")
            ResponseEntity.ok(books)

        } else {
            println("AddBooks: 기존의 도서로 추가 안됨.")
            ResponseEntity.status(HttpStatus.CONFLICT).body(emptyList())
        }
    }

    @DeleteMapping
    fun deleteBooks(@RequestParam("itemIds") itemIds: List<Int>): ResponseEntity<Map<String, Any>> {
        // DB 업데이트 기능 포함
        println("서버 삭제 응답:${itemIds}")
        return try {
            val deletedBookIds = bookService.deleteBooks(itemIds)
            val response = mapOf(
                "deletedBooks" to deletedBookIds,  // "deletedBookIds"를 "deletedBooks"로 변경
                "message" to "총 ${deletedBookIds.size}개의 도서정보가 성공적으로 삭제 되었습니다."
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to (e.message ?: "Unknown error")))
        }
    }

    @PutMapping("/{itemId}")
    fun updateBook(
        @PathVariable itemId: Int,
        @RequestBody updatedData: SimplifiedBookDTO
    ): ResponseEntity<SimplifiedBookDTO> {
        return try {
            val updatedBook = bookService.modifyBook(itemId, updatedData)
            ResponseEntity.ok(updatedBook)
        } catch (e: Exception) {
//           500 Internal Server Error+응답 본문을 null 반환
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }


}