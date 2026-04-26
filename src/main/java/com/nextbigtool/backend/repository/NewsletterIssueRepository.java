package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.newsletter.NewsletterIssue;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsletterIssueRepository extends JpaRepository<NewsletterIssue, Long> {
    List<NewsletterIssue> findByPublisherOrderBySentAtDesc(AppUser publisher);
    long countByPublisher(AppUser publisher);
}
