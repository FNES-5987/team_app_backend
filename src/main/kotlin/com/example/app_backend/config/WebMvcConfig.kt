package com.example.app_backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig: WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins(
                "http://localhost:5000",
                "http://localhost:5500",
                    "192.168.100.204",
                    "192.168.100.177"
            )
            .allowedMethods("*")
            .allowCredentials(true)
    }
}