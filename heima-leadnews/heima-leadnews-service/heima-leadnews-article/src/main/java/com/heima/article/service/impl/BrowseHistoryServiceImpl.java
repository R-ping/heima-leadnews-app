package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApBrowseHistoryMapper;
import com.heima.article.service.BrowseHistoryService;
import com.heima.model.article.pojos.ApBrowseHistory;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
@Slf4j
public class BrowseHistoryServiceImpl extends ServiceImpl<ApBrowseHistoryMapper, ApBrowseHistory> implements BrowseHistoryService {

    @Override
    public ResponseResult getHistoryList(Long userId, Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<ApBrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApBrowseHistory::getUserId, userId);
        wrapper.eq(ApBrowseHistory::getIsDeleted, false);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(ApBrowseHistory::getArticleTitle, keyword.trim());
        }
        wrapper.orderByDesc(ApBrowseHistory::getBrowseTime);

        IPage<ApBrowseHistory> pageResult = page(new Page<>(page, size), wrapper);

        List<ApBrowseHistory> records = pageResult.getRecords();
        List<Map<String, Object>> groupedList = groupByDate(records);

        Map<String, Object> result = new HashMap<>();
        result.put("list", groupedList);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);

        return ResponseResult.okResult(result);
    }

    @Override
    public void clearHistory(Long userId) {
        LambdaUpdateWrapper<ApBrowseHistory> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ApBrowseHistory::getUserId, userId);
        wrapper.set(ApBrowseHistory::getIsDeleted, true);
        update(wrapper);
    }

    /**
     * 将浏览记录按日期分组
     */
    private List<Map<String, Object>> groupByDate(List<ApBrowseHistory> records) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<Map<String, Object>>> dateMap = new LinkedHashMap<>();

        for (ApBrowseHistory record : records) {
            String dateKey = sdf.format(record.getBrowseTime());
            dateMap.computeIfAbsent(dateKey, k -> new ArrayList<>());

            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("articleId", record.getArticleId());
            item.put("articleTitle", record.getArticleTitle());
            item.put("authorName", record.getAuthorName());
            item.put("browseTime", record.getBrowseTime());
            dateMap.get(dateKey).add(item);
        }

        List<Map<String, Object>> groupedList = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : dateMap.entrySet()) {
            Map<String, Object> group = new HashMap<>();
            group.put("date", entry.getKey());
            group.put("items", entry.getValue());
            groupedList.add(group);
        }

        return groupedList;
    }
}