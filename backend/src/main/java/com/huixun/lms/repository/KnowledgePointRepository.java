package com.huixun.lms.repository;

import com.huixun.lms.model.KnowledgePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long> {
    Optional<KnowledgePoint> findByName(String name);
}
