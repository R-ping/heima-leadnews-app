package com.heima.content.service.level.impl;

import com.heima.common.redis.CacheService;
import com.heima.content.mapper.user.UserScoreDetailsMapper;
import com.heima.content.mapper.user.UserScoreSummaryMapper;
import com.heima.content.service.level.JScoreService;
import com.heima.model.level.dtos.JScoreDetailVO;
import com.heima.model.level.dtos.JScoreDetailVO.JScoreDetailItem;
import com.heima.model.level.dtos.JScoreOverviewVO;
import com.heima.model.level.dtos.JScoreOverviewVO.ChartData;
import com.heima.model.level.dtos.JScoreOverviewVO.TodayTotalVO;
import com.heima.model.user.pojos.UserScoreDetails;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JScoreServiceImpl implements JScoreService {

    @Autowired
    private UserScoreDetailsMapper userScoreDetailsMapper;

    @Autowired
    private UserScoreSummaryMapper userScoreSummaryMapper;

    @Autowired
    private CacheService cacheService;

    private static final String REDIS_KEY_PREFIX = "jscore:today:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * category 映射表
     */
    private static final Map<String, Integer> CATEGORY_MAP = new LinkedHashMap<>();
    static {
        CATEGORY_MAP.put("all", null);
        CATEGORY_MAP.put("effect", 4);
        CATEGORY_MAP.put("active", 2);
        CATEGORY_MAP.put("learn", 3);
        CATEGORY_MAP.put("basic", 1);
        CATEGORY_MAP.put("spec", 5);
    }

    /**
     * category 数字 -> 名称映射
     */
    private static final Map<Integer, String> CATEGORY_NAME_MAP = new LinkedHashMap<>();
    static {
        CATEGORY_NAME_MAP.put(1, "basic");
        CATEGORY_NAME_MAP.put(2, "active");
        CATEGORY_NAME_MAP.put(3, "learn");
        CATEGORY_NAME_MAP.put(4, "effect");
        CATEGORY_NAME_MAP.put(5, "spec");
    }

    /**
     * category 数字 -> 汇总表字段名映射
     */
    private static final Map<Integer, String> CATEGORY_FIELD_MAP = new LinkedHashMap<>();
    static {
        CATEGORY_FIELD_MAP.put(1, "basic_score");
        CATEGORY_FIELD_MAP.put(2, "active_score");
        CATEGORY_FIELD_MAP.put(3, "learn_score");
        CATEGORY_FIELD_MAP.put(4, "effect_score");
        CATEGORY_FIELD_MAP.put(5, "spec_score");
    }

    @Override
    public ResponseResult getOverview(Long userId) {
        try {
            String today = LocalDate.now().format(DATE_FMT);
            JScoreOverviewVO vo = new JScoreOverviewVO();
            vo.setStatDate(today);

            // 1. 从 Redis 读取今日增量
            String redisKey = REDIS_KEY_PREFIX + userId + ":" + today;
            Map<Object, Object> redisData = cacheService.hGetAll(redisKey);
            Map<String, TodayTotalVO> summary = new LinkedHashMap<>();
            ChartData chart = new ChartData();
            List<String> dimensions = new ArrayList<>();
            List<BigDecimal> values = new ArrayList<>();

            // 2. 遍历 5 个分类
            for (int cat = 1; cat <= 5; cat++) {
                String catName = CATEGORY_NAME_MAP.get(cat);
                String fieldName = CATEGORY_FIELD_MAP.get(cat);

                // 今日数据：优先 Redis，降级查询数据库
                BigDecimal todayScore;
                if (redisData != null && !redisData.isEmpty()) {
                    Object val = redisData.get(catName);
                    todayScore = val != null ? new BigDecimal(val.toString()) : BigDecimal.ZERO;
                } else {
                    String todayStart = today + " 00:00:00";
                    BigDecimal dbToday = userScoreDetailsMapper.sumTodayScoreByCategory(userId, cat, todayStart);
                    todayScore = dbToday != null ? dbToday : BigDecimal.ZERO;
                }

                // 总计数据：从汇总表查询
                BigDecimal total = userScoreSummaryMapper.sumFieldScore(userId, fieldName);
                if (total == null) {
                    total = BigDecimal.ZERO;
                }

                TodayTotalVO tt = new TodayTotalVO();
                tt.setToday(todayScore);
                tt.setTotal(total);
                summary.put(catName, tt);

                dimensions.add(catName);
                values.add(total);
            }

            chart.setDimensions(dimensions);
            chart.setValues(values);
            vo.setSummary(summary);
            vo.setChart(chart);

            return ResponseResult.okResult(vo);
        } catch (Exception e) {
            log.error("获取积分概览失败, userId={}", userId, e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "获取积分概览失败");
        }
    }

    @Override
    public ResponseResult getDetail(Long userId, String category, String cursor, Integer size) {
        try {
            // 1. 解析 category 参数
            Integer catNum = CATEGORY_MAP.get(category);
            if (catNum == null && !"all".equals(category)) {
                catNum = null; // 默认 all
            }

            // 2. 解析游标
            String cursorCreatedAt = null;
            Long cursorId = null;
            if (cursor != null && !cursor.isEmpty()) {
                String[] parts = cursor.split("_");
                if (parts.length == 2) {
                    cursorCreatedAt = parts[0];
                    cursorId = Long.parseLong(parts[1]);
                }
            }

            // 3. 多查 1 条判断 has_more
            int querySize = size + 1;

            // 4. 调用 Mapper 游标分页查询
            List<UserScoreDetails> records = userScoreDetailsMapper.selectByCursor(
                userId, catNum, cursorCreatedAt, cursorId, querySize);

            // 5. 判断是否有更多数据
            boolean hasMore = records.size() > size;
            if (hasMore) {
                records = records.subList(0, size);
            }

            // 6. 组装返回
            JScoreDetailVO vo = new JScoreDetailVO();
            List<JScoreDetailItem> items = new ArrayList<>();
            String nextCursor = "";

            for (int i = 0; i < records.size(); i++) {
                UserScoreDetails record = records.get(i);
                JScoreDetailItem item = new JScoreDetailItem();
                item.setId(String.valueOf(record.getId()));
                item.setCreatedAt(record.getCreatedAt() != null
                    ? SDF.format(record.getCreatedAt()) : "");
                item.setActionCode(record.getActionCode() != null ? record.getActionCode() : "");
                item.setActionDesc(record.getActionDesc() != null ? record.getActionDesc() : "");
                item.setScore(record.getScore() != null ? record.getScore() : BigDecimal.ZERO);
                String catName = CATEGORY_NAME_MAP.get(record.getCategory());
                item.setCategory(catName != null ? catName : "");
                items.add(item);

                // 最后一个记录的游标
                if (i == records.size() - 1) {
                    String createdAt = record.getCreatedAt() != null
                        ? SDF.format(record.getCreatedAt()) : "";
                    nextCursor = createdAt + "_" + record.getId();
                }
            }

            vo.setList(items);
            vo.setNextCursor(nextCursor);
            vo.setHasMore(hasMore);

            return ResponseResult.okResult(vo);
        } catch (Exception e) {
            log.error("获取积分明细失败, userId={}, category={}", userId, category, e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "获取积分明细失败");
        }
    }
}