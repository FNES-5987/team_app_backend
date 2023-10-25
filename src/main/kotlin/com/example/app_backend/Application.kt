package com.example.app_backend

import com.example.app_backend.redis.cacheInventoryInRedis
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
class TeamApplication

fun main(args: Array<String>) {
    runApplication<TeamApplication>(*args)
    cacheInventoryInRedis()
}
