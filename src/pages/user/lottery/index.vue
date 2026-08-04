<template>
    <div class="lottery-page">
        <div class="art-top" v-if="!isDesktop"><HomeBar/></div>

        <div class="lottery-content">
            <!-- User Info Bar -->
            <div class="user-info-bar">
                <div class="user-info-section">
                    <span class="user-nickname">{{ dashboard.user.name || '用户' }}</span>
                    <span class="user-level-badge">{{ dashboard.user.level || 'JY.1' }}</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section">
                    <span class="info-value">{{ dashboard.user.ore || 0 }}</span>
                    <span class="info-label">矿石</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section ore-section">
                    <span class="info-value ore-value">{{ dashboard.user.ore || 0 }}</span>
                    <span class="ore-icon">&#xf06d;</span>
                    <span class="ore-question">&#xf059;</span>
                </div>
            </div>

            <!-- Sub Tabs -->
            <div class="sub-tabs">
                <div
                    v-for="tab in subTabs"
                    :key="tab.key"
                    class="tab-item"
                    :class="{ active: activeTab === tab.key }"
                    @click="switchTab(tab)"
                >
                    {{ tab.label }}
                </div>
            </div>

            <!-- Lottery Main Area -->
            <div class="lottery-main">
                <div class="prize-pool-section">
                    <div class="section-title">奖品池</div>
                    <div class="prize-grid">
                        <div
                            v-for="(prize, index) in prizePool"
                            :key="index"
                            class="prize-item"
                            :class="{ 'is-first': index === 0 }"
                        >
                            <div class="prize-icon">&#xf0c0;</div>
                            <div class="prize-name">{{ prize.name }}</div>
                            <div class="prize-level" :class="'level-' + (prize.level || 1)">{{ prize.levelLabel || '普通' }}</div>
                        </div>
                    </div>
                    <div class="draw-section">
                        <div class="draw-info">
                            <span class="draw-count">剩余抽奖次数：{{ drawInfo.remainCount || 0 }}</span>
                            <span class="draw-free" v-if="drawInfo.hasFree">免费抽奖({{ drawInfo.freeCount || 0 }})</span>
                        </div>
                        <div class="draw-actions">
                            <div class="draw-btn" @click="handleDraw(1, false)">
                                <span class="draw-btn-icon">&#xf0e7;</span>
                                <span class="draw-btn-text">抽奖</span>
                                <span class="draw-btn-cost">消耗 {{ drawInfo.cost || 10 }} 矿石</span>
                            </div>
                            <div class="draw-btn draw-btn-free" @click="handleDraw(1, true)" v-if="drawInfo.hasFree && (drawInfo.freeCount || 0) > 0">
                                <span class="draw-btn-icon">&#xf06d;</span>
                                <span class="draw-btn-text">免费抽奖</span>
                                <span class="draw-btn-cost">今日免费</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Broadcast Section -->
            <div class="broadcast-section" v-if="broadcasts.length > 0">
                <div class="broadcast-header">
                    <span class="broadcast-icon">&#xf0a1;</span>
                    <span class="broadcast-title">中奖播报</span>
                </div>
                <div class="broadcast-list" ref="broadcastList">
                    <div
                        v-for="(item, index) in broadcasts"
                        :key="index"
                        class="broadcast-item"
                    >
                        <span class="broadcast-user">{{ item.userName }}</span>
                        <span class="broadcast-action">抽中了</span>
                        <span class="broadcast-prize">{{ item.prizeName }}</span>
                    </div>
                </div>
            </div>

            <!-- Lottery Records -->
            <div class="records-section">
                <div class="section-title">抽奖记录</div>
                <div class="records-list" v-if="records.length > 0">
                    <div
                        v-for="(record, index) in records"
                        :key="index"
                        class="record-item"
                    >
                        <div class="record-info">
                            <span class="record-prize">{{ record.prizeName }}</span>
                            <span class="record-time">{{ record.createTime }}</span>
                        </div>
                        <div class="record-status" :class="{ 'is-win': record.status === 'WIN' }">
                            {{ record.status === 'WIN' ? '已中奖' : '未中奖' }}
                        </div>
                    </div>
                </div>
                <div class="records-empty" v-else>
                    <span class="empty-icon">&#xf06d;</span>
                    <span class="empty-text">暂无抽奖记录</span>
                </div>
            </div>

            <!-- Footer -->
            <div class="lottery-footer">
                用户协议 · 法律声明 &copy;{{ new Date().getFullYear() }} 稀土掘金
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import Utils from '@/utils/env'
import store from '@/stores/store'
import { getDashboard, doDraw, getMyPrizes, getBroadcast } from '@/apis/lottery'
import { toast } from '@/utils/toast'

export default {
    name: 'UserLottery',
    components: { HomeBar },
    data() {
        return {
            activeTab: 'lottery',
            subTabs: [
                { key: 'checkin', label: '每日签到' },
                { key: 'growth', label: '成长等级' },
                { key: 'lottery', label: '幸运抽奖' },
                { key: 'exchange', label: '福利兑换' },
                { key: 'harvest', label: '我的收获' }
            ],
            dashboard: {
                user: { name: '', level: '', ore: 0 },
                prizePool: [],
                drawInfo: { remainCount: 0, freeCount: 0, hasFree: false, cost: 10 },
                records: []
            },
            prizePool: [],
            drawInfo: { remainCount: 0, freeCount: 0, hasFree: false, cost: 10 },
            records: [],
            broadcasts: [],
            broadcastTimer: null
        }
    },
    computed: {
        isDesktop() {
            return Utils.isDesktop()
        }
    },
    mounted() {
        this.loadDashboard()
        this.loadBroadcasts()
        this.startBroadcastLoop()
    },
    beforeDestroy() {
        if (this.broadcastTimer) {
            clearInterval(this.broadcastTimer)
            this.broadcastTimer = null
        }
    },
    methods: {
        switchTab(tab) {
            if (tab.key === 'lottery') {
                this.activeTab = tab.key
                return
            }
            if (tab.key === 'checkin') {
                this.$router.push('/user/checkin')
                return
            }
            if (tab.key === 'growth') {
                this.$router.push('/user/growth')
                return
            }
            if (tab.key === 'exchange') {
                toast('福利兑换功能开发中', 2)
                return
            }
            if (tab.key === 'harvest') {
                toast('我的收获功能开发中', 2)
                return
            }
        },
        async loadDashboard() {
            try {
                const res = await getDashboard()
                if (res && res.code === 200 && res.data) {
                    const data = res.data
                    const userInfo = store.state.userInfo || {}
                    this.dashboard.user = {
                        name: userInfo.nickName || '用户',
                        level: 'JY.1',
                        ore: data.oreBalance != null ? data.oreBalance : 0
                    }
                    this.prizePool = data.prizePool || []
                    this.drawInfo = {
                        remainCount: data.drawInfo && data.drawInfo.remainCount != null ? data.drawInfo.remainCount : 0,
                        freeCount: data.drawInfo && data.drawInfo.freeCount != null ? data.drawInfo.freeCount : 0,
                        hasFree: data.drawInfo && data.drawInfo.hasFree != null ? data.drawInfo.hasFree : false,
                        cost: data.drawInfo && data.drawInfo.cost != null ? data.drawInfo.cost : 10
                    }
                    this.records = data.records || []
                }
            } catch (error) {
                toast('加载数据失败，请稍后重试', 2)
            }
        },
        async handleDraw(type, useFree) {
            try {
                if (!useFree && this.drawInfo.remainCount <= 0) {
                    toast('抽奖次数不足', 2)
                    return
                }
                const res = await doDraw(type, useFree)
                if (res && res.code === 200) {
                    const prize = res.data && res.data.prize ? res.data.prize : null
                    if (prize) {
                        toast('恭喜获得：' + prize.name, 3)
                    } else {
                        toast('抽奖成功，再接再厉！', 2)
                    }
                    await this.loadDashboard()
                    await this.loadRecords()
                } else {
                    toast(res && res.message ? res.message : '抽奖失败', 2)
                }
            } catch (error) {
                toast('抽奖失败，请稍后重试', 2)
            }
        },
        async loadRecords() {
            try {
                const res = await getMyPrizes({ page: 1, pageSize: 20 })
                if (res && res.code === 200 && res.data) {
                    this.records = res.data.records || res.data.list || []
                }
            } catch (error) {
                // Keep current records on error
            }
        },
        async loadBroadcasts() {
            try {
                const res = await getBroadcast()
                if (res && res.code === 200 && res.data) {
                    this.broadcasts = res.data.list || res.data || []
                }
            } catch (error) {
                // Keep current broadcasts on error
            }
        },
        startBroadcastLoop() {
            this.broadcastTimer = setInterval(() => {
                this.loadBroadcasts()
            }, 30000)
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.lottery-page {
    min-height: 100vh;
    background: #f5f7fa;
}

.lottery-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px 24px;
}

// User Info Bar
.user-info-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fff;
    border-radius: 0 0 12px 12px;
    padding: 16px 24px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    flex-wrap: wrap;
    gap: 12px;
}

.user-info-section {
    display: flex;
    align-items: center;
    gap: 6px;
}

.user-nickname {
    font-size: 15px;
    color: #1a1a1a;
    font-weight: 500;
}

.user-level-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 10px;
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    line-height: 1.5;
}

.info-separator {
    color: #e8e8e8;
    font-size: 14px;
}

.info-value {
    font-size: 18px;
    font-weight: 700;
    color: #fa8c16;
}

.info-label {
    font-size: 13px;
    color: #999;
}

.ore-section {
    .ore-value {
        font-size: 18px;
    }

    .ore-icon {
        font-family: fontawesome;
        font-size: 16px;
        color: #fa8c16;
    }

    .ore-question {
        font-family: fontawesome;
        font-size: 14px;
        color: #ccc;
        cursor: pointer;
        margin-left: 2px;
    }
}

// Sub Tabs
.sub-tabs {
    display: flex;
    background: #fff;
    border-radius: 12px;
    padding: 0 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.tab-item {
    padding: 16px 20px;
    font-size: 15px;
    color: #666;
    cursor: pointer;
    position: relative;
    transition: color 0.2s;

    &:hover {
        color: #1e80ff;
    }

    &.active {
        color: #1e80ff;
        font-weight: 600;

        &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 28px;
            height: 3px;
            background: #1e80ff;
            border-radius: 2px;
        }
    }
}

// Prize Pool Section
.lottery-main {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 16px;
}

.prize-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-bottom: 24px;
}

.prize-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 12px;
    background: #fafafa;
    border-radius: 12px;
    border: 1px solid #f0f0f0;
    transition: all 0.2s;

    &:hover {
        border-color: #1e80ff;
        box-shadow: 0 4px 12px rgba(30, 128, 255, 0.1);
    }

    &.is-first {
        border-color: #fa8c16;
        background: linear-gradient(135deg, #fff7e6, #fffbe6);
    }
}

.prize-icon {
    font-family: fontawesome;
    font-size: 36px;
    color: #fa8c16;
    margin-bottom: 8px;
}

.prize-name {
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
    margin-bottom: 6px;
    text-align: center;
}

.prize-level {
    font-size: 12px;
    padding: 2px 10px;
    border-radius: 10px;
    background: #f0f0f0;
    color: #999;

    &.level-1 {
        background: #fff7e6;
        color: #fa8c16;
    }

    &.level-2 {
        background: #f0f5ff;
        color: #1e80ff;
    }

    &.level-3 {
        background: #fff0f0;
        color: #ff4d4f;
    }

    &.level-4 {
        background: linear-gradient(135deg, #fff7e6, #fff0f0);
        color: #fa8c16;
    }
}

// Draw Section
.draw-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0;
    border-top: 1px solid #f2f3f5;
    flex-wrap: wrap;
    gap: 12px;
}

.draw-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.draw-count {
    font-size: 14px;
    color: #666;
}

.draw-free {
    font-size: 13px;
    color: #52c41a;
    font-weight: 500;
}

.draw-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.draw-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 12px 28px;
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    border-radius: 12px;
    cursor: pointer;
    transition: opacity 0.2s;
    min-width: 120px;

    &:hover {
        opacity: 0.85;
    }

    &.draw-btn-free {
        background: linear-gradient(135deg, #52c41a, #73d13d);
    }
}

.draw-btn-icon {
    font-family: fontawesome;
    font-size: 28px;
    color: #fff;
    margin-bottom: 4px;
}

.draw-btn-text {
    font-size: 16px;
    color: #fff;
    font-weight: 600;
}

.draw-btn-cost {
    font-size: 11px;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 2px;
}

// Broadcast Section
.broadcast-section {
    background: #fff;
    border-radius: 12px;
    padding: 16px 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.broadcast-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
}

.broadcast-icon {
    font-family: fontawesome;
    font-size: 18px;
    color: #fa8c16;
    animation: broadcast-pulse 2s infinite;
}

@keyframes broadcast-pulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.5; }
}

.broadcast-title {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
}

.broadcast-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-height: 120px;
    overflow: hidden;
}

.broadcast-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    padding: 6px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
        border-bottom: none;
    }
}

.broadcast-user {
    color: #1e80ff;
    font-weight: 500;
}

.broadcast-action {
    color: #999;
}

.broadcast-prize {
    color: #fa8c16;
    font-weight: 500;
}

// Records Section
.records-section {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.records-list {
    display: flex;
    flex-direction: column;
}

.record-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f2f3f5;

    &:last-child {
        border-bottom: none;
    }
}

.record-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.record-prize {
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
}

.record-time {
    font-size: 12px;
    color: #999;
}

.record-status {
    font-size: 13px;
    color: #999;
    padding: 4px 12px;
    border-radius: 12px;
    background: #f5f5f5;

    &.is-win {
        color: #52c41a;
        background: #f6ffed;
        font-weight: 500;
    }
}

.records-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 0;
    gap: 12px;
}

.empty-icon {
    font-family: fontawesome;
    font-size: 48px;
    color: #ddd;
}

.empty-text {
    font-size: 14px;
    color: #999;
}

// Footer
.lottery-footer {
    text-align: center;
    font-size: 12px;
    color: #ccc;
    padding: 16px 0;
}

// Responsive
@media screen and (max-width: 768px) {
    .lottery-content {
        padding: 0 12px 12px;
    }

    .user-info-bar {
        padding: 12px 16px;
        gap: 8px;
    }

    .user-info-section {
        gap: 4px;
    }

    .info-value {
        font-size: 16px;
    }

    .info-label {
        font-size: 12px;
    }

    .sub-tabs {
        padding: 0 12px;
        overflow-x: auto;
    }

    .tab-item {
        padding: 14px 12px;
        font-size: 13px;
        white-space: nowrap;
    }

    .prize-grid {
        grid-template-columns: repeat(2, 1fr);
    }

    .draw-section {
        flex-direction: column;
        align-items: stretch;
    }

    .draw-info {
        justify-content: center;
    }

    .draw-actions {
        justify-content: center;
    }

    .draw-btn {
        min-width: 100px;
        padding: 10px 20px;
    }

    .broadcast-section {
        padding: 12px 16px;
    }

    .broadcast-list {
        max-height: 100px;
    }

    .records-section {
        padding: 16px;
    }
}
</style>