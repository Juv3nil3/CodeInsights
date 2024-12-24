package com.juv3nil3.icdg;

import com.juv3nil3.icdg.config.AsyncSyncConfiguration;
import com.juv3nil3.icdg.config.EmbeddedElasticsearch;
import com.juv3nil3.icdg.config.EmbeddedSQL;
import com.juv3nil3.icdg.config.JacksonConfiguration;
import com.juv3nil3.icdg.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = { DocumentationGeneratorApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class }
)
@EmbeddedElasticsearch
@EmbeddedSQL
public @interface IntegrationTest {
}
