package com.huixun.lms.repository;

import com.huixun.lms.model.ContentFragment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentFragmentRepository extends JpaRepository<ContentFragment, Long> {
    List<ContentFragment> findByDocumentId(Long documentId);
}
