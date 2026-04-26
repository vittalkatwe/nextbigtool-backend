package com.nextbigtool.backend.controller.blog;

import com.nextbigtool.backend.entity.blog.Blog;
import com.nextbigtool.backend.repository.BlogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/blogs")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class BlogController {

    @Autowired
    private BlogRepository blogRepository;

    // ── Public endpoints ─────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> listBlogs() {
        List<Map<String, Object>> blogs = blogRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(b -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", b.getId());
                    m.put("title", b.getTitle());
                    m.put("slug", b.getSlug());
                    m.put("topImageUrl", b.getTopImageUrl());
                    m.put("category", b.getCategory());
                    m.put("updatedAt", b.getUpdatedAt());
                    m.put("createdAt", b.getCreatedAt());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", blogs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBlog(@PathVariable Long id) {
        return blogRepository.findById(id)
                .map(b -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", b.getId());
                    m.put("title", b.getTitle());
                    m.put("slug", b.getSlug());
                    m.put("topImageUrl", b.getTopImageUrl());
                    m.put("content", b.getContent());
                    m.put("category", b.getCategory());
                    m.put("updatedAt", b.getUpdatedAt());
                    m.put("createdAt", b.getCreatedAt());
                    return ResponseEntity.ok(Map.of("success", true, "data", m));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "error", "Blog not found")));
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBlog(@RequestBody Map<String, String> body) {
        try {
            String title = body.get("title");
            if (title == null || title.isBlank())
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Title is required"));

            Blog blog = new Blog();
            blog.setTitle(title.trim());
            blog.setTopImageUrl(body.get("topImageUrl"));
            blog.setContent(body.get("content"));
            blog.setCategory(body.get("category"));
            blog.setSlug(uniqueSlug(title.trim()));

            Blog saved = blogRepository.save(blog);
            return ResponseEntity.ok(Map.of("success", true, "data", Map.of("id", saved.getId(), "slug", saved.getSlug())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBlog(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return blogRepository.findById(id).map(blog -> {
            if (body.containsKey("title") && !body.get("title").isBlank()) {
                blog.setTitle(body.get("title").trim());
            }
            if (body.containsKey("topImageUrl")) blog.setTopImageUrl(body.get("topImageUrl"));
            if (body.containsKey("content")) blog.setContent(body.get("content"));
            if (body.containsKey("category")) blog.setCategory(body.get("category"));
            blogRepository.save(blog);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse((ResponseEntity<Map<String, Boolean>>) ResponseEntity.status(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id) {
        if (!blogRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Blog not found"));
        blogRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String slugify(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        return normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
    }

    private String uniqueSlug(String title) {
        String base = slugify(title);
        String candidate = base;
        int suffix = 1;
        while (blogRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}
