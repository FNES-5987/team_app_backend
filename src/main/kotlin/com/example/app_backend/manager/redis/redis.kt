package com.example.app_backend.manager.redis

import redis.clients.jedis.Jedis
import java.sql.DriverManager
import java.time.LocalDate

fun getStockStatusFromRedis(itemId: String): Triple<String?, String?, String?> {
    Jedis("192.168.100.177", 6379).use { jedis ->
        val today = LocalDate.now()

        val stockStatus = jedis.get(itemId)
        val statusIncrease = jedis.get("$itemId:increase:$today")
        val statusDecrease = jedis.get("$itemId:decrease:$today")

//        println("Retrieved from Redis: $itemId -> $stockStatus (+$statusIncrease, -$statusDecrease) on $today")

        return Triple(stockStatus, statusIncrease, statusDecrease)
    }
}
fun cacheInventoryInRedis(itemId: String, newStockStatus: String, isbn: String, date: LocalDate?) {

    val jedis = Jedis("localhost", 6379)
    val url = "jdbc:mysql://localhost:3306/books"
    val username = "root"
    val password = "password1234!"
    val connection = DriverManager.getConnection(url, username, password)

    try {
        val oldStockStatus = jedis.get(itemId)?.toInt() ?: 0

        val statusChange = newStockStatus.toInt() - oldStockStatus
        val statusIncrease = if (statusChange > 0) statusChange else 0
        val statusDecrease = if (statusChange < 0) -statusChange else 0
        val today = date ?: LocalDate.now()

//        println("Saving to Redis: $itemId -> $newStockStatus (+$statusIncrease, -$statusDecrease) on $today") // 로깅 추가
        jedis.set(itemId, newStockStatus)
        jedis.set("$itemId:increase:$today", statusIncrease.toString())
        jedis.set("$itemId:decrease:$today", statusDecrease.toString())
        jedis.set("$itemId:isbn", isbn)
        jedis.set("$itemId:date", today.toString())

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
        e.printStackTrace()
    } finally {
        jedis.close()
        connection.close()
    }
}