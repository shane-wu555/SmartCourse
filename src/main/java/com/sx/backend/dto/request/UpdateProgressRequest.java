package com.sx.backend.dto.request;

import com.sx.backend.dto.VideoSegment;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProgressRequest {
    private Float lastPosition;
    private Float totalWatched;
    private List<VideoSegment> segments;
}
