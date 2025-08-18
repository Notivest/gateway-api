package com.notivest.gatewayapi.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityProfiles {
    @Bean
    @Profile("!auth")
    fun openChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .authorizeExchange { it.anyExchange().permitAll() }
            .build()
    }

    @Bean
    @Profile("auth")
    fun authChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/actuator/health", "/actuator/info").permitAll()
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt { } }
            .build()
    }
}
