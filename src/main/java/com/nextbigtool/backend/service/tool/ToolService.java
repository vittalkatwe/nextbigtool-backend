package com.nextbigtool.backend.service.tool;

import com.nextbigtool.backend.entity.tool.*;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.model.tool.ToolResponseDto;
import com.nextbigtool.backend.model.tool.ToolSubmitRequestDto;
import com.nextbigtool.backend.repository.ToolRepository;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ToolService {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Submit a new tool listing
     */
    @Transactional
    public ResponseEntity<?> submitTool(ToolSubmitRequestDto request) {
        try {
            AppUser user = currentUserService.getCurrentUser();

            Tool tool = new Tool();
            tool.setSubmittedBy(user);
            tool.setProductName(request.getProductName().trim());
            tool.setWebsiteUrl(request.getWebsiteUrl().trim());
            tool.setLogoUrl(request.getLogoUrl());
            tool.setTagline(request.getTagline());
            tool.setDescription(request.getDescription());
            tool.setScreenshotUrl(request.getScreenshotUrl());
            tool.setCategory(request.getCategory());
            tool.setTargetAudiences(request.getTargetAudiences());
            tool.setUseCases(request.getUseCases());
            tool.setPlatforms(request.getPlatforms());
            tool.setMrr(request.getMrr());
            tool.setArr(request.getArr());
            tool.setDiscountCode(request.getDiscountCode());
            tool.setStatus(ToolStatus.PENDING);

            // Map alternatives
            if (request.getAlternatives() != null) {
                List<ToolAlternative> alts = request.getAlternatives().stream()
                        .filter(a -> a.getName() != null && !a.getName().isBlank())
                        .map(a -> {
                            ToolAlternative alt = new ToolAlternative();
                            alt.setName(a.getName().trim());
                            alt.setUrl(a.getUrl());
                            return alt;
                        })
                        .collect(Collectors.toList());
                tool.setAlternatives(alts);
            }

            Tool saved = toolRepository.save(tool);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Tool submitted successfully and is under review",
                    "data", ToolResponseDto.from(saved)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Failed to submit tool: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all tools submitted by the current user
     */
    public ResponseEntity<?> getMyTools() {
        try {
            AppUser user = currentUserService.getCurrentUser();
            List<ToolResponseDto> tools = toolRepository.findBySubmittedBy(user)
                    .stream()
                    .map(ToolResponseDto::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", tools));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get all approved tools (public listing)
     */
    public ResponseEntity<?> getApprovedTools() {
        try {
            List<ToolResponseDto> tools = toolRepository.findByStatus(ToolStatus.APPROVED)
                    .stream()
                    .map(ToolResponseDto::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", tools));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Get a single tool by ID
     */
    public ResponseEntity<?> getToolById(Long id) {
        try {
            Tool tool = toolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));

            return ResponseEntity.ok(Map.of("success", true, "data", ToolResponseDto.from(tool)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}