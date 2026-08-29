package com.nextenti.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main entry point for the Nextenti Spring Boot application.
 *
 * @author Vishal
 * @version 1.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.nextenti.services.domain.repository")
@EnableJpaAuditing
@EnableCaching
public class BackendApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BackendApplication.class);

    private static final String LINE = "-------------------------------------------------------";
    private static final String EMPTY_LINE = "|                                                     |";

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /**
     * Application entry point — bootstraps the Spring context and logs startup banners.
     *
     * @param args command-line arguments passed to the JVM at startup
     */
    public static void main(String[] args) {

        logger.info(LINE);
        logger.info(EMPTY_LINE);
        logger.info(EMPTY_LINE);
        logger.info("|           APPLICATION START INITIATED               |");
        logger.info(EMPTY_LINE);
        logger.info(EMPTY_LINE);
        logger.info(LINE);

        SpringApplication.run(BackendApplication.class, args);

        logger.info(LINE);
        logger.info(EMPTY_LINE);
        logger.info(EMPTY_LINE);
        logger.info("|               APPLICATION STARTED                   |");
        logger.info(EMPTY_LINE);
        logger.info(EMPTY_LINE);
        logger.info(LINE);

    }

    /**
     * Called after the Spring context is fully loaded. No startup logic is required currently.
     *
     * @param args command-line arguments passed at startup
     */
    @Override
    public void run(String... args) {
        // no startup logic required
    }

}
