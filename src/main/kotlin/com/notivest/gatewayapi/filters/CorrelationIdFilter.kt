package com.notivest.gatewayapi.filters

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class CorrelationIdFilter : GlobalFilter, Ordered {
    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        val correlationId =
            exchange.request.headers.getFirst(HEADER_CORRELATION_ID)
                ?.takeIf(String::isNotBlank)
                ?: exchange.request.headers.getFirst(HEADER_REQUEST_ID)?.takeIf(String::isNotBlank)
                ?: UUID.randomUUID().toString()

        val request =
            exchange.request.mutate()
                .headers { headers -> headers.set(HEADER_CORRELATION_ID, correlationId) }
                .build()

        exchange.response.headers.set(HEADER_CORRELATION_ID, correlationId)

        val mutatedExchange =
            exchange.mutate()
                .request(request)
                .build()

        mutatedExchange.attributes[ATTRIBUTE_CORRELATION_ID] = correlationId
        return chain.filter(mutatedExchange)
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    companion object {
        const val HEADER_CORRELATION_ID = "X-Correlation-Id"
        private const val HEADER_REQUEST_ID = "X-Request-Id"
        const val ATTRIBUTE_CORRELATION_ID = "correlationId"
    }
}
