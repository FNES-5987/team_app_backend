package com.example.app_backend.inventory

// data.kt

data class InventoryResponse(
    val id : Long,
    val publisher: String,
    val title : String,
    val link : String,
    val author : String,
    val pubDate: String,
    val isbn: String,
    val isbn13: String,
    val itemId: Int,
    val categoryId: Int,
    val categoryName: String,
    val priceSales: Int,
    val priceStandard: Int,
    val stockStatus: String,
    val cover : String,
)

data class InventoryCreateRequest(
    val publisher:String?,
    val title:String?,
    val link:String?,
    val author:String?,
    val pubDate:String?,
    val isbn:String?,
    val isbn13:String?,
    var itemId:Int?,
    var categoryId:Int?,
    var categoryName:String?,
    var priceSales:Int?,
    var priceStandard:Int? ,
    var stockStatus :String? ,
    var cover :String?
) {
    fun validate() = !publisher.isNullOrEmpty() && !title.isNullOrEmpty()
}

data class InventoryModifyRequest(
    val id : Long?,
    val publisher: String?,
    val title : String?,
    val link : String?,
    val author : String?,
    val pubDate: String?,
    val isbn: String?,
    val isbn13: String?,
    val itemId: Int?,
    val categoryId: Int?,
    val categoryName: String?,
    val priceSales: Int?,
    val priceStandard: Int?,
    val stockStatus: String?,
    val cover : String?,
)


