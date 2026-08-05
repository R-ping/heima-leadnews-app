<template>
    <div class="checkin-page">
        <div class="art-top" v-if="!isDesktop"><HomeBar/></div>

        <div class="checkin-content">
            <!-- 签到进度弹框 -->
            <CheckinProgressModal
                :visible="showCheckinModal"
                :mode="checkinModalMode"
                :earnedOre="checkinResult.earnedOre"
                :milestoneProgress="checkinResult.milestoneProgress"
                :nextSpecial="checkinResult.nextSpecial"
                :isSigned="dashboard.checkinStats.todaySigned"
                @close="closeCheckinModal"
                @checkin-success="handleCheckinSuccess"
            />

            <!-- User Info Bar -->
            <div class="user-info-bar">
                <div class="user-info-section">
                    <span class="user-nickname">{{ dashboard.userInfo.nickname || '用户' }}</span>
                    <span class="user-level-badge">{{ dashboard.userInfo.level || 'ZR.1' }}</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section">
                    <span class="info-value">{{ dashboard.checkinStats.continuousDays || 0 }}</span>
                    <span class="info-label">连续签到天数</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section">
                    <span class="info-value">{{ dashboard.checkinStats.totalDays || 0 }}</span>
                    <span class="info-label">累计签到天数</span>
                </div>
                <span class="info-separator">|</span>
                <div class="user-info-section ore-section">
                    <span class="info-value ore-value">{{ dashboard.checkinStats.oreBalance || 0 }}</span>
                    <span class="ore-icon">&#xf06d;</span>
                    <span class="ore-question" title="矿石说明">?</span>
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

            <!-- Calendar Section -->
            <div class="calendar-section">
                <div class="calendar-left">
                    <div class="encourage-text">
                        <div class="encourage-line">今日签到</div>
                        <div class="encourage-line">领取矿石</div>
                        <div class="encourage-line">加速升级</div>
                    </div>
                    <button 
                        class="checkin-action-btn" 
                        :class="{ 'signed-btn': dashboard.checkinStats.todaySigned }"
                        @click="handleCheckinBtnClick"
                    >
                        {{ dashboard.checkinStats.todaySigned ? '今日已签到' : '立即签到' }}
                    </button>
                </div>

                <div class="calendar-center">
                    <div class="calendar-grid">
                        <div class="calendar-header-row">
                            <div
                                v-for="day in weekDays"
                                :key="day"
                                class="calendar-header-cell"
                            >
                                {{ day }}
                            </div>
                        </div>
                        <div class="calendar-body">
                            <div
                                v-for="(cell, index) in calendarCells"
                                :key="index"
                                class="calendar-cell"
                                :class="cellClass(cell)"
                                @click="handleCellClick(cell)"
                            >
                                <template v-if="!cell.isEmpty">
                                    <span class="cell-date">{{ cell.day }}</span>
                                    <span v-if="cell.oreReward" class="cell-reward">
                                        +{{ cell.oreReward }}
                                    </span>
                                    <span v-if="cell.status === 'repaired'" class="cell-patched-label">补</span>
                                    <span v-if="cell.isSpecialDay" class="cell-special-label">奖</span>
                                </template>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="calendar-right">
                    <div class="month-nav">
                        <span class="month-arrow" @click="prevMonth">&#xf104;</span>
                        <span class="month-label">{{ currentYear }}年{{ currentMonth }}月</span>
                        <span class="month-arrow" @click="nextMonth">&#xf105;</span>
                    </div>
                    <div class="today-btn" @click="goToday">今天</div>
                    <div class="patch-card-info">
                        <span class="patch-label">补签卡</span>
                        <span class="patch-count">{{ dashboard.patchCardCount || 0 }}张</span>
                    </div>
                    <div class="next-reward" v-if="dashboard.nextSpecialReward">
                        <div class="next-reward-title">下一奖励</div>
                        <div class="next-reward-item">
                            <span class="next-day">第{{ dashboard.nextSpecialReward.day }}天</span>
                            <span class="next-ore">+{{ dashboard.nextSpecialReward.ore }}</span>
                        </div>
                        <div class="next-days-left">
                            还需{{ dashboard.nextSpecialReward.daysLeft }}天
                        </div>
                    </div>
                    <div class="qr-code-area">
                        <div class="qr-placeholder">&#xf029;</div>
                        <div class="qr-text">扫描右侧二维码</div>
                        <div class="qr-text">分享给好友</div>
                    </div>
                </div>
            </div>

            <!-- Bottom Sections -->
            <div class="bottom-sections">
                <div class="bottom-section section-notice">
                    <div class="section-title">掘金公告</div>
                    <div class="notice-list">
                        <div class="notice-item">
                            <span class="notice-name">公益计划</span>
                            <span class="notice-link">了解详情 &#xf105;</span>
                        </div>
                        <div class="notice-item">
                            <span class="notice-name">游戏入口调整</span>
                            <span class="notice-link">了解详情 &#xf105;</span>
                        </div>
                        <div class="notice-item">
                            <span class="notice-link">捐赠详情 &#xf105;</span>
                        </div>
                    </div>
                </div>
                <div class="bottom-section section-exchange">
                    <div class="section-title">点石成金</div>
                    <div class="exchange-content">
                        <div class="exchange-desc">用矿石兑换公益基金</div>
                        <div class="exchange-btn">去兑换</div>
                    </div>
                </div>
            </div>

            <!-- My Tasks Section -->
            <div class="tasks-section">
                <div class="tasks-header">
                    <span class="tasks-title">我的任务</span>
                    <span class="tasks-question-icon" title="任务说明">?</span>
                </div>
                <div class="tasks-list">
                    <div
                        v-for="task in dashboard.tasks"
                        :key="task.id"
                        class="task-item"
                    >
                        <div class="task-info">
                            <div class="task-name">{{ task.name }}</div>
                            <div class="task-desc">{{ task.description }}</div>
                        </div>
                        <div class="task-reward">
                            +{{ task.reward }} 矿石
                        </div>
                        <div
                            class="task-status"
                            :class="{ completed: task.status === 'completed' }"
                        >
                            {{ task.status === 'completed' ? '已完成' : '去完成' }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- Footer -->
            <div class="checkin-footer">
                用户协议 · 法律声明 &copy;{{ new Date().getFullYear() }} 稀土掘金
            </div>
        </div>
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import CheckinProgressModal from '@/components/checkin/CheckinProgressModal.vue'
import Utils from '@/utils/env'
import { getDashboard, doCheckIn, doRetroactive } from '@/apis/checkin'
import { toast } from '@/utils/toast'

export default {
    name: 'UserCheckin',
    components: { HomeBar, CheckinProgressModal },
    data() {
        return {
            activeTab: 'checkin',
            subTabs: [
                { key: 'checkin', label: '每日签到' },
                { key: 'growth', label: '成长等级' },
                { key: 'lottery', label: '幸运抽奖' },
                { key: 'exchange', label: '福利兑换' },
                { key: 'harvest', label: '我的收获' }
            ],
            weekDays: ['日', '一', '二', '三', '四', '五', '六'],
            currentYear: new Date().getFullYear(),
            currentMonth: new Date().getMonth() + 1,
            todayYear: new Date().getFullYear(),
            todayMonth: new Date().getMonth() + 1,
            today: new Date().getDate(),
            // 签到弹框相关
            showCheckinModal: false,
            checkinModalMode: 'entry',
            checkinResult: {
                earnedOre: 0,
                milestoneProgress: { current: 0, total: 30, percent: 0, specialDays: [] },
                nextSpecial: null
            },
            dashboard: {
                userInfo: { nickname: '', level: '', userId: 0 },
                checkinStats: { 
                    continuousDays: 0, 
                    totalDays: 0, 
                    oreBalance: 0, 
                    todaySigned: false,
                    pendingReward: 0
                },
                patchCardCount: 0,
                currentPeriodDay: 0,
                nextSpecialReward: null,
                milestoneProgress: { current: 0, total: 30, percent: 0, specialDays: [] },
                calendar: [],
                tasks: []
            }
        }
    },
    computed: {
        isDesktop() {
            return Utils.isDesktop()
        },
        calendarCells() {
            const cells = []
            const firstDay = new Date(this.currentYear, this.currentMonth - 1, 1).getDay()
            const totalDays = new Date(this.currentYear, this.currentMonth, 0).getDate()

            for (let i = 0; i < firstDay; i++) {
                cells.push({ isEmpty: true })
            }

            for (let d = 1; d <= totalDays; d++) {
                const dateStr = this.formatDateStr(d)
                const match = this.dashboard.calendar.find(
                    item => item.date === dateStr
                )

                // 判断是否为特殊奖励日
                const periodDay = match ? match.periodDay : null
                const specialDays = [3, 7, 14, 21, 30]
                const isSpecialDay = specialDays.includes(periodDay)

                cells.push({
                    isEmpty: false,
                    day: d,
                    date: dateStr,
                    status: match ? match.status : this.getDefaultStatus(d),
                    reward: match ? match.oreReward : null,
                    isSpecialDay: isSpecialDay
                })
            }

            return cells
        }
    },
    mounted() {
        this.loadDashboard()
    },
    methods: {
        goBack() {
            this.$router.back()
        },
        switchTab(tab) {
            if (tab.key === 'checkin') {
                this.activeTab = tab.key
                return
            }
            if (tab.key === 'growth') {
                this.$router.push('/user/growth')
                return
            }
            if (tab.key === 'lottery') {
                this.$router.push('/user/lottery')
                return
            }
            if (tab.key === 'exchange') {
                this.$router.push('/user/welfare')
                return
            }
            if (tab.key === 'harvest') {
                toast('我的收获功能开发中', 2)
                return
            }
        },
        getDefaultStatus(d) {
            const isCurrentMonth =
                this.currentYear === this.todayYear &&
                this.currentMonth === this.todayMonth
            if (isCurrentMonth && d === this.today) {
                return 'today'
            }
            const cellDate = new Date(this.currentYear, this.currentMonth - 1, d)
            const todayDate = new Date(this.todayYear, this.todayMonth - 1, this.today)
            if (cellDate > todayDate) {
                return 'future'
            }
            return 'miss'
        },
        cellClass(cell) {
            if (cell.isEmpty) return 'is-empty'
            const map = {
                signed: 'is-signed',
                repaired: 'is-repaired',
                miss: 'is-missed',
                today: 'is-today',
                future: 'is-future'
            }
            return map[cell.status] || ''
        },
        async loadDashboard() {
            try {
                const res = await getDashboard()
                if (res && res.code === 200 && res.data) {
                    const data = res.data
                    this.dashboard.userInfo = data.userInfo || { nickname: '用户', level: 'ZR.1' }
                    this.dashboard.checkinStats = data.checkinStats || { 
                        continuousDays: 0, totalDays: 0, oreBalance: 0, todaySigned: false 
                    }
                    this.dashboard.patchCardCount = data.patchCardCount || 0
                    this.dashboard.currentPeriodDay = data.currentPeriodDay || 0
                    this.dashboard.nextSpecialReward = data.nextSpecialReward || null
                    this.dashboard.milestoneProgress = data.milestoneProgress || { current: 0, total: 30, percent: 0, specialDays: [] }
                    
                    // 标准化日历数据
                    if (data.calendar) {
                        this.dashboard.calendar = data.calendar.map(item => {
                            const statusMap = {
                                'signed': 'signed',
                                'repaired': 'repaired',
                                'miss': 'miss',
                                'today': 'today',
                                'future': 'future'
                            }
                            return {
                                date: item.date,
                                status: statusMap[item.status] || 'future',
                                oreReward: item.oreReward || null,
                                periodDay: item.periodDay || null,
                                isSpecial: item.isSpecial || false
                            }
                        })
                    } else {
                        this.dashboard.calendar = []
                    }
                    this.dashboard.tasks = data.tasks || []
                }
            } catch (error) {
                toast('加载数据失败，请稍后重试', 2)
            }
        },
        async loadRecords() {
            await this.loadDashboard()
        },
        async handleCellClick(cell) {
            if (cell.isEmpty) return
            if (cell.status === 'future') return

            if (cell.status === 'signed' || cell.status === 'repaired') {
                toast('该日期已签到', 2)
                return
            }

            if (cell.status === 'miss') {
                if (this.dashboard.patchCardCount <= 0) {
                    toast('没有补签卡了', 2)
                    return
                }
                try {
                    const res = await doRetroactive(cell.date)
                    if (res && res.code === 200) {
                        const data = res.data || {}
                        toast('补签成功！+' + (data.earnedOre || 0) + ' 矿石', 2)
                        await this.loadDashboard()
                    } else {
                        toast(res && res.message ? res.message : '补签失败', 2)
                    }
                } catch (error) {
                    toast('补签失败，请稍后重试', 2)
                }
                return
            }

            if (cell.status === 'today') {
                this.openCheckinModal()
            }
        },
        openCheckinModal() {
            this.checkinModalMode = 'entry'
            this.showCheckinModal = true
        },
        closeCheckinModal() {
            this.showCheckinModal = false
        },
        async handleCheckinSuccess(data) {
            this.checkinResult = {
                earnedOre: data.earnedOre || 0,
                milestoneProgress: data.milestoneProgress || { current: 0, total: 30, percent: 0, specialDays: [] },
                nextSpecial: data.nextSpecial || null
            }
            this.checkinModalMode = 'success'
            await this.loadDashboard()
        },
        async handleCheckinBtnClick() {
            if (this.dashboard.checkinStats.todaySigned) {
                // 已签到，不做操作
                return
            }
            try {
                const res = await doCheckIn()
                if (res && res.code === 200 && res.data) {
                    await this.handleCheckinSuccess(res.data)
                } else {
                    toast(res && res.message ? res.message : '签到失败', 2)
                }
            } catch (error) {
                toast('签到失败，请稍后重试', 2)
            }
        },
        prevMonth() {
            if (this.currentMonth === 1) {
                this.currentYear -= 1
                this.currentMonth = 12
            } else {
                this.currentMonth -= 1
            }
            this.loadRecords()
        },
        nextMonth() {
            if (this.currentMonth === 12) {
                this.currentYear += 1
                this.currentMonth = 1
            } else {
                this.currentMonth += 1
            }
            this.loadRecords()
        },
        goToday() {
            this.currentYear = this.todayYear
            this.currentMonth = this.todayMonth
            this.loadRecords()
        },
        formatDateStr(day) {
            const y = this.currentYear
            const m = String(this.currentMonth).padStart(2, '0')
            const d = String(day).padStart(2, '0')
            return y + '-' + m + '-' + d
        }
    }
}
</script>

<style lang="less" scoped>
@import '../../../styles/common';

.checkin-page {
    min-height: 100vh;
    background: #f5f7fa;
}

.checkin-content {
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

// Calendar Section
.calendar-section {
    display: flex;
    gap: 16px;
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.calendar-left {
    width: 140px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;
}

.encourage-text {
    background: linear-gradient(135deg, #1e80ff, #69b1ff);
    border-radius: 12px;
    padding: 20px 16px;
    text-align: center;
    width: 100%;
}

.encourage-line {
    font-size: 15px;
    color: #fff;
    font-weight: 500;
    line-height: 1.8;
}

.checkin-action-btn {
    width: 100%;
    padding: 12px 20px;
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    color: #fff;
    border: none;
    border-radius: 24px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        opacity: 0.9;
        transform: translateY(-1px);
    }

    &.signed-btn {
        background: #52c41a;
        cursor: not-allowed;
    }
}

.calendar-center {
    flex: 1;
    min-width: 0;
}

.calendar-grid {
    width: 100%;
}

.calendar-header-row {
    display: flex;
    border-bottom: 1px solid #f0f2f5;
    padding-bottom: 8px;
    margin-bottom: 8px;
}

.calendar-header-cell {
    flex: 1;
    text-align: center;
    font-size: 13px;
    color: #999;
    height: 32px;
    line-height: 32px;
}

.calendar-body {
    display: flex;
    flex-wrap: wrap;
}

.calendar-cell {
    width: calc(100% / 7);
    aspect-ratio: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
    position: relative;

    &.is-empty {
        cursor: default;
    }

    &.is-signed {
        background: #f6ffed;

        .cell-date {
            color: #52c41a;
            font-weight: 600;
        }

        .cell-reward {
            color: #52c41a;
        }
    }

    &.is-repaired {
        background: #fff7e6;
        border: 1px dashed #fa8c16;

        .cell-date {
            color: #fa8c16;
            font-weight: 600;
        }

        .cell-reward {
            color: #fa8c16;
        }

        .cell-patched-label {
            position: absolute;
            top: 2px;
            right: 2px;
            font-size: 10px;
            color: #fa8c16;
            background: #fff;
            padding: 0 3px;
            border-radius: 2px;
        }
    }

    &.is-missed {
        background: #f5f5f5;

        .cell-date {
            color: #999;
        }

        &:hover {
            background: #e6f7ff;
            cursor: pointer;

            .cell-date {
                color: #1e80ff;
            }
        }
    }

    &.is-today {
        background: #1e80ff;

        .cell-date {
            color: #fff;
            font-weight: 600;
        }

        &:hover {
            background: #4096ff;
        }
    }

    &.is-future {
        cursor: default;

        .cell-date {
            color: #ccc;
        }
    }

    &:hover:not(.is-empty):not(.is-future) {
        opacity: 0.85;
    }

    .cell-special-label {
        position: absolute;
        top: 2px;
        left: 2px;
        font-size: 9px;
        color: #fa8c16;
        background: #fff7e6;
        padding: 0 3px;
        border-radius: 2px;
        font-weight: 600;
    }
}

.cell-date {
    font-size: 15px;
    color: #1a1a1a;
    line-height: 1;
}

.cell-reward {
    font-size: 11px;
    color: #52c41a;
    margin-top: 2px;
    font-weight: 500;
}

.calendar-right {
    width: 160px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding-top: 8px;
}

.month-nav {
    display: flex;
    align-items: center;
    gap: 8px;
}

.month-arrow {
    font-family: fontawesome;
    font-size: 18px;
    color: #666;
    cursor: pointer;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;

    &:hover {
        background: #f0f2f5;
        color: #1e80ff;
    }
}

.month-label {
    font-size: 14px;
    color: #1a1a1a;
    font-weight: 500;
    white-space: nowrap;
}

.today-btn {
    padding: 6px 20px;
    border: 1px solid #1e80ff;
    border-radius: 16px;
    color: #1e80ff;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: #1e80ff;
        color: #fff;
    }
}

.patch-card-info {
    text-align: center;
}

.patch-label {
    display: block;
    font-size: 12px;
    color: #999;
    margin-bottom: 4px;
}

.patch-count {
    font-size: 14px;
    color: #1e80ff;
    font-weight: 500;
}

.next-reward {
    width: 100%;
    background: linear-gradient(135deg, #fff7e6, #ffe7ba);
    border-radius: 8px;
    padding: 10px;
    text-align: center;
}

.next-reward-title {
    font-size: 11px;
    color: #999;
    margin-bottom: 4px;
}

.next-reward-item {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-bottom: 4px;
}

.next-day {
    font-size: 13px;
    color: #fa8c16;
    font-weight: 500;
}

.next-ore {
    font-size: 13px;
    color: #fa8c16;
    font-weight: 600;
}

.next-days-left {
    font-size: 11px;
    color: #999;
}

.qr-code-area {
    text-align: center;
    padding: 8px;
    border: 1px dashed #e8e8e8;
    border-radius: 8px;
    width: 100%;
}

.qr-placeholder {
    font-family: fontawesome;
    font-size: 40px;
    color: #ccc;
    margin-bottom: 6px;
}

.qr-text {
    font-size: 11px;
    color: #999;
    line-height: 1.5;
}

// Bottom Sections
.bottom-sections {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 16px;
}

.bottom-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 16px;
}

.notice-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.notice-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #f2f3f5;

    &:last-child {
        border-bottom: none;
    }
}

.notice-name {
    font-size: 14px;
    color: #1a1a1a;
}

.notice-link {
    font-size: 13px;
    color: #1e80ff;
    cursor: pointer;
    font-family: fontawesome;

    &:hover {
        opacity: 0.8;
    }
}

.exchange-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    padding: 20px 0;
}

.exchange-desc {
    font-size: 14px;
    color: #666;
}

.exchange-btn {
    padding: 10px 32px;
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    color: #fff;
    border-radius: 24px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
        opacity: 0.85;
    }
}

// Tasks Section
.tasks-section {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.tasks-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
}

.tasks-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}

.tasks-question-icon {
    font-family: fontawesome;
    font-size: 16px;
    color: #ccc;
    margin-left: 8px;
    cursor: pointer;
}

.tasks-list {
    display: flex;
    flex-direction: column;
}

.task-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #f2f3f5;
    gap: 12px;

    &:last-child {
        border-bottom: none;
    }
}

.task-info {
    flex: 1;
}

.task-name {
    font-size: 15px;
    color: #1a1a1a;
    font-weight: 500;
    margin-bottom: 4px;
}

.task-desc {
    font-size: 13px;
    color: #999;
}

.task-reward {
    font-size: 13px;
    color: #52c41a;
    font-weight: 500;
    white-space: nowrap;
}

.task-status {
    padding: 6px 16px;
    border-radius: 16px;
    font-size: 13px;
    white-space: nowrap;
    border: 1px solid #1e80ff;
    color: #1e80ff;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: rgba(30, 128, 255, 0.1);
    }

    &.completed {
        border-color: #ccc;
        color: #999;
        cursor: default;
        background: #f5f5f5;

        &:hover {
            background: #f5f5f5;
        }
    }
}

// Footer
.checkin-footer {
    text-align: center;
    font-size: 12px;
    color: #ccc;
    padding: 16px 0;
}

// Responsive
@media screen and (max-width: 768px) {
    .checkin-content {
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

    .calendar-section {
        flex-direction: column;
        padding: 16px;
    }

    .calendar-left {
        width: 100%;
        flex-direction: row;
        justify-content: space-around;
    }

    .encourage-text {
        display: flex;
        gap: 12px;
        padding: 12px 16px;
    }

    .encourage-line {
        font-size: 14px;
    }

    .calendar-right {
        width: 100%;
        flex-direction: row;
        justify-content: space-between;
        flex-wrap: wrap;
    }

    .calendar-cell {
        border-radius: 6px;
    }

    .cell-date {
        font-size: 13px;
    }

    .cell-reward,
    .cell-missed-label {
        font-size: 10px;
    }

    .bottom-sections {
        grid-template-columns: 1fr;
    }

    .task-item {
        flex-wrap: wrap;
        padding: 12px 0;
    }
}
</style>