package com.huixun.lms.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "content_fragments")
public class ContentFragment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(length = 4000)
    private String text;

    private String sectionPath;

    private String pageRange;

    @Column(length = 4000)
    private String keywords;

    @Column(length = 4000)
    private String embeddingJson; // 回退存储，pgvector 启用后替换

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSectionPath() { return sectionPath; }
    public void setSectionPath(String sectionPath) { this.sectionPath = sectionPath; }
    public String getPageRange() { return pageRange; }
    public void setPageRange(String pageRange) { this.pageRange = pageRange; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getEmbeddingJson() { return embeddingJson; }
    public void setEmbeddingJson(String embeddingJson) { this.embeddingJson = embeddingJson; }
}
