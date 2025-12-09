package com.huixun.lms.controller;

import com.huixun.lms.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/fragments")
    public ResponseEntity<?> fragments(@RequestParam String q, @RequestParam(defaultValue = "10") int topK) {
        return ResponseEntity.ok(searchService.topK(q, topK));
    }
}
