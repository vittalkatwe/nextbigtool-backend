package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.bip.BuildInPublicPost;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BuildInPublicPostRepository extends JpaRepository<BuildInPublicPost, Long> {
    Page<BuildInPublicPost> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<BuildInPublicPost> findByUserOrderByCreatedAtDesc(AppUser user);
    long countByUser(AppUser user);
    long countByUserAndCreatedAtAfter(AppUser user, LocalDateTime after);
}
