package com.notivest.gatewayapi.filters

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Profile("auth")
class UserHeaderFilter : GlobalFilter, Ordered {
    private val logger = LoggerFactory.getLogger(UserHeaderFilter::class.java)

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        return exchange.getPrincipal<JwtAuthenticationToken>()
            .map { auth ->
                val token = auth.token
                val claims = token.claims

                // Extraer información del usuario del JWT
                val userId = claims["sub"]?.toString() ?: "anonymous"
                val email = claims["email"]?.toString()
                val name = claims["name"]?.toString()
                val nickname = claims["nickname"]?.toString()

                // Extraer roles del claim personalizado de Auth0
                val roles = extractRoles(claims)

                // Extraer permisos/scopes
                val scopes = extractScopes(claims)

                logger.debug("Processing request for user: {} with roles: {}", userId, roles)

                // Construir request con headers de usuario
                val requestBuilder =
                    exchange.request.mutate()
                        .header("X-User-ID", userId)

                // Agregar headers opcionales si existen
                email?.let { requestBuilder.header("X-User-Email", it) }
                name?.let { requestBuilder.header("X-User-Name", it) }
                nickname?.let { requestBuilder.header("X-User-Nickname", it) }

                if (roles.isNotEmpty()) {
                    requestBuilder.header("X-User-Roles", roles.joinToString(","))
                }

                if (scopes.isNotEmpty()) {
                    requestBuilder.header("X-User-Scopes", scopes.joinToString(","))
                }

                // Headers adicionales para debugging (solo en desarrollo)
                requestBuilder.header("X-Token-Subject", userId)
                requestBuilder.header("X-Token-Issuer", token.issuer?.toString() ?: "unknown")

                requestBuilder.build()
            }
            .defaultIfEmpty(exchange.request)
            .flatMap { request ->
                chain.filter(exchange.mutate().request(request).build())
            }
    }

    /**
     * Extrae roles del JWT. Auth0 puede usar diferentes claims para roles
     */
    private fun extractRoles(claims: Map<String, Any>): List<String> {
        return when {
            // Custom namespace para roles (recomendado por Auth0)
            claims.containsKey("https://notivest.com/roles") -> {
                extractStringList(claims["https://notivest.com/roles"])
            }
            // Fallback a otros claims comunes
            claims.containsKey("roles") -> {
                extractStringList(claims["roles"])
            }
            claims.containsKey("user_roles") -> {
                extractStringList(claims["user_roles"])
            }
            else -> emptyList()
        }
    }

    /**
     * Extrae scopes/permisos del JWT
     */
    private fun extractScopes(claims: Map<String, Any>): List<String> {
        return when {
            claims.containsKey("scope") -> {
                claims["scope"]?.toString()?.split(" ") ?: emptyList()
            }
            claims.containsKey("permissions") -> {
                extractStringList(claims["permissions"])
            }
            else -> emptyList()
        }
    }

    /**
     * Convierte un claim a lista de strings, manejando diferentes tipos
     */
    @Suppress("UNCHECKED_CAST")
    private fun extractStringList(claim: Any?): List<String> {
        return when (claim) {
            is List<*> -> claim.filterIsInstance<String>()
            is Array<*> -> claim.filterIsInstance<String>()
            is String -> if (claim.contains(",")) claim.split(",") else listOf(claim)
            else -> emptyList()
        }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 1
}
