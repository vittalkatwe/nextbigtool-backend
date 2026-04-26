package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.newsletter.NewsletterSubscriber;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    List<NewsletterSubscriber> findByPublisherAndIsActiveTrue(AppUser publisher);
    long countByPublisherAndIsActiveTrue(AppUser publisher);
    Optional<NewsletterSubscriber> findByPublisherAndSubscriber(AppUser publisher, AppUser subscriber);
    boolean existsByPublisherAndSubscriberAndIsActiveTrue(AppUser publisher, AppUser subscriber);
}
