package com.teachengineering.springai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "KILO_API_KEY=test",
    "KILO_BASE_URL=https://test.example.com",
    "KILO_MODEL=test-model"
})
class SpringAiApplicationTests {

    @Test
    void contextLoads() {
    }

}
