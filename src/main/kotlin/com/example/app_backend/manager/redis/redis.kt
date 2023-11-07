package com.example.app_backend.manager.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import redis.clients.jedis.Jedis
import java.sql.DriverManager
import java.time.LocalDate

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        return template
    }
}

fun cacheInventoryInRedis() {
    // MySQL 연결 설정
    val url = "jdbc:mysql://localhost:3306/books"
    val username = "root"
    val password = "password1234!"
    val connection = DriverManager.getConnection(url, username, password)

    // inventory 테이블 데이터 조회
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SELECT itemId, stockStatus FROM inventory")

    // Redis 연결 설정
    val jedis = Jedis("192.168.100.177", 6379)

    try {
        // 조회 결과를 Redis에 저장
        while (resultSet.next()) {
            val itemId = resultSet.getString("itemId")
            val stockStatus = resultSet.getString("stockStatus")
//            println("Saving to Redis: $itemId -> $stockStatus") // 로깅 추가
            jedis.set(itemId, stockStatus)

            // SQL 연결 설정
            val statement = connection.prepareStatement(
                    "INSERT INTO inventory_history (itemId, date, stockStatus) VALUES (?, CURDATE(), ?)" +
                            "ON DUPLICATE KEY UPDATE stockStatus = VALUES(stockStatus)"
            )
            statement.setString(1, itemId)
            statement.setString(2, stockStatus)
            statement.executeUpdate()
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

fun getStockStatusFromRedis(itemId: String): Triple<String?, String?, String?> {
    Jedis("192.168.100.177", 6379).use { jedis ->
        // 현재 날짜를 가져옴
        val today = LocalDate.now()

        val stockStatus = jedis.get(itemId)
        val statusIncrease = jedis.get("$itemId:increase:$today")
        val statusDecrease = jedis.get("$itemId:decrease:$today")

//        println("Retrieved from Redis: $itemId -> $stockStatus (+$statusIncrease, -$statusDecrease) on $today")

        return Triple(stockStatus, statusIncrease, statusDecrease)
    }
}
fun cacheInventoryInRedis(itemId: String, newStockStatus: String) {
    // Redis 연결 설정
    val jedis = Jedis("localhost", 6379)

    // MySQL 연결 설정
    val url = "jdbc:mysql://localhost:3306/books"
    val username = "root"
    val password = "password1234!"
    val connection = DriverManager.getConnection(url, username, password)

    try {
        // 기존의 stockStatus를 가져옴
        val oldStockStatus = jedis.get(itemId)?.toInt() ?: 0

        // 변동값 계산
        val statusChange = newStockStatus.toInt() - oldStockStatus
        val statusIncrease = if (statusChange > 0) statusChange else 0
        val statusDecrease = if (statusChange < 0) -statusChange else 0

        // 현재 날짜를 가져옴
        val today = LocalDate.now()

//        println("Saving to Redis: $itemId -> $newStockStatus (+$statusIncrease, -$statusDecrease) on $today") // 로깅 추가
        jedis.set(itemId, newStockStatus)
        jedis.set("$itemId:increase:$today", statusIncrease.toString())
        jedis.set("$itemId:decrease:$today", statusDecrease.toString())

        // 데이터베이스에 재고 상태 저장
        val statement = connection.prepareStatement(
                "INSERT INTO inventory_history (itemId, date, stockStatus, increase, decrease) VALUES (?, ?, ?, ?, ?)"
        )
        statement.setString(1, itemId)
        statement.setDate(2, java.sql.Date.valueOf(today))
        statement.setString(3, newStockStatus)
        statement.setInt(4, statusIncrease)
        statement.setInt(5, statusDecrease)
        statement.executeUpdate()
    } catch (e: Exception) {
        // 예외 출력
        e.printStackTrace()
    } finally {
        // 연결 종료
        jedis.close()
        connection.close()
    }
}