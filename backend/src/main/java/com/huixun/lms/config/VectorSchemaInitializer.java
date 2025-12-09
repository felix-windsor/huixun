package com.huixun.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class VectorSchemaInitializer {

    public VectorSchemaInitializer(JdbcTemplate jdbc, @Value("${app.vector.enabled:true}") boolean enabled) {
        if (!enabled) return;
        try { jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector"); } catch (Exception ignored) {}
        try { jdbc.execute("ALTER TABLE content_fragments ADD COLUMN IF NOT EXISTS embedding vector(1536)"); } catch (Exception ignored) {}
    }
}
