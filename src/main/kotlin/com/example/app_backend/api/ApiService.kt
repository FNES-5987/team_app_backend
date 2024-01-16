package com.example.app_backend.api

import com.example.app_backend.admin.book.BookService
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Service
class ApiService(private val aladinClient: AladinClient, private val bookService: BookService) {
//    private val mapper = jacksonObjectMapper()
//    @Scheduled(fixedRate = 1000 * 60 * 60 * 24*7)
    fun fetchGetBest() {
    try {
        transaction {
            // 트랜잭션 내에서 작업 실행
            val currentTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm")
            val formattedDate = currentTime.format(formatter)
            println(formattedDate)
            println("--called by schedule best: ${formattedDate}--")

            // 리스트(best)의 item
            // `item`은 API 응답의 구조, 반환하는 JSON 객체의 키 이름(변경X)
            val bestItem = aladinClient.getBest().item
            println(bestItem)

            for (best in bestItem) {
                saveBest(best, formattedDate)
                // 해당 출판사 저장
                val pub = best.publisher
                savePublisher(pub, formattedDate)
//                    println("출판사 저장")
            }
        }
        println("redis초기화")
        bookService.initializeCache()
    } catch (e: Exception) {
        println("알라딘 DB 저장 실패: ${e.message}")
    }
    }
    
    fun fetchGetBook(publisher: String, formattedDate: String){
        try {
            val bookItem = aladinClient.getBook(publisher).item
//            println(bookItem)
                for (book in bookItem) {
                    saveBook(book, formattedDate)
                }
            }

        finally {
            // 트랜잭션 종료
            transaction { commit() }
        }
    }

    //전체 데이터
    fun saveBest(best: BestResponse, formattedDate: String) {
        // 중복 책 제외
        transaction {
            val existingBest = Best.select { Best.isbn eq best.isbn }.firstOrNull()
            if (existingBest == null) {
                Best.insert {
                    it[createdDate] = formattedDate
//                it[version] = best.version
//                it[logo] = best.logo
                    it[title] = best.title
                    it[author] = best.author
                    it[pubDate] = best.pubDate
                    it[description] = best.description
                    it[isbn] = best.isbn
                    it[isbn13] = best.isbn13
                    it[itemId] = best.itemId
                    it[priceSales] = best.priceSales
                    it[priceStandard] = best.priceStandard
                    it[mallType] = best.mallType
                    it[stockStatus] = best.stockStatus
                    it[mileage] = best.mileage
                    it[cover] = best.cover
                    it[categoryId] = best.categoryId
                    it[categoryName] = best.categoryName
                    it[publisher] = best.publisher

                }
            }
        }

    }

    // 출판사만
    fun savePublisher(pub: String, formattedDate: String) {
        transaction {
            val existingPublisher = UniquePublisher.select { UniquePublisher.publisher eq pub }.firstOrNull()
            if (existingPublisher == null) {
                UniquePublisher.insert {
                    it[publisher] = pub
                }
                fetchGetBook(pub, formattedDate)
                println("--called by schedule ${pub}: ${formattedDate}--")
            }
        }
    }

    fun saveBook(book: BookResponse,formattedDate: String){
        transaction {
                BookByPublisher.insert {
                    it[createdDate] = formattedDate
//                it[version] = best.version
//                it[logo] = best.logo
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
                    it[mallType] = book.mallType
                    it[stockStatus] = book.stockStatus
                    it[mileage] = book.mileage
                    it[cover] = book.cover
                    it[categoryId] = book.categoryId
                    it[categoryName] = book.categoryName
                    it[publisher] = book.publisher
                    it[salesPoint] = salesPoint
                    it[adult] = adult
                    it[fixedPrice] = fixedPrice
                    it[customerReviewRank] = customerReviewRank
                }
            copyDataToSimplifiedBooks()
            }
        }
    //book 일부만
    fun copyDataToSimplifiedBooks() {
        transaction {
            BookByPublisher.selectAll().forEach { row ->
                val isbnValue = row[BookByPublisher.isbn]
                val existingBook = SimplifiedBooks.select { SimplifiedBooks.isbn eq isbnValue }.firstOrNull()

                if (existingBook == null) {
                    SimplifiedBooks.insert {
                        it[createdDate] = row[BookByPublisher.createdDate]
                        it[publisher] = row[BookByPublisher.publisher]
                        it[title] = row[BookByPublisher.title]
                        it[link] = row[BookByPublisher.link]
                        it[author] = row[BookByPublisher.author]
                        it[pubDate] = row[BookByPublisher.pubDate]
                        it[description] = row[BookByPublisher.description]
                        it[isbn] = isbnValue
                        it[isbn13] = row[BookByPublisher.isbn13]
                        it[itemId] = row[BookByPublisher.itemId]
                        it[priceSales] = row[BookByPublisher.priceSales]
                        it[priceStandard] = row[BookByPublisher.priceStandard]
                        it[stockStatus] = row[BookByPublisher.stockStatus]
                        it[cover] = row[BookByPublisher.cover]
                        it[categoryId] = row[BookByPublisher.categoryId]
                        it[categoryName] = row[BookByPublisher.categoryName]
                        it[customerReviewRank] = row[BookByPublisher.customerReviewRank]
                    }
                }
            }
        }
    }

}

