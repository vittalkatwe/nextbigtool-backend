package com.nextbigtool.backend.controller.tool;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.model.tool.ToolResponseDto;
import com.nextbigtool.backend.repository.ToolRepository;
import com.nextbigtool.backend.repository.ToolUpvoteRepository;
import com.nextbigtool.backend.repository.ToolCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/hall-of-fame")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class HallOfFameController {

    @Autowired private ToolRepository toolRepository;
    @Autowired private ToolUpvoteRepository upvoteRepository;
    @Autowired private ToolCommentRepository commentRepository;

    @GetMapping
    public ResponseEntity<?> getHallOfFame(@RequestParam(defaultValue = "20") int limit) {
        List<Tool> tools = toolRepository.findHallOfFameTools(PageRequest.of(0, limit));
        List<ToolResponseDto> dtos = tools.stream().map(t -> {
            ToolResponseDto dto = ToolResponseDto.from(t);
            dto.setUpvoteCount(upvoteRepository.countByTool(t));
            dto.setCommentCount(commentRepository.countByToolAndDeletedFalse(t));
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", dtos));
    }
}
