package com.notivest.gateway_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GatewayApiApplication

fun main(args: Array<String>) {
	runApplication<GatewayApiApplication>(*args)
}
