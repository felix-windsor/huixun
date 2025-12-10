package com.huixun.lms.controller;

import com.huixun.lms.model.Document;
import com.huixun.lms.repository.ContentFragmentRepository;
import com.huixun.lms.repository.DocumentRepository;
import com.huixun.lms.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final ContentFragmentRepository fragmentRepository;

    public DocumentController(DocumentService documentService,
                              DocumentRepository documentRepository,
                              ContentFragmentRepository fragmentRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
        this.fragmentRepository = fragmentRepository;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("仅支持 PDF 文件");
        }
        Document doc = documentService.saveUploaded(file.getOriginalFilename(), file.getBytes());
        return ResponseEntity.ok(new UploadResponse(doc.getId()));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> status(@PathVariable("id") Long id) {
        try {
            System.out.println("status called for id=" + id);
            return documentRepository.findById(id)
                    .map(d -> ResponseEntity.ok(new StatusResponse(d.getStatus(), d.getErrorMessage())))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("状态查询失败");
        }
    }

    @GetMapping("/{id}/fragments")
    public ResponseEntity<?> fragments(@PathVariable("id") Long id) {
        try {
            System.out.println("fragments called for id=" + id);
            return ResponseEntity.ok(fragmentRepository.findByDocumentId(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("片段查询失败");
        }
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        try {
            return ResponseEntity.ok(documentRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("查询文档列表失败");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Long id) {
        return documentRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody UpdateRequest req) {
        return documentRepository.findById(id).map(d -> {
            if (req.filename != null && !req.filename.isBlank()) d.setFilename(req.filename);
            if (req.status != null && !req.status.isBlank()) d.setStatus(req.status);
            d.setUpdatedAt(java.time.Instant.now());
            return ResponseEntity.ok(documentRepository.save(d));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        return documentRepository.findById(id).map(d -> {
            try {
                java.util.List<com.huixun.lms.model.ContentFragment> fr = fragmentRepository.findByDocumentId(d.getId());
                if (!fr.isEmpty()) fragmentRepository.deleteAll(fr);
                if (d.getStoragePath() != null) {
                    try { java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(d.getStoragePath())); } catch (Exception ignored) {}
                }
                documentRepository.delete(d);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(500).body("删除失败");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    static class UploadResponse { public Long documentId; UploadResponse(Long id){this.documentId=id;} }
    static class StatusResponse { public String status; public String error; StatusResponse(String s,String e){status=s;error=e;} }
    static class UpdateRequest { public String filename; public String status; }
}
