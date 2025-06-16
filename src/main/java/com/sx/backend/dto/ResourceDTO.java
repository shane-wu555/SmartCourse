package com.sx.backend.dto;

import com.sx.backend.entity.ResourceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceDTO {
    private String resourceId;
    private String courseId;
    private String courseName; // 冗余字段，便于前端显示
    private String name;
    private ResourceType type;
    private String typeDisplayName; // 资源类型显示名称
    private String url;
    private String downloadUrl; // 专门用于下载的URL
    private String previewUrl; // 预览URL（如适用）
    private LocalDateTime uploadTime;
    private String uploaderId;
    private String uploaderName; // 上传者姓名
    private Long size;
    private String sizeDisplay; // 格式化后的文件大小（如 "2.5 MB"）
    private String description;
    private Integer viewCount;
    private Integer downloadCount;

    // 根据文件类型判断是否可以预览
    public boolean isPreviewable() {
        return type == ResourceType.PDF ||
                type == ResourceType.IMAGE ||
                type == ResourceType.VIDEO;
    }
}