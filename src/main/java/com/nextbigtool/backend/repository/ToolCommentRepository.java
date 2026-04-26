package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolCommentRepository extends JpaRepository<ToolComment, Long> {
    List<ToolComment> findByToolAndDeletedFalseOrderByCreatedAtAsc(Tool tool);
    long countByToolAndDeletedFalse(Tool tool);
}
