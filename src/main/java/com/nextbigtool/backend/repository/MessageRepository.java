package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.messaging.Message;
import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByToUserOrderBySentAtDesc(AppUser toUser);
    boolean existsByFromUserAndToUserAndTool(AppUser fromUser, AppUser toUser, Tool tool);
    Optional<Message> findByFromUserAndToUserAndTool(AppUser fromUser, AppUser toUser, Tool tool);
}
