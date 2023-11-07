    package com.example.app_backend.manager.redis

    import com.example.app_backend.manager.statistics.RedisData
    import org.springframework.data.redis.core.RedisTemplate
    import org.springframework.data.redis.core.ScanOptions
    import org.springframework.stereotype.Service
    import java.time.LocalDate

    @Service
    class RedisService(private val redisTemplate: RedisTemplate<String, String>) {

        fun getPreviousDataByDate(date: LocalDate): RedisData? {
            val previousDate = date.minusDays(1)
            return getDataByDate(previousDate).firstOrNull() // previousDate의 첫 번째 아이템을 가져옵니다. 아이템이 없는 경우 null을 반환합니다.
        }

        fun getDataByDate(date: LocalDate): List<RedisData> {
            val scanOptions = ScanOptions.scanOptions().match("*:$date").build()
            val cursor = redisTemplate.executeWithStickyConnection { it.scan(scanOptions) }

            val redisDataList = mutableListOf<RedisData>()

            cursor.use {
                it.forEachRemaining {
                    val itemId = String(it, Charsets.UTF_8).split(":")[0] // UTF-8 문자열로 변환하고 ':'을 기준으로 분리
                    val stockStatus = redisTemplate.opsForValue().get(itemId) ?: ""
                    val increase = redisTemplate.opsForValue().get("$itemId:increase:$date") ?: "0"
                    val decrease = redisTemplate.opsForValue().get("$itemId:decrease:$date") ?: "0"
                    redisDataList.add(RedisData(itemId, stockStatus, increase, decrease))  // RedisData 객체 생성 시 이전 재고 상태를 포함시킵니다.
                }
            }

            return redisDataList
        }
        fun flushAll(): String {
            redisTemplate.connectionFactory?.connection?.flushAll()
            return "All data deleted successfully."
        }
    }