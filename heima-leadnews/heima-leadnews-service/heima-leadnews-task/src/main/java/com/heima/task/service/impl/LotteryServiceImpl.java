package com.heima.task.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.task.entity.*;
import com.heima.task.mapper.*;
import com.heima.task.service.LotteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LotteryServiceImpl implements LotteryService {

    @Autowired
    private LotteryPrizePoolMapper prizePoolMapper;
    @Autowired
    private LotteryDrawRecordMapper drawRecordMapper;
    @Autowired
    private LotteryPhysicalOrderMapper physicalOrderMapper;
    @Autowired
    private LotteryDailyStateMapper dailyStateMapper;
    @Autowired
    private LotteryBroadcastMessageMapper broadcastMapper;
    @Autowired
    private UserAssetsMapper userAssetsMapper;

    @Override
    public ResponseResult getDashboard(Long userId) {
        UserAssets assets = userAssetsMapper.selectById(userId);
        int oreBalance = (assets != null) ? assets.getOreBalance() : 0;
        int luckyValue = (assets != null) ? assets.getLuckyValue() : 0;

        // 获取今日抽奖状态
        String todayStr = DateUtil.today();
        LotteryDailyState daily = dailyStateMapper.selectOne(
                new LambdaQueryWrapper<LotteryDailyState>()
                        .eq(LotteryDailyState::getUserId, userId)
                        .eq(LotteryDailyState::getStatDate, java.sql.Date.valueOf(todayStr))
        );
        int todayDrawCount = (daily != null && daily.getDrawCount() != null) ? daily.getDrawCount() : 0;
        boolean freeUsed = daily != null && daily.getFreeUsed() != null && daily.getFreeUsed();
        boolean freeAvailable = !freeUsed;

        // 获取奖池
        List<LotteryPrizePool> allPrizes = prizePoolMapper.selectList(
                new LambdaQueryWrapper<LotteryPrizePool>().eq(LotteryPrizePool::getStatus, 1)
                        .orderByAsc(LotteryPrizePool::getSortOrder)
        );

        List<Map<String, Object>> prizeList = new ArrayList<>();
        for (LotteryPrizePool p : allPrizes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("icon", p.getIconUrl());
            item.put("type", p.getType() == 1 ? "ore" : p.getType() == 2 ? "virtual" : "physical");
            item.put("minAmount", p.getMinOre());
            item.put("maxAmount", p.getMaxOre());
            boolean isLocked = p.getUnlockRequiredDraws() != null && p.getUnlockRequiredDraws() > 0
                    && todayDrawCount < p.getUnlockRequiredDraws();
            item.put("isLocked", isLocked);
            if (isLocked) {
                item.put("lockHint", "再抽" + (p.getUnlockRequiredDraws() - todayDrawCount) + "次解锁");
            }
            item.put("unlockRequired", p.getUnlockRequiredDraws() != null ? p.getUnlockRequiredDraws() : 0);
            prizeList.add(item);
        }

        // 获取中奖播报
        List<LotteryBroadcastMessage> broadcasts = broadcastMapper.selectList(
                new LambdaQueryWrapper<LotteryBroadcastMessage>()
                        .orderByDesc(LotteryBroadcastMessage::getCreatedAt)
                        .last("LIMIT 5")
        );
        List<Map<String, Object>> broadcastList = broadcasts.stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("user", b.getUserNickname() != null ? b.getUserNickname() : "用户" + b.getUserId());
            m.put("prize", b.getPrizeName());
            m.put("time", DateUtil.formatDateTime(b.getCreatedAt()));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("oreBalance", oreBalance);
        data.put("freeDrawAvailable", freeAvailable);
        data.put("freeDrawUsed", freeUsed);
        data.put("todayDrawCount", todayDrawCount);
        data.put("luckyValue", luckyValue);
        data.put("luckyThreshold", 6000);
        data.put("prizePool", prizeList);
        data.put("broadcastMessages", broadcastList);

        return ResponseResult.okResult(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult draw(Long userId, String type, Boolean useFree) {
        // 默认为单抽
        if (type == null) type = "single";
        boolean isTen = "ten".equals(type);
        int drawCount = isTen ? 10 : 1;
        int costOre = isTen ? 2000 : 200;

        // 1. 获取今日抽奖状态
        String todayStr = DateUtil.today();
        Date todayDate = java.sql.Date.valueOf(todayStr);
        LotteryDailyState daily = dailyStateMapper.selectOne(
                new LambdaQueryWrapper<LotteryDailyState>()
                        .eq(LotteryDailyState::getUserId, userId)
                        .eq(LotteryDailyState::getStatDate, todayDate)
        );
        if (daily == null) {
            daily = new LotteryDailyState();
            daily.setUserId(userId);
            daily.setStatDate(todayDate);
            daily.setDrawCount(0);
            daily.setFreeUsed(false);
        }

        int todayDrawCount = daily.getDrawCount() != null ? daily.getDrawCount() : 0;

        // 2. 校验免费次数
        boolean useFreeDraw = useFree != null && useFree && !isTen;
        if (useFreeDraw) {
            if (daily.getFreeUsed() != null && daily.getFreeUsed()) {
                return ResponseResult.errorResult(400, "今日免费次数已使用");
            }
            costOre = 0;
        }

        // 3. 校验矿石余额
        UserAssets assets = userAssetsMapper.selectById(userId);
        int oreBalance = (assets != null) ? assets.getOreBalance() : 0;
        if (!useFreeDraw && oreBalance < costOre) {
            return ResponseResult.errorResult(400, "矿石不足，当前余额：" + oreBalance);
        }
        if (isTen && oreBalance < 2000) {
            return ResponseResult.errorResult(400, "矿石不足2000，无法十连抽，当前余额：" + oreBalance);
        }

        // 4. 获取有效奖池
        List<LotteryPrizePool> effectivePool = prizePoolMapper.selectList(
                new LambdaQueryWrapper<LotteryPrizePool>()
                        .eq(LotteryPrizePool::getStatus, 1)
                        .orderByAsc(LotteryPrizePool::getSortOrder)
        );

        // 5. 执行抽奖
        String batchId = IdUtil.fastSimpleUUID();
        List<Map<String, Object>> results = new ArrayList<>();
        int totalCost = 0;
        int currentLucky = (assets != null && assets.getLuckyValue() != null) ? assets.getLuckyValue() : 0;

        for (int i = 0; i < drawCount; i++) {
            int luckyBefore = currentLucky;
            // 判断是否触发保底
            int gainedLucky = 10;
            boolean isGuaranteed = (currentLucky + gainedLucky) >= 6000;
            int currentDrawIdx = i; // 用于lambda的最终变量

            LotteryPrizePool selectedPrize;
            if (isGuaranteed) {
                // 保底：从实物奖品中抽取
                int unlockThreshold = todayDrawCount + currentDrawIdx;
                List<LotteryPrizePool> physicalPrizes = effectivePool.stream()
                        .filter(p -> p.getIsPhysical() != null && p.getIsPhysical()
                                && (p.getUnlockRequiredDraws() == null || p.getUnlockRequiredDraws() <= unlockThreshold))
                        .collect(Collectors.toList());
                if (!physicalPrizes.isEmpty()) {
                    selectedPrize = physicalPrizes.get(0);
                } else {
                    // 降级为最高价值矿石
                    selectedPrize = effectivePool.stream()
                            .filter(p -> p.getType() == 1)
                            .max(Comparator.comparing(LotteryPrizePool::getMaxOre))
                            .orElse(effectivePool.get(0));
                }
                int overflow = (currentLucky + gainedLucky) - 6000;
                currentLucky = Math.max(0, overflow);
            } else {
                // 普通抽取：按概率权重
                selectedPrize = randomDraw(effectivePool, todayDrawCount + i);
                if (selectedPrize.getIsPhysical() != null && selectedPrize.getIsPhysical()) {
                    currentLucky = 0;
                } else {
                    currentLucky += gainedLucky;
                }
            }

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("prizeId", selectedPrize.getId());
            result.put("prizeName", selectedPrize.getName());
            result.put("prizeType", selectedPrize.getType() == 1 ? "ore" : selectedPrize.getType() == 2 ? "virtual" : "physical");

            Long physicalOrderId = null;
            if (selectedPrize.getType() == 1) {
                // 矿石奖励：随机范围
                int oreAmount = selectedPrize.getMinOre() + new Random().nextInt(selectedPrize.getMaxOre() - selectedPrize.getMinOre() + 1);
                result.put("oreAmount", oreAmount);
                // 直接增加矿石
                if (assets == null) {
                    assets = new UserAssets();
                    assets.setUserId(userId);
                    assets.setOreBalance(0);
                    assets.setFrozenOre(0);
                    assets.setLuckyValue(0);
                    assets.setCreatedAt(new Date());
                    assets.setUpdatedAt(new Date());
                    userAssetsMapper.insert(assets);
                }
                oreBalance += oreAmount;
            } else if (selectedPrize.getType() == 3) {
                // 实物：创建订单
                LotteryPhysicalOrder order = new LotteryPhysicalOrder();
                order.setUserId(userId);
                order.setPrizeId(selectedPrize.getId());
                order.setPrizeName(selectedPrize.getName());
                order.setStatus(1); // 待填地址
                Calendar expireCal = Calendar.getInstance();
                expireCal.add(Calendar.DAY_OF_MONTH, 30);
                order.setExpireAt(expireCal.getTime());
                order.setCreatedAt(new Date());
                order.setUpdatedAt(new Date());
                physicalOrderMapper.insert(order);
                physicalOrderId = order.getId();
                result.put("physicalOrderId", physicalOrderId);

                // 添加中奖播报
                LotteryBroadcastMessage msg = new LotteryBroadcastMessage();
                msg.setUserId(userId);
                msg.setUserNickname("用户" + userId);
                msg.setPrizeName(selectedPrize.getName());
                msg.setPrizeType(3);
                msg.setCreatedAt(new Date());
                broadcastMapper.insert(msg);
            }

            result.put("luckyValueGained", isGuaranteed ? 0 : gainedLucky);
            result.put("isSpecialUnlock", isGuaranteed);
            results.add(result);

            // 记录抽奖记录
            LotteryDrawRecord record = new LotteryDrawRecord();
            record.setDrawBatchId(batchId);
            record.setUserId(userId);
            record.setPrizeId(selectedPrize.getId());
            record.setPrizeName(selectedPrize.getName());
            record.setPrizeType(selectedPrize.getType());
            record.setOreAmount(selectedPrize.getType() == 1 ? (Integer) result.get("oreAmount") : 0);
            record.setVirtualItemCode(selectedPrize.getVirtualItemCode());
            record.setPhysicalOrderId(physicalOrderId);
            record.setLuckyValueBefore(luckyBefore);
            record.setLuckyValueAfter(currentLucky);
            record.setTodayDrawCountAtTime(todayDrawCount + i);
            record.setCostOre(useFreeDraw ? 0 : costOre / drawCount);
            record.setIsFree(useFreeDraw);
            record.setCreatedAt(new Date());
            drawRecordMapper.insert(record);
        }

        // 6. 扣矿石
        if (!useFreeDraw) {
            oreBalance -= costOre;
        }
        if (assets == null) {
            assets = new UserAssets();
            assets.setUserId(userId);
            assets.setOreBalance(0);
            assets.setFrozenOre(0);
            assets.setLuckyValue(0);
            assets.setCreatedAt(new Date());
            assets.setUpdatedAt(new Date());
            userAssetsMapper.insert(assets);
        }
        // 更新资产的矿石和幸运值
        assets.setOreBalance(oreBalance);
        assets.setLuckyValue(currentLucky);
        assets.setUpdatedAt(new Date());
        userAssetsMapper.updateById(assets);

        // 7. 更新每日抽奖状态
        if (daily.getId() == null) {
            daily.setDrawCount(drawCount);
            daily.setFreeUsed(useFreeDraw);
            dailyStateMapper.insert(daily);
        } else {
            daily.setDrawCount(daily.getDrawCount() + drawCount);
            if (useFreeDraw) {
                daily.setFreeUsed(true);
            }
            dailyStateMapper.updateById(daily);
        }

        // 8. 构建返回
        Map<String, Object> data = new HashMap<>();
        data.put("drawId", batchId);
        data.put("results", results);
        data.put("totalOreCost", useFreeDraw ? 0 : costOre);
        data.put("remainingOre", oreBalance);
        data.put("newLuckyValue", currentLucky);
        data.put("todayDrawCountUpdated", todayDrawCount + drawCount);

        return ResponseResult.okResult(data);
    }

    private LotteryPrizePool randomDraw(List<LotteryPrizePool> pool, int todayDrawCount) {
        // 构建有效奖池（排除未解锁的）
        List<LotteryPrizePool> effective = pool.stream()
                .filter(p -> p.getUnlockRequiredDraws() == null || p.getUnlockRequiredDraws() <= todayDrawCount)
                .collect(Collectors.toList());

        if (effective.isEmpty()) {
            return pool.get(0);
        }

        double rand = Math.random();
        double cumulative = 0.0;
        for (LotteryPrizePool p : effective) {
            cumulative += p.getProbability().doubleValue();
            if (rand <= cumulative) {
                return p;
            }
        }
        // 兜底：返回第一个矿石奖品
        return effective.stream().filter(p -> p.getType() == 1).findFirst().orElse(effective.get(0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult claimPhysical(Long userId, Map<String, Object> body) {
        String orderIdStr = (String) body.get("orderId");
        if (orderIdStr == null) {
            return ResponseResult.errorResult(400, "缺少订单ID");
        }
        Long orderId = Long.parseLong(orderIdStr);
        String receiverName = (String) body.get("receiverName");
        String phone = (String) body.get("phone");
        String address = (String) body.get("address");

        if (receiverName == null || phone == null || address == null) {
            return ResponseResult.errorResult(400, "收货信息不完整");
        }

        LotteryPhysicalOrder order = physicalOrderMapper.selectById(orderId);
        if (order == null) {
            return ResponseResult.errorResult(400, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseResult.errorResult(400, "无权操作该订单");
        }
        if (order.getStatus() != 1) {
            return ResponseResult.errorResult(400, "订单状态不正确");
        }

        order.setReceiverName(receiverName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setStatus(2); // 待发货
        order.setUpdatedAt(new Date());
        physicalOrderMapper.updateById(order);

        return ResponseResult.okResult("收货地址已提交");
    }

    @Override
    public ResponseResult getMyPrizes(Long userId, Integer page, Integer size, String type) {
        if (page == null) page = 1;
        if (size == null) size = 20;

        LambdaQueryWrapper<LotteryDrawRecord> wrapper = new LambdaQueryWrapper<LotteryDrawRecord>()
                .eq(LotteryDrawRecord::getUserId, userId)
                .orderByDesc(LotteryDrawRecord::getCreatedAt);

        if (type != null && !"all".equals(type)) {
            int prizeType = "ore".equals(type) ? 1 : "virtual".equals(type) ? 2 : 3;
            wrapper.eq(LotteryDrawRecord::getPrizeType, prizeType);
        }

        Page<LotteryDrawRecord> p = new Page<>(page, size);
        List<LotteryDrawRecord> records = drawRecordMapper.selectPage(p, wrapper).getRecords();

        List<Map<String, Object>> list = records.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("drawId", r.getId());
            m.put("prizeName", r.getPrizeName());
            m.put("prizeType", r.getPrizeType());
            m.put("oreAmount", r.getOreAmount());
            m.put("createdAt", DateUtil.formatDateTime(r.getCreatedAt()));

            if (r.getPhysicalOrderId() != null) {
                LotteryPhysicalOrder po = physicalOrderMapper.selectById(r.getPhysicalOrderId());
                if (po != null) {
                    String statusText;
                    switch (po.getStatus()) {
                        case 1: statusText = "待填地址"; break;
                        case 2: statusText = "待发货"; break;
                        case 3: statusText = "已发货"; break;
                        case 4: statusText = "已签收"; break;
                        case 5: statusText = "已过期"; break;
                        default: statusText = "未知";
                    }
                    m.put("orderStatus", statusText);
                    m.put("orderId", po.getId());
                }
            }
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", p.getTotal());
        data.put("page", page);
        data.put("size", size);

        return ResponseResult.okResult(data);
    }

    @Override
    public ResponseResult getBroadcast() {
        List<LotteryBroadcastMessage> broadcasts = broadcastMapper.selectList(
                new LambdaQueryWrapper<LotteryBroadcastMessage>()
                        .orderByDesc(LotteryBroadcastMessage::getCreatedAt)
                        .last("LIMIT 20")
        );
        List<Map<String, Object>> list = broadcasts.stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("user", b.getUserNickname() != null ? b.getUserNickname() : "用户" + b.getUserId());
            m.put("prize", b.getPrizeName());
            m.put("time", DateUtil.formatDateTime(b.getCreatedAt()));
            return m;
        }).collect(Collectors.toList());

        return ResponseResult.okResult(list);
    }
}