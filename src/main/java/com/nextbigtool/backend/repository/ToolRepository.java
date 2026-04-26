package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolStatus;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {
    List<Tool> findBySubmittedBy(AppUser user);
    List<Tool> findByStatus(ToolStatus status);
    List<Tool> findBySubmittedByAndStatus(AppUser user, ToolStatus status);
    long countBySubmittedBy(AppUser user);

    @Query("SELECT t FROM Tool t WHERE t.status = 'APPROVED' AND t.featuredUntil > :now ORDER BY t.featuredUntil DESC")
    List<Tool> findFeaturedTools(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Tool t WHERE t.status = 'APPROVED' ORDER BY (SELECT COUNT(u) FROM ToolUpvote u WHERE u.tool = t) DESC")
    List<Tool> findHallOfFameTools(Pageable pageable);
}
