package com.huixun.lms.service;

import com.huixun.lms.model.ContentFragment;
import com.huixun.lms.model.Document;
import com.huixun.lms.repository.ContentFragmentRepository;
import com.huixun.lms.repository.DocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfParseService {

    private final DocumentRepository documentRepository;
    private final ContentFragmentRepository fragmentRepository;

    public PdfParseService(DocumentRepository documentRepository,
                           ContentFragmentRepository fragmentRepository) {
        this.documentRepository = documentRepository;
        this.fragmentRepository = fragmentRepository;
    }

    public void parse(Long documentId) throws IOException {
        Document doc = documentRepository.findById(documentId).orElseThrow();
        doc.setStatus("PARSING");
        documentRepository.save(doc);

        File file = new File(doc.getStoragePath());
        try (PDDocument pdf = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);
            List<String> chunks = chunk(text);
            int pageCount = pdf.getNumberOfPages();
            List<ContentFragment> fragments = new ArrayList<>();
            for (String c : chunks) {
                ContentFragment f = new ContentFragment();
                f.setDocumentId(documentId);
                f.setText(c);
                f.setPageRange("1-" + pageCount);
                fragments.add(f);
            }
            fragmentRepository.saveAll(fragments);
            doc.setStatus("EMBEDDING");
            doc.setErrorMessage(null);
            documentRepository.save(doc);
        } catch (Exception e) {
            doc.setStatus("FAILED");
            doc.setErrorMessage(e.getMessage());
            documentRepository.save(doc);
            throw e instanceof IOException ? (IOException) e : new IOException(e);
        }
    }

    private List<String> chunk(String text) {
        if (text == null) return java.util.Collections.emptyList();
        String normalized = text.replaceAll("\r", "\n").replaceAll("\n{2,}", "\n\n");
        String[] paras = normalized.split("\n\n");
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int limit = 1200;
        for (String p : paras) {
            if (cur.length() + p.length() + 2 > limit) {
                out.add(cur.toString());
                cur = new StringBuilder();
            }
            if (p != null && !p.trim().isEmpty()) {
                if (cur.length() > 0) cur.append("\n\n");
                cur.append(p);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
}
