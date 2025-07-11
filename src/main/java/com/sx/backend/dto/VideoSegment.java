package com.sx.backend.dto;

import lombok.Data;

@Data
public class VideoSegment {
    private Float start; // 片段开始时间(秒)
    private Float end;   // 片段结束时间(秒)
    private Integer count = 1; // 观看次数

    public VideoSegment(Float start, Float end, Integer count) {
        this.start = start;
        this.end = end;
        this.count = count;
    }
}
