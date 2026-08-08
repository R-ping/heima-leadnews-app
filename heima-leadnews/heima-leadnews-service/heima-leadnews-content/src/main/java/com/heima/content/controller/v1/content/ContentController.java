package com.heima.content.controller.v1.content;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容聚合控制器 — 提供统一的 /api/v1/content/* 路径
 * 用于兼容前端通过 /content/ 前缀调用的各种接口
 */
@RestController
@RequestMapping("/api/v1/content")
public class ContentController {
}