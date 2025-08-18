package com.notivest.gatewayapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource


@Configuration
class CorsConfig {

    @Bean
    @Profile("dev") // en prod restringí orígenes/métodos
    fun corsWebFilter(): CorsWebFilter {
        val c = CorsConfiguration().apply {
            // ⚠️ En dev: todo abierto para no trabarte
            allowCredentials = false          // true requiere especificar origins exactos, no '*'
            allowedOrigins = listOf("*")      // en prod: cambia a tu dominio (p.ej. https://app.midominio.com)
            allowedMethods = listOf("GET","POST","PUT","PATCH","DELETE","OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("X-Request-Id","X-User-Id")
            maxAge = 3600                     // cacheo del preflight (1h)
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", c)
        return CorsWebFilter(source)
    }
}