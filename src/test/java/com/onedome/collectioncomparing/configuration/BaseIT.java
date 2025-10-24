package com.onedome.collectioncomparing.configuration;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = PostgresTestContainerConfiguration.class
)
@Execution(ExecutionMode.CONCURRENT)
public abstract class BaseIT {

}
