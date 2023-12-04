package com.example.app_backend

import com.example.app_backend.admin.book.SimplifiedBookDTO
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableCaching
class TeamApplication

fun main(args: Array<String>) {
    runApplication<TeamApplication>(*args)
}
