package com.nextbigtool.backend.controller.admin;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolStatus;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.model.tool.ToolResponseDto;
import com.nextbigtool.backend.repository.ToolRepository;
import com.nextbigtool.backend.repository.UserRepository;
import com.nextbigtool.backend.service.tool.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private ToolRepository toolRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ToolService toolService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        long totalUsers = userRepository.count();
        long totalTools = toolRepository.count();
        long pendingTools = toolRepository.findByStatus(ToolStatus.PENDING).size();
        long approvedTools = toolRepository.findByStatus(ToolStatus.APPROVED).size();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "totalUsers", totalUsers,
                        "totalTools", totalTools,
                        "pendingTools", pendingTools,
                        "approvedTools", approvedTools
                )
        ));
    }

    @GetMapping("/tools")
    public ResponseEntity<?> getTools(@RequestParam(required = false) String status) {
        try {
            List<Tool> tools;
            if (status != null) {
                tools = toolRepository.findByStatus(ToolStatus.valueOf(status.toUpperCase()));
            } else {
                tools = toolRepository.findAll();
            }
            List<ToolResponseDto> dtos = tools.stream().map(ToolResponseDto::from).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("success", true, "data", dtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/tools/{id}/status")
    public ResponseEntity<?> updateToolStatus(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        try {
            Tool tool = toolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));
            ToolStatus newStatus = ToolStatus.valueOf(body.get("status").toUpperCase());
            tool.setStatus(newStatus);
            toolRepository.save(tool);

            if (newStatus == ToolStatus.APPROVED) {
                AppUser submitter = tool.getSubmittedBy();
                toolService.setFeatured(tool, submitter);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Status updated to " + newStatus));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<AppUser> users = userRepository.findAll();
        List<Map<String, Object>> data = users.stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "email", u.getEmail(),
                "role", u.getRole().name(),
                "emailVerified", u.getEmailVerified(),
                "createdAt", u.getCreatedAt()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
