package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolStatus;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {
    List<Tool> findBySubmittedBy(AppUser user);
    List<Tool> findByStatus(ToolStatus status);
    List<Tool> findBySubmittedByAndStatus(AppUser user, ToolStatus status);
}