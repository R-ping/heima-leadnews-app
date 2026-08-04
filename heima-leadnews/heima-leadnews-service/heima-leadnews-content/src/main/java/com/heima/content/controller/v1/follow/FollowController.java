
package com.heima.content.controller.v1.follow;

import com.heima.content.service.fans.FansDataService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/follow")
public class FollowController {

    @Autowired
    private FansDataService fansDataService;

    @PostMapping("/do")
    public ResponseResult doFollow(@RequestParam("userId") Long userId, @RequestParam("followUserId") Long followUserId) {
        ApUser apUser = new ApUser();
        apUser.setId(userId.intValue());
        AppThreadLocalUtil.setUser(apUser);
        try {
            return fansDataService.followFans(followUserId.intValue());
        } finally {
            AppThreadLocalUtil.clear();
        }
    }
}
