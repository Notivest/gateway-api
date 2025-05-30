package com.notivest.gateway_api.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping

class TestController {

    @GetMapping("/test")
    fun testServer(): ResponseEntity<*> {
        return ResponseEntity.ok("Works")
    }
}