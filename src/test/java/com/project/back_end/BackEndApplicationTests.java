package com.project.back_end;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestBeansConfig.class)
class BackEndApplicationTests {

	@Test
	void contextLoads() {
	}

}

