package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/feign")
@Slf4j
public class UserFeignController {

    @Autowired
    private ApUserMapper apUserMapper;

    /**
     * 获取用户基本信息（供其他服务Feign调用）
     */
    @GetMapping("/basic-info")
    public ResponseResult getBasicInfo(@RequestParam("userId") Long userId) {
        if (userId == null) {
            return ResponseResult.errorResult(400, "userId不能为空");
        }
        ApUser user = apUserMapper.selectById(userId);
        if (user == null) {
            return ResponseResult.errorResult(404, "用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        userInfo.put("avatar", user.getImage() != null ? user.getImage() : "");
        return ResponseResult.okResult(userInfo);
    }
}