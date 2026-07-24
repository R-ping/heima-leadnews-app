package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApColumnMapper;
import com.heima.article.mapper.ApPinsMapper;
import com.heima.article.service.ContentDataService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApColumn;
import com.heima.model.article.pojos.ApPins;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContentDataServiceImpl implements ContentDataService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApColumnMapper apColumnMapper;

    @Autowired
    private ApPinsMapper apPinsMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== Article ====================

    @Override
    public ResponseResult getArticleStatistics(Long userId, String startDate, String endDate) {
        Map<String, Object> current = calcArticleMetrics(userId, startDate, endDate);
        String prevDate = getPreviousDay(startDate);
        Map<String, Object> previous = calcArticleMetrics(userId, prevDate, prevDate);

        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", current.get("totalCount"));
        data.put("totalTrend", ((Number) current.get("totalCount")).intValue() - ((Number) previous.get("totalCount")).intValue());
        data.put("showCount", current.get("showCount"));
        data.put("showTrend", ((Number) current.get("showCount")).intValue() - ((Number) previous.get("showCount")).intValue());
        data.put("readCount", current.get("readCount"));
        data.put("readTrend", ((Number) current.get("readCount")).intValue() - ((Number) previous.get("readCount")).intValue());
        data.put("likeCount", current.get("likeCount"));
        data.put("likeTrend", ((Number) current.get("likeCount")).intValue() - ((Number) previous.get("likeCount")).intValue());
        data.put("commentCount", current.get("commentCount"));
        data.put("commentTrend", ((Number) current.get("commentCount")).intValue() - ((Number) previous.get("commentCount")).intValue());
        data.put("collectCount", current.get("collectCount"));
        data.put("collectTrend", ((Number) current.get("collectCount")).intValue() - ((Number) previous.get("collectCount")).intValue());

        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult getArticleTrend(Long userId, String startDate, String endDate, Integer days) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.format(DATE_FORMATTER);
            Map<String, Object> metrics = calcArticleMetrics(userId, dateStr, dateStr);
            Map<String, Object> point = new HashMap<>();
            point.put("date", dateStr);
            point.put("showCount", metrics.get("showCount"));
            point.put("readCount", metrics.get("readCount"));
            point.put("likeCount", metrics.get("likeCount"));
            point.put("commentCount", metrics.get("commentCount"));
            point.put("collectCount", metrics.get("collectCount"));
            trendData.add(point);
        }

        return ResponseResult.okResult(trendData);
    }

    @Override
    public ResponseResult getArticleDetail(Long userId, String startDate, String endDate, Integer page, Integer size) {
        Page<ApArticle> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticle::getAuthorId, userId);
        wrapper.eq(ApArticle::getIsDeleted, false);
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(ApArticle::getPublishTime, parseDate(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(ApArticle::getPublishTime, parseDateEnd(endDate));
        }
        wrapper.orderByDesc(ApArticle::getPublishTime);

        IPage<ApArticle> result = apArticleMapper.selectPage(pageParam, wrapper);
        List<Map<String, Object>> list = result.getRecords().stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("publishTime", a.getPublishTime());
            m.put("readCount", a.getViews() != null ? a.getViews() : 0);
            m.put("likeCount", a.getLikes() != null ? a.getLikes() : 0);
            m.put("commentCount", a.getComment() != null ? a.getComment() : 0);
            m.put("collectCount", a.getCollection() != null ? a.getCollection() : 0);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        return ResponseResult.okResult(data);
    }

    // ==================== Column ====================

    @Override
    public ResponseResult getColumnStatistics(Long userId, String startDate, String endDate) {
        Map<String, Object> current = calcColumnMetrics(userId, startDate, endDate);
        String prevDate = getPreviousDay(startDate);
        Map<String, Object> previous = calcColumnMetrics(userId, prevDate, prevDate);

        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", current.get("totalCount"));
        data.put("totalTrend", ((Number) current.get("totalCount")).intValue() - ((Number) previous.get("totalCount")).intValue());
        data.put("subscribeCount", current.get("subscribeCount"));
        data.put("subscribeTrend", ((Number) current.get("subscribeCount")).intValue() - ((Number) previous.get("subscribeCount")).intValue());

        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult getColumnTrend(Long userId, String startDate, String endDate, Integer days) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.format(DATE_FORMATTER);
            Map<String, Object> metrics = calcColumnMetrics(userId, dateStr, dateStr);
            Map<String, Object> point = new HashMap<>();
            point.put("date", dateStr);
            point.put("subscribeCount", metrics.get("subscribeCount"));
            trendData.add(point);
        }

        return ResponseResult.okResult(trendData);
    }

    @Override
    public ResponseResult getColumnDetail(Long userId, String startDate, String endDate, Integer page, Integer size) {
        Page<ApColumn> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApColumn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApColumn::getAuthorId, userId);
        wrapper.eq(ApColumn::getIsDeleted, false);
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(ApColumn::getCreatedTime, parseDate(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(ApColumn::getCreatedTime, parseDateEnd(endDate));
        }
        wrapper.orderByDesc(ApColumn::getCreatedTime);

        IPage<ApColumn> result = apColumnMapper.selectPage(pageParam, wrapper);
        List<Map<String, Object>> list = result.getRecords().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("title", c.getTitle());
            m.put("publishTime", c.getCreatedTime());
            m.put("subscribeCount", c.getSubscribeCount() != null ? c.getSubscribeCount() : 0);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        return ResponseResult.okResult(data);
    }

    // ==================== Pin ====================

    @Override
    public ResponseResult getPinStatistics(Long userId, String startDate, String endDate) {
        Map<String, Object> current = calcPinMetrics(userId, startDate, endDate);
        String prevDate = getPreviousDay(startDate);
        Map<String, Object> previous = calcPinMetrics(userId, prevDate, prevDate);

        Map<String, Object> data = new HashMap<>();
        data.put("totalCount", current.get("totalCount"));
        data.put("totalTrend", ((Number) current.get("totalCount")).intValue() - ((Number) previous.get("totalCount")).intValue());
        data.put("likeCount", current.get("likeCount"));
        data.put("likeTrend", ((Number) current.get("likeCount")).intValue() - ((Number) previous.get("likeCount")).intValue());
        data.put("commentCount", current.get("commentCount"));
        data.put("commentTrend", ((Number) current.get("commentCount")).intValue() - ((Number) previous.get("commentCount")).intValue());

        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult getPinTrend(Long userId, String startDate, String endDate, Integer days) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.format(DATE_FORMATTER);
            Map<String, Object> metrics = calcPinMetrics(userId, dateStr, dateStr);
            Map<String, Object> point = new HashMap<>();
            point.put("date", dateStr);
            point.put("likeCount", metrics.get("likeCount"));
            point.put("commentCount", metrics.get("commentCount"));
            trendData.add(point);
        }

        return ResponseResult.okResult(trendData);
    }

    @Override
    public ResponseResult getPinDetail(Long userId, String startDate, String endDate, Integer page, Integer size) {
        Page<ApPins> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApPins> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPins::getAuthorId, userId);
        wrapper.eq(ApPins::getIsDeleted, false);
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(ApPins::getPublishTime, parseDate(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(ApPins::getPublishTime, parseDateEnd(endDate));
        }
        wrapper.orderByDesc(ApPins::getPublishTime);

        IPage<ApPins> result = apPinsMapper.selectPage(pageParam, wrapper);
        List<Map<String, Object>> list = result.getRecords().stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("content", p.getContent());
            m.put("publishTime", p.getPublishTime());
            m.put("likeCount", p.getLikes() != null ? p.getLikes() : 0);
            m.put("commentCount", p.getComment() != null ? p.getComment() : 0);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        return ResponseResult.okResult(data);
    }

    // ==================== Private Helpers ====================

    private Map<String, Object> calcArticleMetrics(Long userId, String startDate, String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDateEnd(endDate);

        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticle::getAuthorId, userId);
        wrapper.eq(ApArticle::getIsDeleted, false);
        wrapper.between(ApArticle::getPublishTime, start, end);
        List<ApArticle> articles = apArticleMapper.selectList(wrapper);

        int totalCount = articles.size();
        int showCount = totalCount;
        int readCount = articles.stream().mapToInt(a -> a.getViews() != null ? a.getViews() : 0).sum();
        int likeCount = articles.stream().mapToInt(a -> a.getLikes() != null ? a.getLikes() : 0).sum();
        int commentCount = articles.stream().mapToInt(a -> a.getComment() != null ? a.getComment() : 0).sum();
        int collectCount = articles.stream().mapToInt(a -> a.getCollection() != null ? a.getCollection() : 0).sum();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCount", totalCount);
        metrics.put("showCount", showCount);
        metrics.put("readCount", readCount);
        metrics.put("likeCount", likeCount);
        metrics.put("commentCount", commentCount);
        metrics.put("collectCount", collectCount);
        return metrics;
    }

    private Map<String, Object> calcColumnMetrics(Long userId, String startDate, String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDateEnd(endDate);

        LambdaQueryWrapper<ApColumn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApColumn::getAuthorId, userId);
        wrapper.eq(ApColumn::getIsDeleted, false);
        wrapper.between(ApColumn::getCreatedTime, start, end);
        List<ApColumn> columns = apColumnMapper.selectList(wrapper);

        int totalCount = columns.size();
        int subscribeCount = columns.stream().mapToInt(c -> c.getSubscribeCount() != null ? c.getSubscribeCount() : 0).sum();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCount", totalCount);
        metrics.put("subscribeCount", subscribeCount);
        return metrics;
    }

    private Map<String, Object> calcPinMetrics(Long userId, String startDate, String endDate) {
        Date start = parseDate(startDate);
        Date end = parseDateEnd(endDate);

        LambdaQueryWrapper<ApPins> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApPins::getAuthorId, userId);
        wrapper.eq(ApPins::getIsDeleted, false);
        wrapper.between(ApPins::getPublishTime, start, end);
        List<ApPins> pins = apPinsMapper.selectList(wrapper);

        int totalCount = pins.size();
        int likeCount = pins.stream().mapToInt(p -> p.getLikes() != null ? p.getLikes() : 0).sum();
        int commentCount = pins.stream().mapToInt(p -> p.getComment() != null ? p.getComment() : 0).sum();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalCount", totalCount);
        metrics.put("likeCount", likeCount);
        metrics.put("commentCount", commentCount);
        return metrics;
    }

    private String getPreviousDay(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
        return date.minusDays(1).format(DATE_FORMATTER);
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            log.error("parseDate error: {}", dateStr, e);
            return new Date();
        }
    }

    private Date parseDateEnd(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(dateStr + " 23:59:59");
        } catch (Exception e) {
            log.error("parseDateEnd error: {}", dateStr, e);
            return new Date();
        }
    }
}