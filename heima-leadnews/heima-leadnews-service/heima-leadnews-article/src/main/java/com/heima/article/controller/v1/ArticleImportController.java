package com.heima.article.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/article")
@Slf4j
public class ArticleImportController {

    private static final Pattern TITLE_PATTERN = Pattern.compile("^#\\s+(.+)$");

    @PostMapping("/import")
    public ResponseResult importMarkdown(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseResult.errorResult(400, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
            return ResponseResult.errorResult(400, "仅支持.md格式文件");
        }

        try {
            // 读取文件内容
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }

            // 提取第一个 # 标题作为文章标题
            String title = "";
            int titleIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                Matcher matcher = TITLE_PATTERN.matcher(lines.get(i).trim());
                if (matcher.matches()) {
                    title = matcher.group(1).trim();
                    titleIndex = i;
                    break;
                }
            }

            // 如果没有找到标题，使用文件名（去掉扩展名）
            if (title.isEmpty()) {
                title = originalFilename.substring(0, originalFilename.length() - 3);
            }

            // 构建正文：移除标题行，压缩连续空行
            StringBuilder contentBuilder = new StringBuilder();
            boolean lastWasEmpty = false;
            for (int i = 0; i < lines.size(); i++) {
                if (i == titleIndex) {
                    continue; // 跳过标题行
                }
                String line = lines.get(i);
                boolean isEmpty = line.trim().isEmpty();
                if (isEmpty) {
                    if (lastWasEmpty) {
                        continue; // 跳过连续空行
                    }
                    lastWasEmpty = true;
                } else {
                    lastWasEmpty = false;
                }
                contentBuilder.append(line).append("\n");
            }

            String content = contentBuilder.toString().trim();

            Map<String, String> data = new HashMap<>();
            data.put("title", title);
            data.put("content", content);

            log.info("Markdown文件导入成功, fileName={}, title={}", originalFilename, title);
            return ResponseResult.okResult(data);

        } catch (Exception e) {
            log.error("Markdown文件导入失败", e);
            return ResponseResult.errorResult(500, "文件解析失败: " + e.getMessage());
        }
    }
}
