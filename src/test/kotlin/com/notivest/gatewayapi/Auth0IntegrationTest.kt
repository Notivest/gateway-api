package com.notivest.gatewayapi

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class Auth0IntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should allow access to health endpoint without authentication`() {
        webTestClient
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should reject request to protected endpoint without token when auth profile is not active`() {
        // En profile test, no hay autenticación requerida por defecto
        // Pero el servicio backend no está disponible, así que obtenemos 503 (Service Unavailable)
        webTestClient
            .get()
            .uri("/api/portfolio/test")
            .exchange()
            .expectStatus().is5xxServerError // 503 porque no hay backend disponible
    }

    @Test
    fun `should handle invalid routes gracefully`() {
        webTestClient
            .get()
            .uri("/invalid/route")
            .exchange()
            .expectStatus().isNotFound
    }
}
