package com.nextbigtool.backend.service.tool;

import com.nextbigtool.backend.entity.tool.*;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.PlanType;
import com.nextbigtool.backend.entity.user.Subscription;
import com.nextbigtool.backend.model.tool.ToolResponseDto;
import com.nextbigtool.backend.model.tool.ToolSubmitRequestDto;
import com.nextbigtool.backend.repository.ToolCommentRepository;
import com.nextbigtool.backend.repository.ToolRepository;
import com.nextbigtool.backend.repository.ToolUpvoteRepository;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import com.nextbigtool.backend.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ToolService {

    @Autowired private ToolRepository toolRepository;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private ToolUpvoteRepository upvoteRepository;
    @Autowired private ToolCommentRepository commentRepository;
    @Autowired private SubscriptionService subscriptionService;

    @Transactional
    public ResponseEntity<?> submitTool(ToolSubmitRequestDto request) {
        try {
            AppUser user = currentUserService.getCurrentUser();

            // Check plan limits
            Subscription sub = subscriptionService.getOrCreateFree(user);
            PlanType plan = sub.getEffectivePlan();
            long toolCount = toolRepository.countBySubmittedBy(user);

            int limit = switch (plan) {
                case FREE -> 1;
                case BASIC -> 5;
                case CORE -> Integer.MAX_VALUE;
            };
            if (toolCount >= limit) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of(
                        "success", false,
                        "error", plan == PlanType.FREE
                                ? "Free plan allows 1 tool listing. Upgrade to Basic for 5 listings."
                                : "Upgrade to Core for unlimited listings.",
                        "limitReached", true
                ));
            }

            Tool tool = new Tool();
            tool.setSubmittedBy(user);
            tool.setProductName(request.getProductName().trim());
            tool.setWebsiteUrl(request.getWebsiteUrl().trim());
            tool.setLogoUrl(request.getLogoUrl());
            tool.setTagline(request.getTagline());
            tool.setDescription(request.getDescription());
            tool.setVideoUrl(request.getVideoUrl());

            if (request.getScreenshots() != null) {
                tool.setScreenshots(request.getScreenshots().stream()
                        .filter(s -> s != null && !s.isBlank()).limit(10).collect(Collectors.toList()));
            }

            tool.setCategory(request.getCategory());
            tool.setTargetAudiences(request.getTargetAudiences());
            tool.setUseCases(request.getUseCases());
            tool.setPlatforms(request.getPlatforms());
            tool.setMrr(request.getMrr());
            tool.setArr(request.getArr());
            tool.setDiscountCode(request.getDiscountCode());
            tool.setStatus(ToolStatus.APPROVED);

            if (request.getAlternatives() != null) {
                tool.setAlternatives(request.getAlternatives().stream()
                        .filter(a -> a.getName() != null && !a.getName().isBlank())
                        .map(a -> { ToolAlternative alt = new ToolAlternative(); alt.setName(a.getName().trim()); alt.setUrl(a.getUrl()); return alt; })
                        .collect(Collectors.toList()));
            }

            Tool saved = toolRepository.save(tool);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Tool submitted and is under review",
                    "data", enrichDto(saved)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false, "error", "Failed to submit tool: " + e.getMessage()
            ));
        }
    }

    public ResponseEntity<?> getMyTools() {
        AppUser user = currentUserService.getCurrentUser();
        List<ToolResponseDto> tools = toolRepository.findBySubmittedBy(user)
                .stream().map(this::enrichDto).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", tools));
    }

    public ResponseEntity<?> getApprovedTools() {
        List<ToolResponseDto> tools = toolRepository.findByStatus(ToolStatus.APPROVED)
                .stream().map(this::enrichDto).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", tools));
    }

    public ResponseEntity<?> getFeaturedTools() {
        List<ToolResponseDto> tools = toolRepository.findFeaturedTools(LocalDateTime.now())
                .stream().map(this::enrichDto).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", tools));
    }

    public ResponseEntity<?> getToolById(Long id) {
        try {
            Tool tool = toolRepository.findById(id).orElseThrow(() -> new RuntimeException("Tool not found"));
            return ResponseEntity.ok(Map.of("success", true, "data", enrichDto(tool)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public void setFeatured(Tool tool, AppUser submitter) {
        Subscription sub = subscriptionService.getOrCreateFree(submitter);
        PlanType plan = sub.getEffectivePlan();
        if (plan == PlanType.BASIC || plan == PlanType.CORE) {
            tool.setFeaturedUntil(LocalDateTime.now().plusHours(48));
            toolRepository.save(tool);
        }
    }

    private ToolResponseDto enrichDto(Tool tool) {
        ToolResponseDto dto = ToolResponseDto.from(tool);
        dto.setUpvoteCount(upvoteRepository.countByTool(tool));
        dto.setCommentCount(commentRepository.countByToolAndDeletedFalse(tool));
        return dto;
    }
}
