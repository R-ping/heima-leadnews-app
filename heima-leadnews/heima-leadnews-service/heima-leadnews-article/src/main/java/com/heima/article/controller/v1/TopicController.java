package com.heima.article.controller.v1;

import com.heima.article.service.TopicService;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/topic")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @GetMapping("/list")
    public ResponseResult findList(@RequestParam(required = false) String keyword) {
        return ResponseResult.okResult(topicService.findList(keyword));
    }

    @GetMapping("/recommend")
    public ResponseResult getRecommendTopics() {
        return ResponseResult.okResult(topicService.getRecommendTopics());
    }
}