package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.tool.Sponsor;
import com.nextbigtool.backend.entity.tool.SponsorStatus;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, Long> {
    List<Sponsor> findBySubmittedBy(AppUser user);
    List<Sponsor> findByStatus(SponsorStatus status);
}