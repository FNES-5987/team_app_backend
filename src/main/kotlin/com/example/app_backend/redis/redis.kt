package com.example.app_backend.redis

import redis.clients.jedis.Jedis
import java.sql.DriverManager

fun cacheInventoryInRedis() {
    // MySQL 연결 설정
    val url = "jdbc:mysql://localhost:3306/books"
    val username = "root"
    val password = "password1234!"
    val connection = DriverManager.getConnection(url, username, password)

    // 데이터 조회
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SELECT itemId, stockStatus FROM inventory")

    // Redis 연결 설정
    val jedis = Jedis("localhost", 6379)

    try {
        // 조회 결과를 Redis에 저장
        while (resultSet.next()) {
            val itemId = resultSet.getString("itemId")
            val stockStatus = resultSet.getString("stockStatus")
            println("Saving to Redis: $itemId -> $stockStatus") // 로깅 추가
            jedis.set(itemId, stockStatus)
        }
    } catch (e: Exception) {
        // 예외 출력
        e.printStackTrace()
    } finally {
        // 연결 종료
        jedis.close()
        connection.close()
    }
}

fun getStockStatusFromRedis(itemId: String): String? {
    Jedis("localhost", 6379).use { jedis ->
        return jedis.get(itemId)?.also { stockStatus ->
            println("Retrieved from Redis: $itemId -> $stockStatus")
        }
    }
}
