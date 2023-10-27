package com.example.app_backend.redis

import com.example.app_backend.statistics.RedisData
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
class RedisService(private val redisTemplate: RedisTemplate<String, String>) {

    fun getAllData(page: Int, size: Int): List<RedisData> {
        val pageRequest = PageRequest.of(page, size)
        val scanOptions = ScanOptions.scanOptions().match("*").count(pageRequest.pageSize.toLong()).build()
        val cursor = redisTemplate.executeWithStickyConnection { it.scan(scanOptions) }

        val redisDataList = mutableListOf<RedisData>()
        cursor.use {
            it.forEachRemaining {
                val itemId = it.toString()
                val stockstatus = try {
                    redisTemplate.opsForValue().get(itemId) ?: ""
                } catch (e: Exception) {
                    // 로깅 또는 적절한 예외 처리를 수행합니다.
                    ""
                }
                redisDataList.add(RedisData(itemId, stockstatus))
            }
        }

        // 페이지에 맞는 데이터를 반환합니다.
        val start = pageRequest.offset.toInt()
        val end = min(start + pageRequest.pageSize, redisDataList.size)
        return redisDataList.subList(start, end)
    }
}
