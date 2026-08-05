package com.heima.content.service.impl;

import com.heima.content.service.ContentImportService;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ContentImportServiceImpl implements ContentImportService {

    private final Tika tika = new Tika();

    @Override
    public ResponseResult importMarkdown(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            String content = tika.parseToString(is);

            String originalFilename = file.getOriginalFilename();
            String title = "未命名文档";
            if (originalFilename != null && originalFilename.contains(".md")) {
                title = originalFilename.substring(0, originalFilename.lastIndexOf(".md"));
            }

            Map<String, String> data = new HashMap<>();
            data.put("title", title);
            data.put("content", content);

            log.info("文档导入成功: title={}, contentLength={}", title, content.length());
            return ResponseResult.okResult(data);
        } catch (Exception e) {
            log.error("文档导入失败", e);
            return ResponseResult.errorResult(500, "文档解析失败: " + e.getMessage());
        }
    }
}