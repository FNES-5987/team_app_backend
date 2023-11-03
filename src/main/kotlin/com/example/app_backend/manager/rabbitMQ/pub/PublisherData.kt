package com.example.app_backend.manager.rabbitMQ.pub

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BookMessageRequest (
        val id: Long,
        val publisher : String,
        val title : String,
        val author : String,
        val pubDate : String,
        val isbn : String,
        val categoryName : String,
        val priceStandard : Int,
        val quantity : Int,
        val imageUuidName: String,
)
