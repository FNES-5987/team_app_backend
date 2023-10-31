package com.example.app_backend.redis

import com.example.app_backend.statistics.RedisData
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Service

@Service
class RedisService(private val redisTemplate: RedisTemplate<String, String>) {

    fun getAllData(): List<RedisData> {
        val scanOptions = ScanOptions.scanOptions().match("*").build()
        val cursor = redisTemplate.executeWithStickyConnection { it.scan(scanOptions) }

        val redisDataList = mutableListOf<RedisData>()
        cursor.use {
            it.forEachRemaining {
                val itemId = String(it, Charsets.UTF_8) // UTF-8 문자열로 변환
                val stockstatus = try {
                    redisTemplate.opsForValue().get(itemId) ?: ""
                } catch (e: Exception) {
                    // 로깅 또는 적절한 예외 처리를 수행합니다.
                    ""
                }
                redisDataList.add(RedisData(itemId, stockstatus))
            }
        }

        return redisDataList
    }
}
