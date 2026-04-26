package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolView;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ToolViewRepository extends JpaRepository<ToolView, Long> {
    long countByTool(Tool tool);
    List<ToolView> findByToolAndUserIsNotNull(Tool tool);

    @Query("SELECT DISTINCT v.user FROM ToolView v WHERE v.tool = :tool AND v.user IS NOT NULL")
    List<AppUser> findDistinctViewersByTool(@Param("tool") Tool tool);

    @Query("SELECT v FROM ToolView v WHERE v.tool = :tool AND v.viewedAt >= :since ORDER BY v.viewedAt ASC") //it should be created
    List<ToolView> findByToolSince(@Param("tool") Tool tool, @Param("since") LocalDateTime since);
}
