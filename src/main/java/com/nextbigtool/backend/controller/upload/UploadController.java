package com.nextbigtool.backend.controller.upload;

import com.nextbigtool.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class UploadController {

    @Autowired
    private S3Service s3Service;

    @PostMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "File must be an image"));
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Image must be under 5MB"));
            }
            String url = s3Service.uploadFile(file, "tools/screenshots");
            return ResponseEntity.ok(Map.of("success", true, "url", url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/video")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "File must be a video"));
            }
            if (file.getSize() > 100 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Video must be under 100MB"));
            }
            String url = s3Service.uploadFile(file, "tools/videos");
            return ResponseEntity.ok(Map.of("success", true, "url", url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Upload failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "File must be an image"));
            }
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Logo must be under 2MB"));
            }
            String url = s3Service.uploadFile(file, "tools/logos");
            return ResponseEntity.ok(Map.of("success", true, "url", url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Upload failed: " + e.getMessage()));
        }
    }
}
