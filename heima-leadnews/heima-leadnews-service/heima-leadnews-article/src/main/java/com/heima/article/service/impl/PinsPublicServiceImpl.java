package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.apis.notification.INotificationClient;
import com.heima.article.mapper.*;
import com.heima.article.service.PinsPublicService;
import com.heima.article.service.TopicService;
import com.heima.model.article.dtos.*;
import com.heima.model.article.pojos.*;
import com.heima.model.article.vos.*;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PinsPublicServiceImpl extends ServiceImpl<ApPinsMapper, ApPins> implements PinsPublicService {

    @Autowired
    private ApPinsMapper apPinsMapper;

    @Autowired
    private ApPinsLikeMapper apPinsLikeMapper;

    @Autowired
    private ApPinsCommentMapper apPinsCommentMapper;

    @Autowired
    private ApFollowMapper apFollowMapper;

    @Autowired
    private ApUserCircleMapper apUserCircleMapper;

    @Autowired
    private ApCircleMapper apCircleMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private ApUserLevelMapper apUserLevelMapper;

    @Autowired
    private TopicService topicService;

    @Autowired(required = false)
    private INotificationClient notificationClient;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResponseResult list(String tab, Integer page, Integer size) {
        ApUser user = getUserOrNull();
        if ("following".equals(tab)) {
            if (user == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            }
            return listFollowing(user, page, size);
        } else if ("hot".equals(tab)) {
            return listHot(user, page, size);
        } else {
            return listLatest(user, page, size);
        }
    }

    private ResponseResult listLatest(ApUser user, Integer page, Integer size) {
        Page<ApPins> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApPins> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPins::getStatus, ApPins.Status.PUBLISHED.getCode());
        wrapper.eq(ApPins::getIsDeleted, false);
        wrapper.orderByDesc(ApPins::getReviewTime);
        IPage<ApPins> result = page(pageParam, wrapper);
        List<PinsVO> voList = convertToVOList(result.getRecords(), user);
        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return ResponseResult.okResult(data);
    }

    private ResponseResult listHot(ApUser user, Integer page, Integer size) {
        // 查询所有已发布的沸点
        LambdaQueryWrapper<ApPins> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPins::getStatus, ApPins.Status.PUBLISHED.getCode());
        wrapper.eq(ApPins::getIsDeleted, false);
        List<ApPins> allPins = list(wrapper);

        if (allPins.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("list", new ArrayList<>());
            data.put("total", 0);
            data.put("page", page);
            data.put("size", size);
            return ResponseResult.okResult(data);
        }

        // 批量查询用户等级
        Set<Long> authorIds = allPins.stream()
                .map(ApPins::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        final Map<Long, ApUserLevel> levelMap;
        if (!authorIds.isEmpty()) {
            LambdaQueryWrapper<ApUserLevel> levelWrapper = new LambdaQueryWrapper<>();
            levelWrapper.in(ApUserLevel::getUserId, authorIds);
            List<ApUserLevel> levels = apUserLevelMapper.selectList(levelWrapper);
            levelMap = levels.stream().collect(Collectors.toMap(ApUserLevel::getUserId, l -> l, (a, b) -> a));
        } else {
            levelMap = new HashMap<>();
        }

        // 计算热度分并排序
        List<ApPins> sortedPins = allPins.stream()
                .sorted((a, b) -> {
                    int scoreA = calcHotScore(a, levelMap);
                    int scoreB = calcHotScore(b, levelMap);
                    return Integer.compare(scoreB, scoreA);
                })
                .collect(Collectors.toList());

        // 手动分页
        int total = sortedPins.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<ApPins> pagePins = fromIndex < total ? sortedPins.subList(fromIndex, toIndex) : new ArrayList<>();

        List<PinsVO> voList = convertToVOList(pagePins, user);
        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("total", (long) total);
        data.put("page", page);
        data.put("size", size);
        return ResponseResult.okResult(data);
    }

    private int calcHotScore(ApPins pin, Map<Long, ApUserLevel> levelMap) {
        int likes = pin.getLikes() != null ? pin.getLikes() : 0;
        int comment = pin.getComment() != null ? pin.getComment() : 0;
        int share = pin.getShare() != null ? pin.getShare() : 0;
        int dailyLevel = 0;
        int powerLevel = 0;
        ApUserLevel level = levelMap.get(pin.getAuthorId());
        if (level != null) {
            dailyLevel = level.getDailyLevel() != null ? level.getDailyLevel() : 0;
            powerLevel = level.getPowerLevel() != null ? level.getPowerLevel() : 0;
        }
        return likes * 1 + comment * 2 + share * 3 + dailyLevel * 5 + powerLevel * 5;
    }

    private ResponseResult listFollowing(ApUser user, Integer page, Integer size) {
        // 获取当前用户关注的用户ID列表
        LambdaQueryWrapper<ApFollow> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.eq(ApFollow::getUserId, user.getId());
        List<ApFollow> follows = apFollowMapper.selectList(followWrapper);
        List<Long> followedIds = follows.stream()
                .map(f -> f.getFollowUserId().longValue())
                .collect(Collectors.toList());

        if (followedIds.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("list", new ArrayList<>());
            data.put("total", 0);
            data.put("page", page);
            data.put("size", size);
            return ResponseResult.okResult(data);
        }

        Page<ApPins> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApPins> pinsWrapper = new LambdaQueryWrapper<>();
        pinsWrapper.in(ApPins::getAuthorId, followedIds);
        pinsWrapper.eq(ApPins::getStatus, ApPins.Status.PUBLISHED.getCode());
        pinsWrapper.eq(ApPins::getIsDeleted, false);
        pinsWrapper.orderByDesc(ApPins::getReviewTime);
        IPage<ApPins> result = page(pageParam, pinsWrapper);

        List<PinsVO> voList = convertToVOList(result.getRecords(), user);
        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return ResponseResult.okResult(data);
    }

    @Override
    @Transactional
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
        save(pins);

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

        // 发送通知
        sendPinsCreatedNotification(pins);

        // 异步审核
        asyncReviewPins(pins);

        return ResponseResult.okResult(pins);
    }

    @Async
    public void asyncReviewPins(ApPins pins) {
        try {
            // 日志记录
            log.info("开始异步审核沸点, pinsId={}", pins.getId());

            // 简单审核：直接通过
            pins.setStatus(ApPins.Status.PUBLISHED.getCode());
            pins.setReviewTime(new Date());
            updateById(pins);
            log.info("沸点审核通过, pinsId={}", pins.getId());
        } catch (Exception e) {
            log.error("沸点审核异常, pinsId={}", pins.getId(), e);
            try {
                pins.setStatus(ApPins.Status.FAIL.getCode());
                pins.setReason("审核系统异常");
                updateById(pins);
            } catch (Exception ex) {
                log.error("更新沸点审核失败状态异常, pinsId={}", pins.getId(), ex);
            }
        }
    }

    private void sendPinsCreatedNotification(ApPins pins) {
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
            params.put("userId", Long.valueOf(pins.getAuthorId()));
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

    @Override
    @Transactional
    public ResponseResult like(PinsLikeDTO dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }

        LambdaQueryWrapper<ApPinsLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPinsLike::getPinsId, dto.getPinsId());
        wrapper.eq(ApPinsLike::getUserId, user.getId());
        ApPinsLike existLike = apPinsLikeMapper.selectOne(wrapper);

        if (Boolean.TRUE.equals(dto.getLiked())) {
            // 点赞
            if (existLike != null) {
                return ResponseResult.okResult();
            }
            ApPinsLike like = new ApPinsLike();
            like.setPinsId(dto.getPinsId());
            like.setUserId(user.getId());
            like.setCreatedTime(new Date());
            apPinsLikeMapper.insert(like);
            // 更新沸点点赞数
            ApPins pins = getById(dto.getPinsId());
            if (pins != null) {
                pins.setLikes((pins.getLikes() != null ? pins.getLikes() : 0) + 1);
                updateById(pins);
            }
        } else {
            // 取消点赞
            if (existLike == null) {
                return ResponseResult.okResult();
            }
            apPinsLikeMapper.deleteById(existLike.getId());
            ApPins pins = getById(dto.getPinsId());
            if (pins != null) {
                int newLikes = Math.max(0, (pins.getLikes() != null ? pins.getLikes() : 0) - 1);
                pins.setLikes(newLikes);
                updateById(pins);
            }
        }
        return ResponseResult.okResult();
    }

    @Override
    @Transactional
    public ResponseResult createComment(PinsCommentDTO dto) {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容不能为空");
        }

        ApPinsComment comment = new ApPinsComment();
        comment.setPinsId(dto.getPinsId());
        comment.setUserId(user.getId());
        comment.setUserName(user.getNickname() != null ? user.getNickname() : "");
        comment.setUserAvatar(user.getImage() != null ? user.getImage() : "");
        comment.setParentId(dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setCreatedTime(new Date());
        apPinsCommentMapper.insert(comment);

        // 更新沸点评论数
        ApPins pins = getById(dto.getPinsId());
        if (pins != null) {
            pins.setComment((pins.getComment() != null ? pins.getComment() : 0) + 1);
            updateById(pins);
        }

        // 如果是回复，更新父评论的回复数
        if (dto.getParentId() != null) {
            ApPinsComment parentComment = apPinsCommentMapper.selectById(dto.getParentId());
            if (parentComment != null) {
                parentComment.setReplyCount((parentComment.getReplyCount() != null ? parentComment.getReplyCount() : 0) + 1);
                apPinsCommentMapper.updateById(parentComment);
            }
        }

        return ResponseResult.okResult(comment);
    }

    @Override
    public ResponseResult commentList(Long pinsId, Integer page, Integer size) {
        if (pinsId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }

        // 分页查询顶级评论
        Page<ApPinsComment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApPinsComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPinsComment::getPinsId, pinsId);
        wrapper.isNull(ApPinsComment::getParentId).or().eq(ApPinsComment::getParentId, 0);
        wrapper.orderByDesc(ApPinsComment::getCreatedTime);
        IPage<ApPinsComment> pageResult = apPinsCommentMapper.selectPage(pageParam, wrapper);
        List<ApPinsComment> pageComments = pageResult.getRecords();
        long total = pageResult.getTotal();

        // 批量查询子回复
        List<ApPinsComment> allSubReplies = new ArrayList<>();
        if (!pageComments.isEmpty()) {
            List<Long> parentIds = pageComments.stream().map(ApPinsComment::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ApPinsComment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.in(ApPinsComment::getParentId, parentIds);
            replyWrapper.orderByAsc(ApPinsComment::getCreatedTime);
            allSubReplies = apPinsCommentMapper.selectList(replyWrapper);
        }
        Map<Long, List<ApPinsComment>> replyMap = allSubReplies.stream()
                .collect(Collectors.groupingBy(ApPinsComment::getParentId));

        // 转换为VO，每个顶级评论附带子回复
        List<PinsCommentVO> voList = pageComments.stream().map(comment -> {
            PinsCommentVO vo = convertCommentToVO(comment);
            List<ApPinsComment> replies = replyMap.getOrDefault(comment.getId(), new ArrayList<>());
            List<PinsCommentVO> replyVOs = replies.stream()
                    .map(this::convertCommentToVO)
                    .collect(Collectors.toList());
            vo.setReplies(replyVOs);
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return ResponseResult.okResult(data);
    }

    @Override
    @Transactional
    public ResponseResult share(PinsShareDTO dto) {
        if (dto.getPinsId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "pinsId不能为空");
        }
        ApPins pins = getById(dto.getPinsId());
        if (pins == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        // 原子递增分享数
        apPinsMapper.incrementShare(dto.getPinsId());
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult linkPreview(PinsLinkPreviewDTO dto) {
        if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "URL不能为空");
        }
        PinsLinkPreviewVO vo = new PinsLinkPreviewVO();
        vo.setUrl(dto.getUrl());
        try {
            URI uri = new URI(dto.getUrl());
            String domain = uri.getHost();
            vo.setDomain(domain != null ? domain : "");
        } catch (Exception e) {
            vo.setDomain("");
        }
        vo.setTitle("");
        return ResponseResult.okResult(vo);
    }

    @Override
    public ResponseResult sidebar() {
        ApUser user = getUserOrNull();
        PinsSidebarVO vo = new PinsSidebarVO();

        if (user != null) {
            // 沸点数量
            LambdaQueryWrapper<ApPins> pinsWrapper = new LambdaQueryWrapper<>();
            pinsWrapper.eq(ApPins::getAuthorId, user.getId().longValue());
            pinsWrapper.eq(ApPins::getIsDeleted, false);
            vo.setPinsCount((int) count(pinsWrapper));

            // 圈子数量
            LambdaQueryWrapper<ApUserCircle> circleWrapper = new LambdaQueryWrapper<>();
            circleWrapper.eq(ApUserCircle::getUserId, user.getId());
            vo.setCircleCount((int) apUserCircleMapper.selectCount(circleWrapper).longValue());

            // 关注数量
            LambdaQueryWrapper<ApFollow> followWrapper = new LambdaQueryWrapper<>();
            followWrapper.eq(ApFollow::getUserId, user.getId());
            vo.setFollowingCount((int) apFollowMapper.selectCount(followWrapper).longValue());

            // 粉丝数量
            LambdaQueryWrapper<ApFollow> fansWrapper = new LambdaQueryWrapper<>();
            fansWrapper.eq(ApFollow::getFollowUserId, user.getId());
            vo.setFollowersCount((int) apFollowMapper.selectCount(fansWrapper).longValue());
        }

        // 精选沸点：likes + comment 最高的3条
        LambdaQueryWrapper<ApPins> featuredWrapper = new LambdaQueryWrapper<>();
        featuredWrapper.eq(ApPins::getStatus, ApPins.Status.PUBLISHED.getCode());
        featuredWrapper.eq(ApPins::getIsDeleted, false);
        List<ApPins> allPublished = list(featuredWrapper);
        List<ApPins> featuredPins = allPublished.stream()
                .sorted((a, b) -> {
                    int scoreA = (a.getLikes() != null ? a.getLikes() : 0) + (a.getComment() != null ? a.getComment() : 0);
                    int scoreB = (b.getLikes() != null ? b.getLikes() : 0) + (b.getComment() != null ? b.getComment() : 0);
                    return Integer.compare(scoreB, scoreA);
                })
                .limit(3)
                .collect(Collectors.toList());
        vo.setFeaturedPins(convertToVOList(featuredPins, user));

        // 推荐话题
        try {
            Map<String, Object> topicResult = topicService.recommend(0, 5);
            @SuppressWarnings("unchecked")
            List<TopicRecommendVO> topics = (List<TopicRecommendVO>) topicResult.get("list");
            vo.setRecommendedTopics(topics != null ? topics : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取推荐话题失败", e);
            vo.setRecommendedTopics(new ArrayList<>());
        }

        return ResponseResult.okResult(vo);
    }

    // ========== 工具方法 ==========

    private ApUser getUserOrNull() {
        try {
            return AppThreadLocalUtil.getUser();
        } catch (Exception e) {
            return null;
        }
    }

    private List<PinsVO> convertToVOList(List<ApPins> pinsList, ApUser currentUser) {
        if (pinsList == null || pinsList.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量查询当前用户是否已点赞
        Set<Long> likedPinsIds = new HashSet<>();
        if (currentUser != null) {
            List<Long> pinsIds = pinsList.stream().map(ApPins::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ApPinsLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.in(ApPinsLike::getPinsId, pinsIds);
            likeWrapper.eq(ApPinsLike::getUserId, currentUser.getId());
            List<ApPinsLike> likedList = apPinsLikeMapper.selectList(likeWrapper);
            likedPinsIds = likedList.stream().map(ApPinsLike::getPinsId).collect(Collectors.toSet());
        }

        Set<Long> finalLikedPinsIds = likedPinsIds;
        return pinsList.stream().map(pin -> convertToVO(pin, finalLikedPinsIds)).collect(Collectors.toList());
    }

    private PinsVO convertToVO(ApPins pin, Set<Long> likedPinsIds) {
        PinsVO vo = new PinsVO();
        vo.setId(pin.getId());
        vo.setUserId(pin.getUserId());
        vo.setUserName(pin.getUserName() != null ? pin.getUserName() : "");
        vo.setUserAvatar(pin.getUserAvatar() != null ? pin.getUserAvatar() : "");
        vo.setAuthorId(pin.getAuthorId());
        vo.setAuthorName(pin.getAuthorName() != null ? pin.getAuthorName() : "");
        vo.setAuthorImage(pin.getAuthorImage() != null ? pin.getAuthorImage() : "");
        vo.setContent(pin.getContent() != null ? pin.getContent() : "");
        vo.setImageUrls(parseStringList(pin.getImageUrls()));
        vo.setTopicTags(parseStringList(pin.getTopicTags()));
        vo.setLinkUrl(pin.getLinkUrl() != null ? pin.getLinkUrl() : "");
        vo.setLinkTitle(pin.getLinkTitle() != null ? pin.getLinkTitle() : "");
        vo.setLikeCount(pin.getLikes() != null ? pin.getLikes() : 0);
        vo.setCommentCount(pin.getComment() != null ? pin.getComment() : 0);
        vo.setShareCount(pin.getShare() != null ? pin.getShare() : 0);
        vo.setLiked(likedPinsIds != null && likedPinsIds.contains(pin.getId()));
        vo.setCreatedTime(pin.getCreatedTime());
        vo.setPublishTime(pin.getPublishTime());
        vo.setReviewTime(pin.getReviewTime());
        return vo;
    }

    private List<String> parseStringList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private PinsCommentVO convertCommentToVO(ApPinsComment comment) {
        PinsCommentVO vo = new PinsCommentVO();
        vo.setId(comment.getId());
        vo.setPinsId(comment.getPinsId());
        vo.setUserId(comment.getUserId());
        vo.setUserName(comment.getUserName() != null ? comment.getUserName() : "");
        vo.setUserAvatar(comment.getUserAvatar() != null ? comment.getUserAvatar() : "");
        vo.setParentId(comment.getParentId() != null ? comment.getParentId() : 0L);
        vo.setContent(comment.getContent() != null ? comment.getContent() : "");
        vo.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0);
        vo.setReplyCount(comment.getReplyCount() != null ? comment.getReplyCount() : 0);
        vo.setReplies(new ArrayList<>());
        vo.setCreatedTime(comment.getCreatedTime());
        return vo;
    }

    @Override
    public ResponseResult topics(String keyword, Integer page, Integer size) {
        Page<ApTopic> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApTopic> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ApTopic::getName, keyword);
        }
        wrapper.orderByDesc(ApTopic::getPostCount);
        IPage<ApTopic> result = topicMapper.selectPage(pageParam, wrapper);
        List<Map<String, Object>> voList = result.getRecords().stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName() != null ? t.getName() : "");
            m.put("count", t.getPostCount() != null ? t.getPostCount() : 0);
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("list", voList);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult circles() {
        // 查询所有一级圈子（parent_id为null）
        LambdaQueryWrapper<ApCircle> parentWrapper = new LambdaQueryWrapper<>();
        parentWrapper.isNull(ApCircle::getParentId);
        parentWrapper.orderByAsc(ApCircle::getSortOrder);
        List<ApCircle> parentCircles = apCircleMapper.selectList(parentWrapper);

        // 查询所有二级圈子
        LambdaQueryWrapper<ApCircle> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.isNotNull(ApCircle::getParentId);
        childWrapper.orderByAsc(ApCircle::getSortOrder);
        List<ApCircle> childCircles = apCircleMapper.selectList(childWrapper);

        // 按parentId分组
        Map<Long, List<ApCircle>> childrenMap = childCircles.stream()
                .collect(Collectors.groupingBy(ApCircle::getParentId));

        // 构建返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (ApCircle parent : parentCircles) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", parent.getId());
            category.put("name", parent.getName() != null ? parent.getName() : "");
            List<Map<String, Object>> items = new ArrayList<>();
            List<ApCircle> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
            for (ApCircle child : children) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", child.getId());
                item.put("name", child.getName() != null ? child.getName() : "");
                item.put("icon", child.getIcon() != null ? child.getIcon() : "");
                item.put("memberCount", child.getMemberCount() != null ? child.getMemberCount() : 0);
                item.put("pinsCount", child.getPinsCount() != null ? child.getPinsCount() : 0);
                items.add(item);
            }
            category.put("circles", items);
            result.add(category);
        }
        return ResponseResult.okResult(result);
    }

    @Override
    public ResponseResult uploadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "图片URL不能为空");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("url", imageUrl);
        return ResponseResult.okResult(data);
    }
}