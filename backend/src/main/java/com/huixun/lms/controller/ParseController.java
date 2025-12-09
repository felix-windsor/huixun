package com.huixun.lms.controller;

import com.huixun.lms.service.EmbeddingService;
import com.huixun.lms.service.PdfParseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parse")
public class ParseController {

    private final PdfParseService pdfParseService;
    private final EmbeddingService embeddingService;

    public ParseController(PdfParseService pdfParseService, EmbeddingService embeddingService) {
        this.pdfParseService = pdfParseService;
        this.embeddingService = embeddingService;
    }

    @PostMapping("/{documentId}")
    public ResponseEntity<?> start(@PathVariable("documentId") Long documentId) {
        try {
            pdfParseService.parse(documentId);
            embeddingService.embedDocument(documentId);
            return ResponseEntity.ok("done");
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("解析失败");
        }
    }
}
