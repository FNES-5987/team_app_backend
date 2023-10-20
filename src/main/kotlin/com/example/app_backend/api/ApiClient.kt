package com.example.app_backend.api


import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class BestListResponse(
    val item: List<BestResponse>
)
data class BookListResponse(
    val item: List<BookResponse>
)

data class FormattedDate(
    val createdDate: String
)

data class PublisherResponse (
    val publisher: String
)
data class BestResponse(
    val version: String?,
    val logo: String?,
    val title: String,
    val author: String,
    val pubDate: String,
    val description: String,
    val isbn: String,
    val isbn13: String,
    val itemId: Int,
    val priceSales: Int,
    val priceStandard: Int,
    val mallType: String,
    val stockStatus: String,
    val mileage: Int,
    val cover: String,
    val categoryId: Int,
    val categoryName: String,
    val publisher: String,
)


// after Best
data class BookResponse (
    val title: String,
    val link : String,
    val author: String,
    val pubDate: String,
    val description: String,
    val isbn: String,
    val isbn13: String,
    val itemId: Int,
    val priceSales: Int,
    val priceStandard: Int,
    val mallType: String,
    val stockStatus: String,
    val mileage: Int,
    val cover: String,
    val categoryId: Int,
    val categoryName: String,
    val publisher: String,
    val salesPoint: Int,
    val adult: Boolean,
    val fixedPrice: Boolean,
    val customerReviewRank: Int
)
// 리스트
// 베스트 셀러 http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=ttbrlathgus5411950001&QueryType=Bestseller&MaxResults=100&start=1&SearchTarget=Book&output=xml&Version=20131101
@FeignClient(name = "aladdin", url = "http://www.aladin.co.kr/ttb/api")
interface AladinClient {
    @GetMapping("/ItemList.aspx?ttbkey=ttbrlathgus5411950001&QueryType=Bestseller&MaxResults=100&start=1&SearchTarget=Book&output=js&Version=20131101")
    fun getBest(): BestListResponse
    @GetMapping("/ItemSearch.aspx?ttbkey=ttbrlathgus5411950001&QueryType=Publisher&MaxResults=100&start=1&SearchTarget=Book&Version=20131101&output=js")
    fun getBook(@RequestParam("Query") publisher: String): BookListResponse

}



