package com.heima.content.controller.v1;

import com.heima.content.service.ContentImportService;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/content")
public class ContentImportController {

    @Autowired
    private ContentImportService contentImportService;

    @PostMapping("/import")
    public ResponseResult importMarkdown(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseResult.errorResult(400, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
            return ResponseResult.errorResult(400, "仅支持导入 MD 格式的文档");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseResult.errorResult(400, "文档大小不能超过 10 MB");
        }

        return contentImportService.importMarkdown(file);
    }
}