package com.nextbigtool.backend.service.newsletter;

import com.nextbigtool.backend.entity.newsletter.NewsletterIssue;
import com.nextbigtool.backend.entity.newsletter.NewsletterSubscriber;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.repository.*;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class NewsletterService {

    @Autowired private NewsletterSubscriberRepository subscriberRepository;
    @Autowired private NewsletterIssueRepository issueRepository;
    @Autowired private ToolRepository toolRepository;
    @Autowired private ToolCommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CurrentUserService currentUserService;

    public ResponseEntity<?> getMyStats() {
        AppUser user = currentUserService.getCurrentUser();

        long subscriberCount = subscriberRepository.countByPublisherAndIsActiveTrue(user);
        long issueCount = issueRepository.countByPublisher(user);

        List<NewsletterIssue> issues = issueRepository.findByPublisherOrderBySentAtDesc(user);
        double avgOpenRate = issues.stream()
                .filter(i -> i.getRecipientCount() > 0)
                .mapToDouble(i -> (double) i.getOpenCount() / i.getRecipientCount())
                .average()
                .orElse(0.0);

        // Total mentions = total comments on all their tools
        long totalMentions = toolRepository.findBySubmittedBy(user).stream()
                .mapToLong(t -> commentRepository.countByToolAndDeletedFalse(t))
                .sum();

        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "subscriberCount", subscriberCount,
                "issueCount", issueCount,
                "avgOpenRate", Math.round(avgOpenRate * 1000.0) / 10.0, // percentage with 1 decimal
                "totalMentions", totalMentions
        )));
    }

    public ResponseEntity<?> getMySubscribers() {
        AppUser user = currentUserService.getCurrentUser();
        List<Map<String, Object>> subs = subscriberRepository.findByPublisherAndIsActiveTrue(user)
                .stream()
                .map(s -> Map.of(
                        "id", (Object) s.getId(),
                        "email", s.getSubscriber().getEmail(),
                        "name", s.getSubscriber().getFirstname() != null ? s.getSubscriber().getFirstname() : "",
                        "subscribedAt", s.getSubscribedAt().toString()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", subs));
    }

    public ResponseEntity<?> getMyIssues() {
        AppUser user = currentUserService.getCurrentUser();
        List<Map<String, Object>> issues = issueRepository.findByPublisherOrderBySentAtDesc(user)
                .stream()
                .map(i -> Map.of(
                        "id", (Object) i.getId(),
                        "subject", i.getSubject(),
                        "content", i.getContent(),
                        "sentAt", i.getSentAt().toString(),
                        "recipientCount", i.getRecipientCount(),
                        "openCount", i.getOpenCount(),
                        "openRate", i.getRecipientCount() > 0
                                ? Math.round((double) i.getOpenCount() / i.getRecipientCount() * 1000.0) / 10.0
                                : 0.0
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", issues));
    }

    @Transactional
    public ResponseEntity<?> publishIssue(String subject, String content) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            if (subject == null || subject.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Subject is required"));
            }
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Content is required"));
            }

            List<NewsletterSubscriber> subs = subscriberRepository.findByPublisherAndIsActiveTrue(user);
            int recipientCount = subs.size();

            NewsletterIssue issue = new NewsletterIssue();
            issue.setPublisher(user);
            issue.setSubject(subject.trim());
            issue.setContent(content.trim());
            issue.setRecipientCount(recipientCount);
            issueRepository.save(issue);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Issue published and sent to " + recipientCount + " subscribers",
                    "recipientCount", recipientCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> trackOpen(Long issueId) {
        try {
            NewsletterIssue issue = issueRepository.findById(issueId)
                    .orElseThrow(() -> new RuntimeException("Issue not found"));
            issue.setOpenCount(issue.getOpenCount() + 1);
            issueRepository.save(issue);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false));
        }
    }

    @Transactional
    public ResponseEntity<?> subscribeToUser(Long publisherId) {
        try {
            AppUser subscriber = currentUserService.getCurrentUser();
            AppUser publisher = userRepository.findById(publisherId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (publisher.getId().equals(subscriber.getId())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Cannot subscribe to yourself"));
            }

            var existing = subscriberRepository.findByPublisherAndSubscriber(publisher, subscriber);
            if (existing.isPresent()) {
                NewsletterSubscriber sub = existing.get();
                sub.setIsActive(!sub.getIsActive());
                subscriberRepository.save(sub);
                return ResponseEntity.ok(Map.of("success", true, "subscribed", sub.getIsActive()));
            }

            NewsletterSubscriber sub = new NewsletterSubscriber();
            sub.setPublisher(publisher);
            sub.setSubscriber(subscriber);
            subscriberRepository.save(sub);
            return ResponseEntity.ok(Map.of("success", true, "subscribed", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
