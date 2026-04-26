package com.nextbigtool.backend.controller.profile;

import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.repository.UserRepository;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ProfileController {

    @Autowired private CurrentUserService currentUserService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile() {
        AppUser user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(Map.of("success", true, "data", toDto(user)));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body) {
        AppUser user = currentUserService.getCurrentUser();

        if (body.containsKey("firstname")) user.setFirstname((String) body.get("firstname"));
        if (body.containsKey("lastname"))  user.setLastname((String) body.get("lastname"));
        if (body.containsKey("bio"))       user.setBio((String) body.get("bio"));
        if (body.containsKey("website"))   user.setWebsite((String) body.get("website"));
        if (body.containsKey("avatarUrl")) user.setAvatarUrl((String) body.get("avatarUrl"));
        if (body.containsKey("twitterHandle")) user.setTwitterHandle((String) body.get("twitterHandle"));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "data", toDto(user)));
    }

    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSettings() {
        AppUser user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "notifyOnUpvote",  user.getNotifyOnUpvote(),
                "notifyOnComment", user.getNotifyOnComment(),
                "notifyOnMessage", user.getNotifyOnMessage(),
                "profilePublic",   user.getProfilePublic()
        )));
    }

    @PutMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> body) {
        AppUser user = currentUserService.getCurrentUser();

        if (body.containsKey("notifyOnUpvote"))  user.setNotifyOnUpvote((Boolean) body.get("notifyOnUpvote"));
        if (body.containsKey("notifyOnComment")) user.setNotifyOnComment((Boolean) body.get("notifyOnComment"));
        if (body.containsKey("notifyOnMessage")) user.setNotifyOnMessage((Boolean) body.get("notifyOnMessage"));
        if (body.containsKey("profilePublic"))   user.setProfilePublic((Boolean) body.get("profilePublic"));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Settings saved"));
    }

    private Map<String, Object> toDto(AppUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("email", u.getEmail());
        m.put("firstname", u.getFirstname() != null ? u.getFirstname() : "");
        m.put("lastname",  u.getLastname()  != null ? u.getLastname()  : "");
        m.put("bio",       u.getBio()        != null ? u.getBio()        : "");
        m.put("website",   u.getWebsite()    != null ? u.getWebsite()    : "");
        m.put("avatarUrl", u.getAvatarUrl()  != null ? u.getAvatarUrl()  : "");
        m.put("twitterHandle", u.getTwitterHandle() != null ? u.getTwitterHandle() : "");
        m.put("emailVerified", u.getEmailVerified());
        m.put("createdAt", u.getCreatedAt());
        return m;
    }
}
