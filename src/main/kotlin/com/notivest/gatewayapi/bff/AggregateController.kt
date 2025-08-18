package com.notivest.gatewayapi.bff

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@RestController
class AggregateController(
    private val web : WebClient
) {

    @GetMapping("/api/aggregate/portfolio-summary" , produces = [MediaType.APPLICATION_JSON_VALUE])
    fun summary(@RequestHeader(name = "X-User-Id", required = false) userId: String?) : Mono<Map<String, Any>> {
        val uid = userId ?: "anonymous"
        val p = web.get().uri("http://portfolio-service:8080/summary?user=$uid").retrieve().bodyToMono(Map::class.java)
        val pr = web.get().uri("http://price-fetcher:8080/last?user=$uid").retrieve().bodyToMono(Map::class.java)
        return Mono.zip(p, pr).map { t -> mapOf("portfolio" to t.t1, "prices" to t.t2) }
    }
}