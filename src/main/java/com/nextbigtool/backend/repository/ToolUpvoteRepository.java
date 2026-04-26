package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolUpvote;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolUpvoteRepository extends JpaRepository<ToolUpvote, Long> {
    Optional<ToolUpvote> findByToolAndUser(Tool tool, AppUser user);
    List<ToolUpvote> findByTool(Tool tool);
    long countByTool(Tool tool);
    boolean existsByToolAndUser(Tool tool, AppUser user);
}
