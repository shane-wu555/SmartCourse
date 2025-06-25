package com.sx.backend.service;

import com.sx.backend.entity.FileMeta;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /* * 上传文件
     * @param file 要上传的文件
     * @return 文件元数据，包括文件ID、名称、类型、大小和下载链接
     */
    FileMeta uploadFile(MultipartFile file);

    
}
