package com.example.app_backend.manager.statistics

import java.time.LocalDate

data class RedisData(val itemId: String, val stockStatus: String, val increase: String, val decrease: String, val isbn: String, val date: LocalDate?)
