package io.github.ladder.backend

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@Disabled("Context loading skipped in unit test suite")
@SpringBootTest
class BackendApplicationTests {

    @Test
    fun contextLoads() {
        // context load smoke test
    }
}
