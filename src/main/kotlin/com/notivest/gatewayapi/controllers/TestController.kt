package com.notivest.gatewayapi.controllers

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/test")
class TestController {
    /**
     * Endpoint de prueba simple - requiere autenticación
     */
    @GetMapping("/hello")
    fun hello(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Map<String, Any>> {
        val response =
            mapOf(
                "message" to "¡Hola! Autenticación exitosa 🎉",
                "timestamp" to Instant.now().toString(),
                "status" to "authenticated",
                "user" to
                    mapOf(
                        "sub" to jwt.subject,
                        "email" to (jwt.getClaim<String>("email") ?: "No email found"),
                        "name" to (jwt.getClaim<String>("name") ?: "No name found"),
                        "nickname" to (jwt.getClaim<String>("nickname") ?: "No nickname found"),
                    ),
                "token_info" to
                    mapOf(
                        "issuer" to jwt.issuer,
                        "audience" to jwt.audience,
                        "expires_at" to jwt.expiresAt,
                        "issued_at" to jwt.issuedAt,
                    ),
            )

        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para ver todos los claims del JWT
     */
    @GetMapping("/claims")
    fun claims(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Map<String, Any>> {
        val response =
            mapOf(
                "message" to "JWT Claims completos",
                "timestamp" to Instant.now().toString(),
                "claims" to jwt.claims,
            )

        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para verificar roles/permisos
     */
    @GetMapping("/roles")
    fun roles(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Map<String, Any>> {
        val roles = jwt.getClaim<List<String>>("https://notivest.com/roles") ?: emptyList()
        val scopes = jwt.getClaim<String>("scope")?.split(" ") ?: emptyList()

        val response =
            mapOf(
                "message" to "Información de roles y permisos",
                "timestamp" to Instant.now().toString(),
                "user_id" to jwt.subject,
                "roles" to roles,
                "scopes" to scopes,
                "has_admin_role" to roles.contains("admin"),
                "has_user_role" to roles.contains("user"),
            )

        return ResponseEntity.ok(response)
    }
}
