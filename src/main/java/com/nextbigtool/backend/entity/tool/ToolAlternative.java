package com.nextbigtool.backend.entity.tool;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tool_alternatives")
@Data
public class ToolAlternative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String url;
}