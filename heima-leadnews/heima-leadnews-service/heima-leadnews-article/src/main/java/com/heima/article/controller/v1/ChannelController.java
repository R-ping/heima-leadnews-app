package com.heima.article.controller.v1;

import com.heima.article.service.ChannelService;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/channel")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @GetMapping("/channels")
    public ResponseResult findAll() {
        return channelService.findAll();
    }
}