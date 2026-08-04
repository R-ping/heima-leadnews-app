package com.heima.content.behavior.service.impl;

import com.heima.content.behavior.service.ApUnlikesBehaviorService;
import com.heima.content.mapper.pins.ApUserActionLogMapper;
import com.heima.model.user.pojos.ApUserActionLog;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * APP不喜欢行为表 服务实现类
 * </p>
 *
 * @author itheima
 */
@Slf4j
@Service
public class ApUnlikesBehaviorServiceImpl implements ApUnlikesBehaviorService {

    @Autowired
    private ApUserActionLogMapper apUserActionLogMapper;

    @Override
    public ResponseResult unLike(UnLikesBehaviorDto dto) {

        if (dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 记录不喜欢行为日志
        ApUserActionLog actionLog = new ApUserActionLog();
        actionLog.setUserId(user.getId().longValue());
        actionLog.setActionType(dto.getType() == 0 ? "UNLIKE_ARTICLE" : "CANCEL_UNLIKE_ARTICLE");
        actionLog.setActionDetail("文章ID:" + dto.getArticleId());
        actionLog.setCreatedTime(new Date());
        apUserActionLogMapper.insert(actionLog);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}