package com.heima.content.behavior.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.content.behavior.service.ApLikesBehaviorService;
import com.heima.content.mapper.ApArticleMapper;
import com.heima.content.mapper.ApUserActionLogMapper;
import com.heima.content.service.ApArticleService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApUserActionLog;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ApLikesBehaviorServiceImpl implements ApLikesBehaviorService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApUserActionLogMapper apUserActionLogMapper;

    @Autowired
    private ApArticleService apArticleService;

    @Override
    public ResponseResult like(LikesBehaviorDto dto) {

        //1.检查参数
        if (dto == null || dto.getArticleId() == null || checkParam(dto)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //3.更新文章点赞数
        LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApArticle::getId, dto.getArticleId());
        if (dto.getOperation() == 0) {
            // 点赞：likes + 1
            updateWrapper.setSql("likes = likes + 1");
        } else {
            // 取消点赞：likes - 1
            updateWrapper.setSql("likes = likes - 1");
        }
        apArticleMapper.update(null, updateWrapper);

        //4.记录行为日志
        ApUserActionLog actionLog = new ApUserActionLog();
        actionLog.setUserId(user.getId().longValue());
        actionLog.setActionType(dto.getOperation() == 0 ? "LIKE" : "UNLIKE");
        actionLog.setActionDetail("文章ID:" + dto.getArticleId() + ",类型:" + dto.getType());
        actionLog.setCreatedTime(new Date());
        apUserActionLogMapper.insert(actionLog);

        //5.更新文章热度分数
        apArticleService.updateScoreByBehavior(dto.getArticleId(), UpdateArticleMess.UpdateArticleType.LIKES,
            dto.getOperation() == 0 ? 1 : -1);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }

    /**
     * 检查参数
     *
     * @return
     */
    private boolean checkParam(LikesBehaviorDto dto) {
        if (dto.getType() > 2 || dto.getType() < 0 || dto.getOperation() > 1 || dto.getOperation() < 0) {
            return true;
        }
        return false;
    }
}