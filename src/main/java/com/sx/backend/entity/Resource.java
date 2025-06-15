package com.sx.backend.entity;

import java.time.LocalDateTime;

public class Resource {
    private String resourceId;
    private Course course;
    private String name;
    private ResourceType type;
    private String url;
    private LocalDateTime uploadTime;
    private User uploader;
    private Long size;
    private String description;
    private Integer viewCount;

    public Resource(String resourceId, String name, Course course, String url, ResourceType type, User uploader, String description) {
        this.resourceId = resourceId;
        this.name = name;
        this.course = course;
        this.url = url;
        this.type = type;
        this.uploader = uploader;
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

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
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

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
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
}
