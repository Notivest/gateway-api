package com.notivest.gatewayapi.security

import com.notivest.gatewayapi.filters.AuthenticationErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityProfiles(
    private val authenticationErrorHandler: AuthenticationErrorHandler,
) {
    @Bean
    @Profile("!auth")
    fun openChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .authorizeExchange { it.anyExchange().permitAll() }
            .build()
    }

    @Bean
    @Profile("auth")
    fun authChain(
        http: ServerHttpSecurity,
        jwtDecoder: ReactiveJwtDecoder,
        jwtAuthenticationConverter: ReactiveJwtAuthenticationConverter,
    ): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    // Endpoints públicos (health checks)
                    .pathMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/api/*/actuator/health",
                        "/api/*/actuator/info",
                    ).permitAll()
                    // Todos los demás requieren autenticación
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtDecoder(jwtDecoder)
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
                // Manejo personalizado de errores de autenticación
                oauth2.authenticationEntryPoint(authenticationErrorHandler)
            }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(authenticationErrorHandler)
            }
            .build()
    }
}
