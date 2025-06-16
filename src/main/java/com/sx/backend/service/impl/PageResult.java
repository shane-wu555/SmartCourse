package com.sx.backend.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// 辅助类
@Data
@AllArgsConstructor
public class PageResult<T> {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<T> content;
}
