package com.sx.backend.entity;

import java.time.LocalDateTime;

public class Resource {
    private String resourceId;
    private String courseId;
    private String name;
    private ResourceType type;
    private String url;
    private LocalDateTime uploadTime;
    private String uploaderId;
    private Long size;
    private String description;
    private Integer viewCount;
    private Float duration;

    public Resource(String resourceId, String name, String courseId, String url, ResourceType type, String uploaderId, String description) {
        this.resourceId = resourceId;
        this.name = name;
        this.courseId = courseId;
        this.url = url;
        this.type = type;
        this.uploaderId = uploaderId;
        this.description = description;
        this.uploadTime = LocalDateTime.now();
    }

    public Resource() {
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Float getDuration() { return duration; }

    public void setDuration(Float duration) { this.duration = duration; }
}
