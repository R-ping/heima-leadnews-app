
package com.heima.content.controller.v1.level;

import com.heima.content.service.level.JScoreService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jscore")
public class JScoreController {

    @Autowired
    private JScoreService jScoreService;

    @GetMapping("/overview")
    public ResponseResult overview() {
        ApUser user = AppThreadLocalUtil.getUser();
        Long userId = user != null ? user.getId().longValue() : 0L;
        return jScoreService.getOverview(userId);
    }

    @GetMapping("/detail")
    public ResponseResult detail(
            @RequestParam(required = false, defaultValue = "all") String category,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        ApUser user = AppThreadLocalUtil.getUser();
        Long userId = user != null ? user.getId().longValue() : 0L;
        return jScoreService.getDetail(userId, category, cursor, size);
    }
}
