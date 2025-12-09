package com.huixun.lms.controller;

import com.huixun.lms.model.Course;
import com.huixun.lms.repository.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public List<Course> list() { return courseRepository.findAll(); }

    @PostMapping
    public Course create(@RequestBody Course c) { return courseRepository.save(c); }

    @PutMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable("id") Long id, @RequestBody Course c) {
        return courseRepository.findById(id).map(x -> {
            x.setName(c.getName());
            x.setDescription(c.getDescription());
            return ResponseEntity.ok(courseRepository.save(x));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        return courseRepository.findById(id).map(x -> { courseRepository.delete(x); return ResponseEntity.ok().build(); }).orElse(ResponseEntity.notFound().build());
    }
}
