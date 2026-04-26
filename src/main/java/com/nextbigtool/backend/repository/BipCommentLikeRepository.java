package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.bip.BipComment;
import com.nextbigtool.backend.entity.bip.BipCommentLike;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BipCommentLikeRepository extends JpaRepository<BipCommentLike, Long> {
    Optional<BipCommentLike> findByCommentAndUser(BipComment comment, AppUser user);
    long countByComment(BipComment comment);
    boolean existsByCommentAndUser(BipComment comment, AppUser user);
}
