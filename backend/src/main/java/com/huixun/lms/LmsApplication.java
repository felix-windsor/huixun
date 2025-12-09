package com.huixun.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class LmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LmsApplication.class, args);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner migrateColumns(JdbcTemplate jdbcTemplate) {
        return args -> {
            try { jdbcTemplate.execute("ALTER TABLE content_fragments ALTER COLUMN text TYPE text"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE content_fragments ALTER COLUMN keywords TYPE text"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE content_fragments ALTER COLUMN embedding_json TYPE text"); } catch (Exception ignored) {}
        };
    }
}
