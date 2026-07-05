package com.notivest.gatewayapi.filters

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class RequestLoggingFilter : GlobalFilter, Ordered {
    private val logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        val request = exchange.request
        val startTime = System.currentTimeMillis()
        val correlationId =
            exchange.getAttribute<String>(CorrelationIdFilter.ATTRIBUTE_CORRELATION_ID)
                ?: request.headers.getFirst(CorrelationIdFilter.HEADER_CORRELATION_ID)
                ?: "unknown"

        logger.info(
            "incoming-request correlationId={} method={} path={} remoteIp={} userAgent={}",
            correlationId,
            request.method,
            request.path.value(),
            request.remoteAddress?.address?.hostAddress ?: "unknown",
            request.headers.getFirst("User-Agent") ?: "unknown",
        )

        // Log authentication headers if present
        val authHeader = request.headers.getFirst("Authorization")
        if (authHeader != null) {
            logger.debug(
                "Request has Authorization header: {}",
                if (authHeader.startsWith("Bearer ")) "Bearer [REDACTED]" else "Non-Bearer auth",
            )
        } else {
            logger.debug("Request has no Authorization header")
        }

        return chain.filter(exchange).doFinally {
            val duration = System.currentTimeMillis() - startTime
            val response = exchange.response

            logger.info(
                "request-completed correlationId={} method={} path={} status={} durationMs={}",
                correlationId,
                request.method,
                request.path.value(),
                response.statusCode?.value() ?: "unknown",
                duration,
            )
        }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 10
}
