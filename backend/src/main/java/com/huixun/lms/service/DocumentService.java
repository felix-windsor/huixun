package com.huixun.lms.service;

import com.huixun.lms.model.Document;
import com.huixun.lms.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DocumentService {

    @Value("${storage.docs:storage/docs}")
    private String storageDir;

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document saveUploaded(String originalFilename, byte[] content) throws IOException {
        Path dir = Paths.get(storageDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path target = dir.resolve(System.currentTimeMillis() + "_" + originalFilename);
        Files.write(target, content);
        Document doc = new Document();
        doc.setFilename(originalFilename);
        doc.setStoragePath(target.toString());
        doc.setStatus("UPLOADED");
        return documentRepository.save(doc);
    }
}
