package com.example.app_backend.admin.book

import com.example.app_backend.admin.alarm.AlarmService
import com.example.app_backend.admin.rabbit.BookDTO
import com.example.app_backend.admin.rabbit.HitDetails
import com.example.app_backend.admin.rabbit.HitsRecords
import com.example.app_backend.api.SimplifiedBooks
import com.example.app_backend.api.TodayBooks
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
// JSON 문자열을 코틀린으로
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
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
        private val redisTemplate: RedisTemplate<String, String>,
        private val alarmService: AlarmService,


        ) {
    // Java 객체와 JSON 문자열 간의 변환
    private val mapper = jacksonObjectMapper()
    // 개별 책 정보 저장
    private fun saveBookToCache(book: SimplifiedBookDTO) {
        try {
            val bookKey = "book:${book.itemId}"
            println("Redis에 책 저장 시도: 키 = $bookKey")
            redisTemplate.opsForValue().set(bookKey, mapper.writeValueAsString(book))
            println("Redis에 책 저장 성공: $book")
        } catch (e: Exception) {
            println("redis 업데이트 실패: ${e.message}")
        }
    }

    // 개별 책 정보 삭제
    private fun deleteBookFromCache(itemId: Int) {
        try {
            val bookKey = "book:$itemId"
            println("Redis에서 책 삭제 시도: 키 = $bookKey")
            redisTemplate.delete(bookKey)
            println("Redis에서 책 삭제 성공: 키 = $bookKey")

        } catch (e: Exception) {
            println("redis 삭제 업데이트 실패:: ${e.message}")
        }
    }

    //    마지막 데이터가 최근 추가된 데이터니까
    fun checkLast(cacheData: Any?) {
        when (cacheData) {
            // 매개변수가 string일때
            is String -> {
                if (cacheData != null) {
                    val books: List<SimplifiedBookDTO> = mapper.readValue(cacheData)
                    val lastBook = books.lastOrNull()
                    println("Last book: $lastBook")
                }
            }

            is List<*> -> {
                val lastBook = cacheData.lastOrNull()
//                println("Last updated book: $lastBook")
            }

            else -> {
                println("Unsupported type")
            }
        }
    }

    // redisTemplate 방식
//    fun getCacheBooks(): List<SimplifiedBookDTO> {
////        println("cache 요청 들어 옴")
//        // 성능 측정 시작 시간
//        val start = System.currentTimeMillis()
//        val bookKeys = redisTemplate.keys("book:*") ?: emptySet()
//        // Redis 캐시에서 "books"라는 키로 저장된 데이터를 가져옴.
//        val cacheData = redisTemplate.opsForValue().get("books")
//        println("2.조회: redis cache 업데이트 확인")
//        checkLast(cacheData)
//
//        val booksFromDB = getBooks()
//        return if (!cacheData.isNullOrEmpty()) {
//            val booksFromCache = try {
////               readValue: JSON 문자열을 코틀린의 리스트 객체로 변환
//                //typeOf<T>(cacheData)
//                mapper.readValue<List<SimplifiedBookDTO>>(cacheData)
//            } catch (e: JsonParseException) {
//                println("Json parsing error: ${e.message}")
//                emptyList<SimplifiedBookDTO>()
//            }
//            // 캐시 데이터와 DB 데이터 일치 여부 확인
//            if (booksFromCache == booksFromDB) {
//                println("Data from Redis")
//                val cacheData = redisTemplate.opsForValue().get("books")
//                println("3. redis cache 업데이트 확인")
//                checkLast(cacheData)
//                booksFromCache
//            } else {
//                println("Data from MySQL")
//                // 캐시 데이터와 DB 데이터 불일치 시, 캐시 업데이트
//                redisTemplate.opsForValue().set("books", mapper.writeValueAsString(booksFromDB))
//                val cacheData = redisTemplate.opsForValue().get("books")
//                println("DB와 일치 하지 않을떄")
//                checkLast(cacheData)
//                booksFromDB
//            }
//        } else {
//            println("Data from MySQL")
//            // 캐시에 데이터 없을 시, 캐시 업데이트
//            val cacheData = redisTemplate.opsForValue().set("books", mapper.writeValueAsString(booksFromDB))
//            checkLast(cacheData)
//            println("redis null일때:")
//            booksFromDB
//        }.also {
//            val end = System.currentTimeMillis()
//            println("Time taken: ${end - start} ms")
//        }
//    }
    // redis: 개별 도서에 key부여
    fun getCacheBooks(): List<SimplifiedBookDTO> {
        // 성능 측정 시작 시간
        val start = System.currentTimeMillis()

        // Redis에서 모든 책 키 검색 (예: "book:*")
        val bookKeys = redisTemplate.keys("book:*") ?: emptySet()

        // Redis에서 각 키에 해당하는 책 정보를 가져와 리스트로 변환
        val booksFromCache = bookKeys.mapNotNull { key ->
            redisTemplate.opsForValue().get(key)?.let { mapper.readValue(it, SimplifiedBookDTO::class.java) }
        }
        // 마지막으로 추가된 책 확인 (Redis)
        checkLast(booksFromCache)
        // DB에서 책 정보를 가져옴
        val booksFromDB = getBooks()
        // 마지막으로 추가된 책 확인 (MySQL)
        checkLast(booksFromDB)
        // 캐시 데이터와 DB 데이터 일치 여부 확인
        val isDataConsistent = booksFromCache.toSet() == booksFromDB.toSet()
        // 결과 출력 및 성능 측정
        return if (isDataConsistent) {
            // 데이터가 일치하면 Redis에서 가져온 데이터 반환
            println("Data from Redis")
            booksFromCache
        } else {
            // 데이터가 불일치하면 MySQL에서 가져온 데이터 반환
            println("Data from MySQL")
            booksFromDB
        }.also {
            val end = System.currentTimeMillis()
            println("Time taken: ${end - start} ms")
        }
    }

//    fun updateCache(newBook: SimplifiedBookDTO? = null, deletedItemIds: List<Int>? = null): List<SimplifiedBookDTO> {
//        println("updateCache 요청 들어 옴")
//        // Redis 캐시에서 "books"라는 키로 저장된 데이터를 가져옴.
//        val cacheData = redisTemplate.opsForValue().get("books")
//        val booksFromCache = if (cacheData != null) {
//            mapper.readValue<List<SimplifiedBookDTO>>(cacheData)
//        } else {
//            emptyList()
//        }
//
//        if (deletedItemIds != null) {
//            println("updateCache 삭제 요청 옴.")
//            // 일치하는 것 제외하고 새로운 데이터로 set함.
//            val updatedBooks = booksFromCache.filterNot { it.itemId in deletedItemIds }
//            redisTemplate.opsForValue().set("books", mapper.writeValueAsString(updatedBooks))
////            println("삭제 됨.Updated cache: ${redisTemplate.opsForValue().get("books")}")
//            return updatedBooks
//        }
//
//        if (newBook != null) {
//            val isNewBook = booksFromCache.none { it.itemId == newBook.itemId }
//            val isExistingBook = booksFromCache.any { it.itemId == newBook.itemId }
//
//            if (isNewBook) {
//                println("updateCache 추가 요청 옴")
//                // 새로운 책을 추가
//                val updatedBooks = booksFromCache + newBook
//                redisTemplate.opsForValue().set("books", mapper.writeValueAsString(updatedBooks))
////                println("Updated cache with new book: ${redisTemplate.opsForValue().get("books")}")
//                return updatedBooks
//            } else if (isExistingBook) {
//                println("updateCache 수정 요청 옴")
//                // 기존 책 정보를 삭제하고, 수정된 책 정보를 추가
//                val updatedBooks = booksFromCache.map {
//                    if (it.itemId == newBook.itemId) newBook else it
//                }
//                redisTemplate.opsForValue().set("books", mapper.writeValueAsString(updatedBooks))
////                println("Updated cache with updated book: ${redisTemplate.opsForValue().get("books")}")
//                return updatedBooks
//            }
//        }
//        // 아무런 변경이 없으면 기존 정보 반환
//        return getBooks()
//    }


    // DB
    fun getBooks(): List<SimplifiedBookDTO> {
        println("mysql: getBooks() called")
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

    val currentTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")
    val formattedDate = currentTime.format(formatter)

    //mysql DB
    fun addBook(book: SimplifiedBookDTO): List<SimplifiedBookDTO> {
        println("addBook 요청 들어옴")
        val existingBooks = getBooks()
        val isBook = existingBooks.filter { it.itemId == book.itemId }
        if (isBook.isEmpty()) {
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
            //데이터베이스에 책을 추가한 후, 반환된 ID(addBook)를 사용-> 자동생성기능도 업데이트
            // 새로운 SimplifiedBookDTO 객체를 생성
            saveBookToCache(book)
            return getBooks()
        }
        return emptyList()
    }

    fun getBookByItemId(itemId: Int): SimplifiedBookDTO? {
        return transaction {
            SimplifiedBooks.select { SimplifiedBooks.itemId eq itemId }
                .map { row ->
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
                .singleOrNull() // 여기서 singleOrNull()을 사용하여 해당하는 데이터가 없을 경우 null을 반환하도록 함
        }
    }


    // DB에서 책을 삭제
    fun deleteBooks(itemIdList: List<Int>): List<SimplifiedBookDTO> {
        println("deleteBooks 요청 들어옴")
        println("삭제 요청 리스트 데이터: $itemIdList")

//        val check = SimplifiedBooks.deleteWhere { SimplifiedBooks.itemId inList itemIdList }

        // DB에서 책과 관련된 hitdetails와 hits_record를 먼저 삭제
        transaction {
            // 먼저 삭제해야 할 book_id를 찾음
            val bookIdsToDelete = SimplifiedBooks.slice(SimplifiedBooks.id)
                .select { SimplifiedBooks.itemId inList itemIdList }
                .map { it[SimplifiedBooks.id].value }

            // hits_record와 연관된 hitdetails 행들을 찾아 삭제
            val hitRecordIdsToDelete = HitsRecords.slice(HitsRecords.id)
                .select { HitsRecords.book inList bookIdsToDelete }
                .map { it[HitsRecords.id].value }

            HitDetails.deleteWhere { HitDetails.hitRecord inList hitRecordIdsToDelete }

            // 이제 hits_record 테이블에서 참조하는 행들을 삭제
            HitsRecords.deleteWhere { HitsRecords.book inList bookIdsToDelete }

            // 마지막으로 SimplifiedBooks 테이블에서 행을 삭제
           val book= SimplifiedBooks.deleteWhere { SimplifiedBooks.itemId inList itemIdList }
            println("SimplifiedBooks에서 삭제된 행의 수: ${book}")
        }
        // 삭제된 캐시 업데이트
        // 캐시에서 삭제된 책 정보 삭제
        itemIdList.forEach { deleteBookFromCache(it) }
        return getBooks()
    }

    fun modifyBook(itemId: Int, updatedData: SimplifiedBookDTO): SimplifiedBookDTO {
        // DB에서 책 정보를 업데이트
        val updatedBook = transaction {
            SimplifiedBooks.update({ SimplifiedBooks.itemId eq itemId }) {
                it[createdDate] = formattedDate
                if (updatedData.title != null) it[title] = updatedData.title
                if (updatedData.link != null) it[link] = updatedData.link
                if (updatedData.author != null) it[author] = updatedData.author
//                if (updatedData.categoryName != null) it[categoryName] = updatedData.categoryName
                if (updatedData.pubDate != null) it[pubDate] = updatedData.pubDate
                if (updatedData.description != null) it[description] = updatedData.description
                if (updatedData.isbn != null) it[isbn] = updatedData.isbn
                if (updatedData.isbn13 != null) it[isbn13] = updatedData.isbn13
//                if (updatedData.itemId != null) it[itemId] = updatedData.itemId
                if (updatedData.priceSales != null) it[priceSales] = updatedData.priceSales
                if (updatedData.priceStandard != null) it[priceStandard] = updatedData.priceStandard
                if (updatedData.stockStatus != null) it[stockStatus] = updatedData.stockStatus
                if (updatedData.cover != null) it[cover] = updatedData.cover
                if (updatedData.categoryId != null) it[categoryId] = updatedData.categoryId
                if (updatedData.categoryName != null) it[categoryName] = updatedData.categoryName
                if (updatedData.publisher != null) it[publisher] = updatedData.publisher
                if (updatedData.customerReviewRank != null) it[customerReviewRank] = updatedData.customerReviewRank
            }
        }
        // 캐시에 수정된 책 정보 업데이트
        saveBookToCache(updatedData)
        return updatedData
    }


    fun addTodayBook(book: TodayBookDTO): TodayBookDTO {
        transaction {
            TodayBooks.insert {
                it[title] = book.title
                it[author] = book.author
                it[priceSales] = book.priceSales
                it[cover] = book.cover
                it[todayLetter] = book.todayLetter
                it[itemId] = book.itemId
                it[readData] = book.readDate
            }
        }

        println("추가된 오늘의 도서 정보: ${book}")
        return book
    }

    fun getTodayBooks(readDate: String? = null): TodayBookDTO? {
        println("getTodayBooks() called")
        return transaction {
            val query = if (readDate != null) {
                TodayBooks.select { TodayBooks.readData eq readDate }
            } else {
                TodayBooks.selectAll()
            }
            query.map { row ->
                TodayBookDTO(
                    title = row[TodayBooks.title],
                    author = row[TodayBooks.author],
                    priceSales = row[TodayBooks.priceSales],
                    cover = row[TodayBooks.cover],
                    todayLetter = row[TodayBooks.todayLetter],
                    itemId = row[TodayBooks.itemId],
                    readDate = row[TodayBooks.readData]
                )
            }
                .singleOrNull()
        }
    }

    //통계
    fun findOrCreateBookByItemId(itemId: Int): BookDTO {
        val existingBook = SimplifiedBooks.select { SimplifiedBooks.itemId eq itemId }.singleOrNull()

        return if (existingBook != null) {
            // 도서가 존재하는 경우 해당 도서의 DTO를 반환
            BookDTO(
                id = existingBook[SimplifiedBooks.id].value,
                itemId = existingBook[SimplifiedBooks.itemId],
                title = existingBook[SimplifiedBooks.title],
                author = existingBook[SimplifiedBooks.author],
                publisher = existingBook[SimplifiedBooks.publisher],
                categoryName = existingBook[SimplifiedBooks.categoryName]
            )
        } else {
            // 존재하지 않으면 ->1. DB 새로운 도서를 추가
            println("신간 itemId: ${itemId}")
            val newBookId = SimplifiedBooks.insertAndGetId {
                it[createdDate] = formattedDate
                it[this.itemId] = itemId
                // 다른 필드들은 기본값 또는 null 처리
                it[title] = "Unknown"
                it[author] = "Unknown"
                it[publisher] = "Unknown"
                it[categoryName] = "Unknown"
                it[createdDate] = formattedDate
                it[link] = "Unknown"
                it[pubDate] = "Unknown"
                it[description] = "Unknown"
                it[isbn] = "Unknown"
                it[isbn13] = "Unknown"
                it[priceSales] = 0
                it[priceStandard] = 0
                it[stockStatus] = "Unknown"
                it[cover] = "Unknown"
                it[categoryId] = 0
                it[customerReviewRank] = 0

            }.value
            // 새로 추가된 도서의 DTO 생성
            val newBookDTO = BookDTO(
                id = newBookId,
                itemId = itemId,
                title = "Unknown",
                author = "Unknown",
                publisher = "Unknown",
                categoryName = "Unknown"
            )
            alarmService.sendNotification(itemId)
            println("SimplifiedBooks 신간추가: ${itemId}")
            return newBookDTO
        }
    }
}

