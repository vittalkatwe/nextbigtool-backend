package com.nextbigtool.backend.controller.messaging;

import com.nextbigtool.backend.entity.messaging.Message;
import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.PlanType;
import com.nextbigtool.backend.repository.MessageRepository;
import com.nextbigtool.backend.repository.ToolRepository;
import com.nextbigtool.backend.repository.UserRepository;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import com.nextbigtool.backend.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/messages")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class MessageController {

    @Autowired private MessageRepository messageRepository;
    @Autowired private ToolRepository toolRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private SubscriptionService subscriptionService;

    @PostMapping("/tool/{toolId}/user/{toUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendFollowUp(@PathVariable Long toolId,
                                          @PathVariable Long toUserId,
                                          @RequestBody Map<String, String> body) {
        try {
            AppUser sender = currentUserService.getCurrentUser();
            var sub = subscriptionService.getOrCreateFree(sender);
            if (sub.getEffectivePlan() != PlanType.CORE) {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(Map.of("success", false, "error", "Follow-up messages require Core plan"));
            }

            Tool tool = toolRepository.findById(toolId)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));
            if (!tool.getSubmittedBy().getId().equals(sender.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "error", "Not your tool"));
            }

            AppUser toUser = userRepository.findById(toUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (messageRepository.existsByFromUserAndToUserAndTool(sender, toUser, tool)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false, "error", "You already sent a message to this user for this tool"));
            }

            String content = body.get("content");
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Message cannot be empty"));
            }

            Message msg = new Message();
            msg.setFromUser(sender);
            msg.setToUser(toUser);
            msg.setTool(tool);
            msg.setContent(content.trim());
            messageRepository.save(msg);

            return ResponseEntity.ok(Map.of("success", true, "message", "Message sent"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getInbox() {
        AppUser user = currentUserService.getCurrentUser();
        List<Message> messages = messageRepository.findByToUserOrderBySentAtDesc(user);
        List<Map<String, Object>> dtos = messages.stream().map(m -> Map.of(
                "id", (Object) m.getId(),
                "fromEmail", m.getFromUser().getEmail(),
                "fromName", m.getFromUser().getFirstname() != null ? m.getFromUser().getFirstname() : "",
                "toolName", m.getTool() != null ? m.getTool().getProductName() : "",
                "content", m.getContent(),
                "isRead", m.getIsRead(),
                "sentAt", m.getSentAt().toString()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", dtos));
    }
}
