package com.notivest.gatewayapi

import com.notivest.gatewayapi.filters.CorrelationIdFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CorrelationIdFilterTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `echoes provided correlation id`() {
        val correlationId = "test-correlation-id"

        webTestClient
            .get()
            .uri("/api/portfolio/test")
            .header(CorrelationIdFilter.HEADER_CORRELATION_ID, correlationId)
            .exchange()
            .expectStatus().is5xxServerError
            .expectHeader().valueEquals(CorrelationIdFilter.HEADER_CORRELATION_ID, correlationId)
    }

    @Test
    fun `generates correlation id when request does not include one`() {
        val generated =
            webTestClient
                .get()
                .uri("/api/portfolio/test")
                .exchange()
                .expectStatus().is5xxServerError
                .returnResult(String::class.java)
                .responseHeaders
                .getFirst(CorrelationIdFilter.HEADER_CORRELATION_ID)

        assertThat(generated).isNotBlank()
    }
}
