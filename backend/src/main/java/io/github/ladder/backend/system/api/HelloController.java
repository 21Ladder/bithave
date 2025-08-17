package io.github.ladder.backend.system.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/v1") // v1
public class HelloController {

    @GetMapping("/hello") // GET mapping als test
    public Map<String, String> hello() {
        // test um auf response zu überprüfen, nur JSON
        return Map.of("message", "Hello Bithave");
    }
}
