package com.sx.backend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HeatmapData {
    private Integer version = 1;
    private Float duration;
    private List<VideoSegment> segments = new ArrayList<>();
}
