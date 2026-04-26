package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.tool.CommentLike;
import com.nextbigtool.backend.entity.tool.ToolComment;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndUser(ToolComment comment, AppUser user);
    long countByComment(ToolComment comment);
    boolean existsByCommentAndUser(ToolComment comment, AppUser user);
}
