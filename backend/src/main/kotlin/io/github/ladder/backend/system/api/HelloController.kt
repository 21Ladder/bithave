package io.github.ladder.backend.system.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class HelloController {

    @GetMapping("/hello")
    fun hello(): Map<String, String> = mapOf("message" to "Hello Bithave")
}
