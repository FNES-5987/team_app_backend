package com.example.app_backend.book

import com.example.app_backend.api.SimplifiedBooks
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.json.JsonParseException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class BookService(
    // 중간 저장소
    // String 타입의 키와 값으로 데이터를 저장하거나 조회
    private val redisTemplate: RedisTemplate<String, String>
) {
    // Java 객체와 JSON 문자열 간의 변환
    private val mapper = jacksonObjectMapper()

    fun getCacheBooks(): List<SimplifiedBookDTO> {
//        println("cache 요청 들어 옴")
        // 성능 측정 시작 시간
        val start = System.currentTimeMillis()
        // Redis 캐시에서 "books"라는 키로 저장된 데이터를 가져옴.
        val cacheData = redisTemplate.opsForValue().get("books")
        return if (!cacheData.isNullOrEmpty()) {
            println("Data from Redis")
//            JSON 문자열을 List<SimplifiedBookDTO> 타입으로 변환하여 반환
//            println("Cacheed data: $cacheedData")
            try {
                mapper.readValue(cacheData)

            } catch (e: JsonParseException) {
                println("Json parsing error: ${e.message}")
                emptyList()
            }

        } else {
            println("Data from MySQL")
            // DB 가져옴 (호출)
            val books = getBooks()
            //가져온 리스트를 JSON 문자열로 변환하여 "books"라는 키로 Redis 캐시에 저장
//            redisTemplate.opsForValue().set("books", mapper.writeValueAsString(books))
//            val jsonString = mapper.writeValueAsString(books)
//            println("JSON string: $jsonString")
            redisTemplate.opsForValue().set("books", mapper.writeValueAsString(books))

            val end = System.currentTimeMillis()
            println("Time taken: ${end - start} ms")
//            가져온 책의 리스트를 반환
            return books
        }

    }

    // 최신 DB정보 cache에 업데이트
    fun updateCache(newBook: SimplifiedBookDTO? = null, deletedItemId: Int? = null): List<SimplifiedBookDTO> {
        println("updateCache 요청 들어 옴")
        // db 가져옴
        val existingBooks = getBooks()
        
        if (deletedItemId != null) {
            println("삭제 요청 들어 옴")
            // 일치하는 것 제외하고 새로운 데이터로 set함.
            val updatedBooks = existingBooks.filterNot { it.itemId == deletedItemId }
            redisTemplate.opsForValue().set("books", mapper.writeValueAsString(updatedBooks))
            return updatedBooks
        }

        if (newBook != null) {
            println("추가 요청 들어 옴")
            // 새로운 책이 있다면, 기존 정보에 새로운 책 정보를 추가
            val isNewBook = existingBooks.none { it.itemId == newBook.itemId  }
            if (isNewBook) {
                val updatedBooks = existingBooks + newBook
                redisTemplate.opsForValue().set("books", mapper.writeValueAsString(updatedBooks))
                return updatedBooks
            }
            println("추가요청 redis 반영됨")
        }

        // 아무런 변경이 없으면 기존 정보 반환
        return existingBooks
    }

    // DB
    fun getBooks(): List<SimplifiedBookDTO> {
        println("getBooks() called")//함수 호출
        return transaction {
            SimplifiedBooks.selectAll().map { row ->
                SimplifiedBookDTO(
                    id = row[SimplifiedBooks.id].value,
                    createdDate = row[SimplifiedBooks.createdDate],
                    publisher = row[SimplifiedBooks.publisher],
                    title = row[SimplifiedBooks.title],
                    link = row[SimplifiedBooks.link],
                    author = row[SimplifiedBooks.author],
                    pubDate = row[SimplifiedBooks.pubDate],
                    description = row[SimplifiedBooks.description],
                    isbn = row[SimplifiedBooks.isbn],
                    isbn13 = row[SimplifiedBooks.isbn13],
                    itemId = row[SimplifiedBooks.itemId],
                    priceSales = row[SimplifiedBooks.priceSales],
                    priceStandard = row[SimplifiedBooks.priceStandard],
                    stockStatus = row[SimplifiedBooks.stockStatus],
                    cover = row[SimplifiedBooks.cover],
                    categoryId = row[SimplifiedBooks.categoryId],
                    categoryName = row[SimplifiedBooks.categoryName],
                    customerReviewRank = row[SimplifiedBooks.customerReviewRank]
                )
            }
        }
    }


    fun addBook(book: SimplifiedBookDTO): List<SimplifiedBookDTO> {
        println("addBook 요청 들어옴")
        val existingBooks = getBooks()
        val isBook = existingBooks.filter { it.itemId == book.itemId }
        if (isBook.isEmpty()) {
            val currentTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")
            val formattedDate = currentTime.format(formatter)
            // DB에 책을 추가
            val addBook = transaction {
                SimplifiedBooks.insertAndGetId {
                    it[createdDate] = formattedDate
                    it[title] = book.title
                    it[link] = book.link
                    it[author] = book.author
                    it[pubDate] = book.pubDate
                    it[description] = book.description
                    it[isbn] = book.isbn
                    it[isbn13] = book.isbn13
                    it[itemId] = book.itemId
                    it[priceSales] = book.priceSales
                    it[priceStandard] = book.priceStandard
                    it[stockStatus] = book.stockStatus
                    it[cover] = book.cover
                    it[categoryId] = book.categoryId
                    it[categoryName] = book.categoryName
                    it[publisher] = book.publisher
                    it[customerReviewRank] = book.customerReviewRank
                }
            }
            return updateCache(book)
        }
        return emptyList()
    }

    fun deleteBook(itemId: Int): List<SimplifiedBookDTO> {
        // DB에서 책을 삭제
        transaction {
            SimplifiedBooks.deleteWhere { SimplifiedBooks.id eq itemId }
        }

        // 캐시 업데이트
        return updateCache(deletedItemId = itemId)
    }
}