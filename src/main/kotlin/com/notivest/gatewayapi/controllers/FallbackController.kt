package com.notivest.gatewayapi.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/fallback")
class FallbackController {
    @GetMapping("/portfolio")
    fun portfolioFallback(): ResponseEntity<Map<String, Any>> {
        val response =
            mapOf(
                "error" to "service_unavailable",
                "message" to "Portfolio service is temporarily unavailable",
                "service" to "portfolio-service",
                "timestamp" to Instant.now().toString(),
                "status" to 503,
            )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
    }

    @GetMapping("/prices")
    fun pricesFallback(): ResponseEntity<Map<String, Any>> {
        val response =
            mapOf(
                "error" to "service_unavailable",
                "message" to "Prices service is temporarily unavailable",
                "service" to "prices-service",
                "timestamp" to Instant.now().toString(),
                "status" to 503,
            )
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
    }
}
