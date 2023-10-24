package com.example.app_backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebMvcConfig: WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins(
                "http://localhost:5000",
                "http://localhost:5500",
            ) // 로컬 호스트 origin 허용
            .allowedMethods("*")
            .allowCredentials(true)
    }
}