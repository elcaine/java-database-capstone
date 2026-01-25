package com.project.back_end;

import com.project.back_end.repo.PrescriptionRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test-only beans so Spring can start the application context without Mongo.
 */
@TestConfiguration
public class TestBeansConfig {

    @Bean
    public PrescriptionRepository prescriptionRepository() {
        return Mockito.mock(PrescriptionRepository.class);
    }
}
