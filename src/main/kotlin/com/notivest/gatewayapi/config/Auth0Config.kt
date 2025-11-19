package com.notivest.gatewayapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter

@Configuration
@Profile("auth")
class Auth0Config {
    @Value("\${auth0.domain}")
    private lateinit var domain: String

    @Value("\${auth0.audience}")
    private lateinit var audience: String

    /**
     * Configura el decoder JWT personalizado para Auth0 con soporte para JWE
     */
    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val issuer = "https://$domain/"
        val jwkSetUrl = "https://$domain/.well-known/jwks.json"

        // Crear decoder usando el constructor que acepta JWK Set URL
        val jwtDecoder = NimbusReactiveJwtDecoder(jwkSetUrl)

        // Configurar validadores
        val audienceValidator = AudienceValidator(audience)
        val withIssuer = JwtValidators.createDefaultWithIssuer(issuer)
        val withAudience = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)

        jwtDecoder.setJwtValidator(withAudience)

        return jwtDecoder
    }

    /**
     * Configura el converter de autenticación JWT para extraer roles y authorities
     */
    @Bean
    fun jwtAuthenticationConverter(): ReactiveJwtAuthenticationConverter {
        val authoritiesConverter =
            JwtGrantedAuthoritiesConverter().apply {
                // Buscar roles en el claim personalizado de Auth0
                setAuthoritiesClaimName("https://notivest.com/roles")
                setAuthorityPrefix("ROLE_")
            }

        return ReactiveJwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(ReactiveJwtGrantedAuthoritiesConverterAdapter(authoritiesConverter))
        }
    }
}

/**
 * Validador personalizado para verificar el audience de Auth0
 */
class AudienceValidator(private val expectedAudience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val audiences = jwt.audience

        return if (audiences != null && audiences.contains(expectedAudience)) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_audience", "The required audience is missing", null))
        }
    }
}
