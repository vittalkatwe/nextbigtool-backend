package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.blog.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findAllByOrderByUpdatedAtDesc();
    Optional<Blog> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
