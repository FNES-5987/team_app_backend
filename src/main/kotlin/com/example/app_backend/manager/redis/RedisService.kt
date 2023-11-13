    package com.example.app_backend.manager.redis

    import com.example.app_backend.manager.statistics.RedisData
    import org.springframework.data.redis.core.RedisTemplate
    import org.springframework.data.redis.core.ScanOptions
    import org.springframework.stereotype.Service
    import redis.clients.jedis.Jedis
    import java.sql.DriverManager
    import java.time.LocalDate

    @Service
    class RedisService(private val redisTemplate: RedisTemplate<String, String>) {

        fun getPreviousDataByDate(date: LocalDate): RedisData? {
            val previousDate = date.minusDays(1)
            return getDataByDate(previousDate).firstOrNull()
        }

        fun getDataByDate(date: LocalDate): List<RedisData> {
            val scanOptions = ScanOptions.scanOptions().match("*:$date").build()
            val cursor = redisTemplate.executeWithStickyConnection { it.scan(scanOptions) }
            val redisDataList = mutableListOf<RedisData>()

            cursor.use {
                it.forEachRemaining {
                    val itemId = String(it, Charsets.UTF_8).split(":")[0]
                    val stockStatus = redisTemplate.opsForValue().get(itemId) ?: ""
                    val increase = redisTemplate.opsForValue().get("$itemId:increase:$date") ?: "0"
                    val decrease = redisTemplate.opsForValue().get("$itemId:decrease:$date") ?: "0"
                    val isbn = redisTemplate.opsForValue().get("$itemId:isbn") ?: ""
                    val date = LocalDate.parse(redisTemplate.opsForValue().get("$itemId:date"))
                    redisDataList.add(RedisData(itemId, stockStatus, increase, decrease, isbn, date))
                }
            }
            return redisDataList
        }
        fun flushAll(): String {
            redisTemplate.connectionFactory?.connection?.flushAll()
            return "All data deleted successfully."
        }

        fun cacheInventoryInRedis() {
            val url = "jdbc:mysql://localhost:3306/books"
            val username = "root"
            val password = "password1234!"
            val connection = DriverManager.getConnection(url, username, password)
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT itemId, stockStatus FROM inventory")
            val jedis = Jedis("192.168.100.177", 6379)

            try {
                while (resultSet.next()) {
                    val itemId = resultSet.getString("itemId")
                    val stockStatus = resultSet.getString("stockStatus")
//            println("Saving to Redis: $itemId -> $stockStatus") // 로깅 추가
                    jedis.set(itemId, stockStatus)

                    val statement = connection.prepareStatement(
                            "INSERT INTO inventory_history (itemId, date, stockStatus) VALUES (?, CURDATE(), ?)" +
                                    "ON DUPLICATE KEY UPDATE stockStatus = VALUES(stockStatus)"
                    )
                    statement.setString(1, itemId)
                    statement.setString(2, stockStatus)
                    statement.executeUpdate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                jedis.close()
                connection.close()
            }
        }

    }