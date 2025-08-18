package com.notivest.gatewayapi.filters

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono


@Component
@Profile("auth")
class UserHeaderFilter : GlobalFilter {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return exchange.getPrincipal<JwtAuthenticationToken>()
            .map { auth ->
                // Toma claim custom o 'sub' y lo pasa a String
                val userId = ( auth.token.claims["sub"]
                    ?: "anonymous").toString()

                // Mutamos el request agregando el header
                exchange.request.mutate()
                    .header("X-User-Id", userId)
                    .build()
            }
            // Si no hay principal (no debería con el perfil 'auth'), seguimos igual
            .defaultIfEmpty(exchange.request)
            // Continuamos la cadena con el request (mutado o original)
            .flatMap { req -> chain.filter(exchange.mutate().request(req).build()) }
    }
}