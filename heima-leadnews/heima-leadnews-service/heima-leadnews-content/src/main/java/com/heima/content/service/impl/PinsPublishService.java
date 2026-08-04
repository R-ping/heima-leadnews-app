package com.heima.content.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.apis.notification.INotificationClient;
import com.heima.content.behavior.service.BehaviorEventBus;
import com.heima.content.mapper.ApCircleMapper;
import com.heima.content.mapper.ApPinsMapper;
import com.heima.content.mapper.TopicMapper;
import com.heima.model.article.dtos.PinsPublishDTO;
import com.heima.model.article.pojos.ApCircle;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.article.pojos.ApTopic;
import com.heima.model.audit.AuditContext;
import com.heima.model.audit.AuditEntityType;
import com.heima.model.behavior.BehaviorContext;
import com.heima.model.behavior.BehaviorType;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PinsPublishService {

    @Autowired
    private ApPinsMapper apPinsMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private ApCircleMapper apCircleMapper;

    @Autowired
    private PinsAuditService pinsAuditService;

    @Autowired(required = false)
    private BehaviorEventBus behaviorEventBus;

    @Autowired(required = false)
    private INotificationClient notificationClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(rollbackFor = Exception.class)
    public ResponseResult publish(PinsPublishDTO dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "内容不能为空");
        }

        ApPins pins = new ApPins();
        pins.setUserId(user.getId().longValue());
        pins.setAuthorId(user.getId().longValue());
        pins.setAuthorName(user.getNickname() != null ? user.getNickname() : "");
        pins.setAuthorImage(user.getImage() != null ? user.getImage() : "");
        pins.setUserName(user.getNickname() != null ? user.getNickname() : "");
        pins.setUserAvatar(user.getImage() != null ? user.getImage() : "");
        pins.setContent(dto.getContent());
        pins.setImageUrls(dto.getImageUrls() != null ? dto.getImageUrls() : "");
        pins.setTopicTags(dto.getTopicTags() != null ? dto.getTopicTags() : "");
        pins.setTopicId(dto.getTopicId());
        pins.setCircleId(dto.getCircleId());
        pins.setLinkUrl(dto.getLinkUrl() != null ? dto.getLinkUrl() : "");
        pins.setLinkTitle(dto.getLinkTitle() != null ? dto.getLinkTitle() : "");
        pins.setStatus(ApPins.Status.SUBMIT.getCode());
        pins.setLikes(0);
        pins.setComment(0);
        pins.setShare(0);
        pins.setIsDeleted(false);
        pins.setCreatedTime(new Date());
        pins.setPublishTime(new Date());
        apPinsMapper.insert(pins);

        // 更新话题计数
        if (dto.getTopicId() != null) {
            ApTopic topic = topicMapper.selectById(dto.getTopicId());
            if (topic != null) {
                topic.setPostCount((topic.getPostCount() != null ? topic.getPostCount() : 0) + 1);
                topicMapper.updateById(topic);
            }
        }

        // 更新圈子计数
        if (dto.getCircleId() != null) {
            ApCircle circle = apCircleMapper.selectById(dto.getCircleId());
            if (circle != null) {
                circle.setPinsCount((circle.getPinsCount() != null ? circle.getPinsCount() : 0) + 1);
                apCircleMapper.updateById(circle);
            }
        }

        // 发送系统通知（沸点正在审核中）
        sendPinsCreatedNotification(pins);

        // 异步审核（使用统一审核模块）
        asyncReviewPins(pins, user);

        return ResponseResult.okResult(pins);
    }

    @Async
    public void asyncReviewPins(ApPins pins, ApUser user) {
        try {
            log.info("开始异步审核沸点, pinsId={}", pins.getId());

            // 构建审核上下文
            AuditContext auditContext = new AuditContext(AuditEntityType.PINS, pins.getId(), pins.getAuthorId());
            auditContext.withTitle("")
                .withContent(pins.getContent())
                .withAuthorName(pins.getAuthorName());

            // 提取图片URL列表
            if (pins.getImageUrls() != null && !pins.getImageUrls().isEmpty()) {
                List<String> imageUrls = Arrays.stream(pins.getImageUrls().split(","))
                    .map(String::trim)
                    .filter(url -> !url.isEmpty())
                    .collect(Collectors.toList());
                auditContext.withImageUrls(imageUrls);
            }

            // 执行审核
            com.heima.model.audit.AuditResult result = pinsAuditService.audit(auditContext);

            if (result.isPassed()) {
                log.info("沸点审核通过, pinsId={}", pins.getId());

                // 审核通过后，通过事件总线触发等级积分计算
                if (behaviorEventBus != null && user != null) {
                    try {
                        BehaviorContext behaviorContext = new BehaviorContext(BehaviorType.PUBLISH_PIN, user.getId());
                        behaviorContext.withTarget(2, pins.getId())
                            .withUserInfo(user.getNickname(), user.getImage());
                        behaviorEventBus.execute(behaviorContext);
                        log.info("沸点发布行为已通过事件总线处理, pinsId={}, userId={}", pins.getId(), user.getId());
                    } catch (Exception e) {
                        log.error("沸点发布行为事件处理失败, pinsId={}", pins.getId(), e);
                    }
                }
            } else {
                log.info("沸点审核未通过, pinsId={}, reason={}", pins.getId(), result.getReason());
            }
        } catch (Exception e) {
            log.error("沸点审核异常, pinsId={}", pins.getId(), e);
            try {
                ApPins updatePins = apPinsMapper.selectById(pins.getId());
                if (updatePins != null) {
                    updatePins.setStatus(ApPins.Status.FAIL.getCode());
                    updatePins.setReason("审核系统异常");
                    apPinsMapper.updateById(updatePins);
                }
            } catch (Exception ex) {
                log.error("更新沸点审核失败状态异常, pinsId={}", pins.getId(), ex);
            }
        }
    }

    public void sendPinsCreatedNotification(ApPins pins) {
        try {
            if (notificationClient == null) {
                log.warn("通知服务不可用，跳过发送沸点发布通知, pinsId={}", pins.getId());
                return;
            }
            String message = "你的沸点已发布，正在审核中。";
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("pinsId", String.valueOf(pins.getId()));
            contentMap.put("message", message);
            contentMap.put("notification_type", "system");
            String contentJson = objectMapper.writeValueAsString(contentMap);
            Map<String, Object> params = new HashMap<>();
            params.put("userId", pins.getAuthorId());
            params.put("type", 4);
            params.put("sourceId", String.valueOf(pins.getId()));
            params.put("content", contentJson);
            ResponseResult result = notificationClient.createNotification(params);
            if (result != null && result.getCode() == 200) {
                log.info("沸点发布通知已发送, pinsId={}, authorId={}", pins.getId(), pins.getAuthorId());
            } else {
                log.warn("沸点发布通知发送失败, pinsId={}, result={}", pins.getId(), result);
            }
        } catch (Exception e) {
            log.error("发送沸点发布通知异常, pinsId={}", pins.getId(), e);
        }
    }
}