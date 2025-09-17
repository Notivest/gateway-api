package com.notivest.gatewayapi.filters

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant

@Component
class AuthenticationErrorHandler(
    private val objectMapper: ObjectMapper,
) : ServerAuthenticationEntryPoint {
    private val logger = LoggerFactory.getLogger(AuthenticationErrorHandler::class.java)

    override fun commence(
        exchange: ServerWebExchange,
        ex: AuthenticationException,
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)

        val errorResponse = createErrorResponse(ex)

        // Log del error para debugging
        logger.warn("Authentication failed: ${errorResponse.message}", ex)

        val buffer: DataBuffer =
            response.bufferFactory().wrap(
                objectMapper.writeValueAsBytes(errorResponse),
            )

        return response.writeWith(Mono.just(buffer))
    }

    private fun createErrorResponse(ex: AuthenticationException): ErrorResponse {
        return when (ex) {
            is OAuth2AuthenticationException -> {
                when {
                    ex.error.errorCode == "invalid_token" ->
                        ErrorResponse(
                            error = "invalid_token",
                            message = "El token JWT proporcionado es inválido",
                            details = "Verifica que el token esté bien formado y no haya expirado",
                        )
                    ex.error.errorCode == "insufficient_scope" ->
                        ErrorResponse(
                            error = "insufficient_scope",
                            message = "El token no tiene los permisos necesarios",
                            details = "Se requieren permisos adicionales para acceder a este recurso",
                        )
                    else ->
                        ErrorResponse(
                            error = "authentication_failed",
                            message = "Error de autenticación OAuth2",
                            details = ex.error.description ?: "Token inválido o expirado",
                        )
                }
            }

            else -> {
                when {
                    ex.message?.contains("JWT") == true ->
                        ErrorResponse(
                            error = "jwt_error",
                            message = "Error procesando el token JWT",
                            details = "Token malformado, expirado o inválido",
                        )
                    ex.message?.contains("Bearer") == true ->
                        ErrorResponse(
                            error = "missing_token",
                            message = "Token de autorización requerido",
                            details = "Incluye el header 'Authorization: Bearer <token>'",
                        )
                    else ->
                        ErrorResponse(
                            error = "unauthorized",
                            message = "Acceso no autorizado",
                            details = "Se requiere autenticación válida para acceder a este recurso",
                        )
                }
            }
        }
    }

    data class ErrorResponse(
        val error: String,
        val message: String,
        val details: String,
        val timestamp: String = Instant.now().toString(),
        val status: Int = 401,
    )
}
