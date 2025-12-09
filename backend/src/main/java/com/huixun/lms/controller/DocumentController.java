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

    static class UploadResponse { public Long documentId; UploadResponse(Long id){this.documentId=id;} }
    static class StatusResponse { public String status; public String error; StatusResponse(String s,String e){status=s;error=e;} }
}
