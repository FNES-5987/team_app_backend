package com.example.app_backend

import com.example.app_backend.manager.redis.cacheInventoryInRedis
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling
<<<<<<< HEAD
import org.springframework.web.servlet.config.annotation.EnableWebMvc
=======
>>>>>>> origin/modules/manager

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableCaching
<<<<<<< HEAD
@EnableWebMvc
=======
>>>>>>> origin/modules/manager
class TeamApplication

fun main(args: Array<String>) {
    runApplication<TeamApplication>(*args)
    cacheInventoryInRedis()
}
