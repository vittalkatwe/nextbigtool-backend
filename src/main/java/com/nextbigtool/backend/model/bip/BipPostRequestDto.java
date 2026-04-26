package com.nextbigtool.backend.model.bip;

import com.nextbigtool.backend.entity.bip.PostType;

public class BipPostRequestDto {
    private PostType type;
    private String content;
    private String metricLabel;
    private String metricValue;
    private Long toolId;

    public PostType getType() { return type; }
    public void setType(PostType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMetricLabel() { return metricLabel; }
    public void setMetricLabel(String metricLabel) { this.metricLabel = metricLabel; }

    public String getMetricValue() { return metricValue; }
    public void setMetricValue(String metricValue) { this.metricValue = metricValue; }

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
}
