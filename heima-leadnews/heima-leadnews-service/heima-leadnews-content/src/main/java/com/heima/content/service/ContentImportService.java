package com.heima.content.service;

import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

public interface ContentImportService {
    ResponseResult importMarkdown(MultipartFile file);
}