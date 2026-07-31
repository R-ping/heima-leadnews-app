package com.heima.article.controller.v1;

import com.aliyun.oss.OSS;
import com.heima.article.config.OssConfig;
import com.heima.article.service.PinsPublicService;
import com.heima.model.article.dtos.*;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pins")
@Slf4j
public class PinsPublicController {

    @Autowired
    private PinsPublicService pinsPublicService;

    @Autowired(required = false)
    private OSS ossClient;

    @Autowired(required = false)
    private OssConfig ossConfig;

    /**
     * 沸点列表
     */
    @GetMapping("/list")
    public ResponseResult list(
            @RequestParam(defaultValue = "latest") String tab,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        log.info("获取沸点列表, tab={}, page={}, size={}", tab, page, size);
        return pinsPublicService.list(tab, page, size);
    }

    /**
     * 侧边栏
     */
    @GetMapping("/sidebar")
    public ResponseResult sidebar() {
        log.info("获取沸点侧边栏");
        return pinsPublicService.sidebar();
    }

    /**
     * 发布沸点
     */
    @PostMapping("/publish")
    public ResponseResult publish(@RequestBody PinsPublishDTO dto) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "内容不能为空");
        }
        if (dto.getContent().length() > 1000) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "内容长度不能超过1000字");
        }
        log.info("发布沸点, content={}", dto.getContent());
        return pinsPublicService.publish(dto);
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like")
    public ResponseResult like(@RequestBody PinsLikeDTO dto) {
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }
        log.info("沸点点赞, pinsId={}, liked={}", dto.getPinsId(), dto.getLiked());
        return pinsPublicService.like(dto);
    }

    /**
     * 创建评论
     */
    @PostMapping("/comment/create")
    public ResponseResult createComment(@RequestBody PinsCommentDTO dto) {
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容不能为空");
        }
        if (dto.getContent().length() > 500) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容长度不能超过500字");
        }
        log.info("创建沸点评论, pinsId={}, parentId={}", dto.getPinsId(), dto.getParentId());
        return pinsPublicService.createComment(dto);
    }

    /**
     * 评论列表
     */
    @GetMapping("/comment/list")
    public ResponseResult commentList(
            @RequestParam Long pinsId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        log.info("获取沸点评论列表, pinsId={}, page={}, size={}", pinsId, page, size);
        return pinsPublicService.commentList(pinsId, page, size);
    }

    /**
     * 分享
     */
    @PostMapping("/share")
    public ResponseResult share(@RequestBody PinsShareDTO dto) {
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }
        log.info("分享沸点, pinsId={}", dto.getPinsId());
        return pinsPublicService.share(dto);
    }

    /**
     * 链接预览
     */
    @PostMapping("/link-preview")
    public ResponseResult linkPreview(@RequestBody PinsLinkPreviewDTO dto) {
        if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "URL不能为空");
        }
        log.info("链接预览, url={}", dto.getUrl());
        return pinsPublicService.linkPreview(dto);
    }

    /**
     * 话题列表
     */
    @GetMapping("/topics")
    public ResponseResult topics(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 20;
        log.info("获取话题列表, keyword={}, page={}, size={}", keyword, page, size);
        return pinsPublicService.topics(keyword, page, size);
    }

    /**
     * 所有圈子（按类别分组）
     */
    @GetMapping("/circles")
    public ResponseResult circles() {
        log.info("获取所有圈子");
        return pinsPublicService.circles();
    }

    /**
     * 上传图片到OSS
     */
    @PostMapping("/upload-image")
    public ResponseResult uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文件不能为空");
        }
        if (ossClient == null || ossConfig == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "OSS服务未配置");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String key = ossConfig.getDir() + UUID.randomUUID().toString() + suffix;
            ossClient.putObject(ossConfig.getBucket(), key, file.getInputStream());
            String url = ossConfig.getHost() + "/" + key;
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("key", key);
            log.info("图片上传成功, key={}", key);
            return ResponseResult.okResult(data);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "图片上传失败");
        }
    }
}