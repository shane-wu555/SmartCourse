package com.sx.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sx.backend.dto.VideoSegment;
import com.sx.backend.entity.Resource;
import com.sx.backend.entity.VideoProgress;
import com.sx.backend.exception.ResourceNotFoundException;
import com.sx.backend.mapper.ResourceMapper;
import com.sx.backend.mapper.VideoProgressMapper;
import com.sx.backend.service.impl.VideoProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoProgressServiceImplTest {

    @Mock
    VideoProgressMapper videoProgressMapper;

    @Mock
    ResourceMapper resourceMapper;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    VideoProgressServiceImpl videoProgressService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOrCreateProgress_shouldReturnExistingProgress_whenExists() {
        String resourceId = "res1";
        String userId = "user1";

        VideoProgress existing = new VideoProgress();
        existing.setProgressId("progress1");

        when(videoProgressMapper.selectByResourceAndStudent(resourceId, userId)).thenReturn(existing);

        VideoProgress result = videoProgressService.getOrCreateProgress(resourceId, userId);

        assertNotNull(result);
        assertEquals("progress1", result.getProgressId());
        verify(videoProgressMapper, never()).insert(any());
    }

    @Test
    void getOrCreateProgress_shouldCreateNewProgress_whenNotExists() {
        String resourceId = "res1";
        String userId = "user1";

        when(videoProgressMapper.selectByResourceAndStudent(resourceId, userId)).thenReturn(null);

        Resource resource = new Resource();
        resource.setDuration(100f);

        when(resourceMapper.getResourceById(resourceId)).thenReturn(resource);
        when(videoProgressMapper.insert(any())).then(invocation -> {
            VideoProgress p = invocation.getArgument(0);
            assertNotNull(p.getProgressId());
            assertEquals(userId, p.getUserId());
            return 1;
        });

        VideoProgress progress = videoProgressService.getOrCreateProgress(resourceId, userId);

        assertNotNull(progress);
        assertEquals(userId, progress.getUserId());
        assertEquals(0f, progress.getLastPosition());
        assertEquals(100f, progress.getVideo().getDuration());
        verify(videoProgressMapper).insert(any());
    }

    @Test
    void getOrCreateProgress_shouldThrowException_whenResourceNotFound() {
        String resourceId = "resX";
        String userId = "user1";

        when(videoProgressMapper.selectByResourceAndStudent(resourceId, userId)).thenReturn(null);
        when(resourceMapper.getResourceById(resourceId)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            videoProgressService.getOrCreateProgress(resourceId, userId);
        });
    }

    @Test
    void updateProgress_shouldUpdateAndReturnProgress() {
        String resourceId = "res1";
        String userId = "user1";

        Resource resource = new Resource();
        resource.setDuration(100f);

        VideoProgress existing = new VideoProgress();
        existing.setProgressId("p1");
        existing.setUserId(userId);
        existing.setVideo(resource);
        existing.setCompletionRate(0.3f);
        existing.setHeatmapData("{\"version\":1,\"duration\":100,\"segments\":[]}");

        when(videoProgressMapper.selectByResourceAndStudent(resourceId, userId)).thenReturn(existing);
        when(videoProgressMapper.update(any())).thenReturn(1);

        List<VideoSegment> segments = Collections.singletonList(
                new VideoSegment(0f, 10f, 2)
        );

        VideoProgress updated = videoProgressService.updateProgress(resourceId, userId, 50f, 50f, segments);

        assertNotNull(updated);
        assertEquals(50f, updated.getLastPosition());
        assertTrue(updated.getCompletionRate() > 0.3f);
        assertNotNull(updated.getHeatmapData());

        verify(videoProgressMapper).update(any());
    }

    @Test
    void getFormattedHeatmapData_shouldReturnEmpty_whenNoProgress() {
        String resourceId = "res1";
        String userId = "user1";

        when(videoProgressMapper.selectByResourceAndStudent(resourceId, userId)).thenReturn(null);

        var heatmapData = videoProgressService.getFormattedHeatmapData(resourceId, userId);

        assertNotNull(heatmapData);
        assertEquals(0, heatmapData.getTotalSegments());
        assertEquals(0.0, heatmapData.getDuration());
    }
}
