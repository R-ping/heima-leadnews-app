package com.heima.content.behavior.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.content.behavior.service.ApReadBehaviorService;
import com.heima.content.mapper.ApArticleMapper;
import com.heima.content.mapper.ApBrowseHistoryMapper;
import com.heima.content.service.ApArticleService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApBrowseHistory;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
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
@Transactional
@Slf4j
public class ApReadBehaviorServiceImpl implements ApReadBehaviorService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApBrowseHistoryMapper apBrowseHistoryMapper;

    @Autowired
    private ApArticleService apArticleService;

    @Override
    public ResponseResult readBehavior(ReadBehaviorDto dto) {
        //1.检查参数
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //3.更新文章阅读数
        LambdaUpdateWrapper<ApArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ApArticle::getId, dto.getArticleId());
        updateWrapper.setSql("views = views + " + (dto.getCount() != null ? dto.getCount() : 1));
        apArticleMapper.update(null, updateWrapper);

        //4.记录浏览历史
        ApBrowseHistory browseHistory = new ApBrowseHistory();
        browseHistory.setUserId(user.getId().longValue());
        browseHistory.setArticleId(dto.getArticleId());
        browseHistory.setReadCount(dto.getCount() != null ? dto.getCount().intValue() : 1);
        browseHistory.setBrowseTime(new Date());
        apBrowseHistoryMapper.insert(browseHistory);

        //5.更新文章热度分数
        apArticleService.updateScoreByBehavior(dto.getArticleId(), UpdateArticleMess.UpdateArticleType.VIEWS,
            dto.getCount() != null ? dto.getCount().intValue() : 1);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }
}