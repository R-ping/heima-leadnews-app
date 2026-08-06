<template>
    <div class="checkin-page">
        <div class="art-top" v-if="!isDesktop"><HomeBar/></div>

        <div class="checkin-content">
            <!-- 左侧侧边栏 -->
            <UserCenterSidebar
                active-menu="checkin"
                :user-info="dashboard.userInfo"
                @menu-click="handleSidebarMenuClick"
            />

            <!-- 右侧主内容区 -->
            <div class="main-area">
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

                <!-- 用户信息栏 -->
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

                <!-- 签到操作区 -->
                <div class="action-section">
                    <div class="action-left">
                        <div class="action-title">
                            <span class="ore-icon">&#xf06d;</span>
                            <span class="action-text">每日签到</span>
                        </div>
                        <div class="action-desc">连续签到领取矿石奖励</div>
                        <button
                            class="checkin-btn"
                            :class="{ 'checked': dashboard.checkinStats.todaySigned }"
                            @click="handleCheckinBtnClick"
                        >
                            <span class="btn-ore-icon">&#xf06d;</span>
                            {{ dashboard.checkinStats.todaySigned ? '今日已签到' : '立即签到' }}
                        </button>
                    </div>
                    <div class="action-stats">
                        <div class="stat-item">
                            <div class="stat-value">{{ dashboard.checkinStats.continuousDays || 0 }}</div>
                            <div class="stat-label">连续签到</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">{{ dashboard.checkinStats.totalDays || 0 }}</div>
                            <div class="stat-label">累计签到</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value ore">{{ dashboard.checkinStats.oreBalance || 0 }}</div>
                            <div class="stat-label">矿石余额</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">{{ dashboard.patchCardCount || 0 }}</div>
                            <div class="stat-label">补签卡</div>
                        </div>
                    </div>
                </div>

                <!-- 日历区域 -->
                <div class="calendar-section">
                    <div class="calendar-header">
                        <span class="calendar-title">签到日历</span>
                        <span class="today-btn" @click="goToday">今天</span>
                    </div>
                    <!-- 上一月 -->
                    <div class="calendar-month-block">
                        <div class="month-label">{{ prevYear }}年{{ prevMonth }}月</div>
                        <div class="calendar-grid">
                            <div class="calendar-header-row">
                                <div
                                    v-for="day in weekDays"
                                    :key="'prev-h-' + day"
                                    class="calendar-header-cell"
                                >{{ day }}</div>
                            </div>
                            <div class="calendar-body">
                                <div
                                    v-for="(cell, index) in prevMonthCells"
                                    :key="'prev-' + index"
                                    class="calendar-cell"
                                    :class="cellClass(cell)"
                                    @click="handleCellClick(cell)"
                                >
                                    <template v-if="!cell.isEmpty">
                                        <span class="cell-date">{{ cell.day }}</span>
                                        <span v-if="cell.reward" class="cell-reward">
                                            +{{ cell.reward }}
                                        </span>
                                        <span v-if="cell.status === 'repaired'" class="cell-patched-label">补</span>
                                        <span v-if="cell.isSpecialDay" class="cell-special-label">奖</span>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- 当前月 -->
                    <div class="calendar-month-block">
                        <div class="month-label">{{ currentYear }}年{{ currentMonth }}月</div>
                        <div class="calendar-grid">
                            <div class="calendar-header-row">
                                <div
                                    v-for="day in weekDays"
                                    :key="'curr-h-' + day"
                                    class="calendar-header-cell"
                                >{{ day }}</div>
                            </div>
                            <div class="calendar-body">
                                <div
                                    v-for="(cell, index) in calendarCells"
                                    :key="'curr-' + index"
                                    class="calendar-cell"
                                    :class="cellClass(cell)"
                                    @click="handleCellClick(cell)"
                                >
                                    <template v-if="!cell.isEmpty">
                                        <span class="cell-date">{{ cell.day }}</span>
                                        <span v-if="cell.reward" class="cell-reward">
                                            +{{ cell.reward }}
                                        </span>
                                        <span v-if="cell.status === 'repaired'" class="cell-patched-label">补</span>
                                        <span v-if="cell.isSpecialDay" class="cell-special-label">奖</span>
                                    </template>
                                </div>
                            </div>
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
    </div>
</template>

<script>
import HomeBar from '@/components/bars/home_bar'
import CheckinProgressModal from '@/components/checkin/CheckinProgressModal.vue'
import UserCenterSidebar from '@/components/user/UserCenterSidebar.vue'
import Utils from '@/utils/env'
import { getDashboard, doCheckIn, doRetroactive } from '@/apis/checkin'
import { toast } from '@/utils/toast'

export default {
    name: 'UserCheckin',
    components: { HomeBar, CheckinProgressModal, UserCenterSidebar },
    data() {
        return {
            weekDays: ['日', '一', '二', '三', '四', '五', '六'],
            currentYear: new Date().getFullYear(),
            currentMonth: new Date().getMonth() + 1,
            todayYear: new Date().getFullYear(),
            todayMonth: new Date().getMonth() + 1,
            today: new Date().getDate(),
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
        prevYear() {
            if (this.currentMonth === 1) {
                return this.currentYear - 1
            }
            return this.currentYear
        },
        prevMonth() {
            if (this.currentMonth === 1) {
                return 12
            }
            return this.currentMonth - 1
        },
        calendarCells() {
            return this.buildMonthCells(this.currentYear, this.currentMonth, true)
        },
        prevMonthCells() {
            return this.buildMonthCells(this.prevYear, this.prevMonth, false)
        }
    },
    mounted() {
        this.loadDashboard()
    },
    methods: {
        goBack() {
            this.$router.back()
        },
        handleSidebarMenuClick(key) {
            if (key === 'harvest') {
                toast('我的收获功能开发中', 2)
                return
            }
            const routeMap = {
                checkin: '/user/center/checkin',
                growth: '/user/center/growth',
                lottery: '/user/center/lottery',
                welfare: '/user/center/welfare'
            }
            const path = routeMap[key]
            if (path && this.$route.path !== path) {
                this.$router.push(path)
            }
        },
        buildMonthCells(year, month, isCurrentMonth) {
            const cells = []
            const firstDay = new Date(year, month - 1, 1).getDay()
            const totalDays = new Date(year, month, 0).getDate()

            for (let i = 0; i < firstDay; i++) {
                cells.push({ isEmpty: true })
            }

            for (let d = 1; d <= totalDays; d++) {
                const dateStr = this.formatDateStrWithYearMonth(year, month, d)
                const match = this.dashboard.calendar.find(
                    item => item.date === dateStr
                )

                const periodDay = match ? match.periodDay : null
                const specialDays = [3, 7, 14, 21, 30]
                const isSpecialDay = specialDays.includes(periodDay)

                cells.push({
                    isEmpty: false,
                    day: d,
                    date: dateStr,
                    status: match ? match.status : (isCurrentMonth ? this.getDefaultStatus(d) : 'miss'),
                    reward: match ? match.oreReward : null,
                    isSpecialDay: isSpecialDay
                })
            }

            return cells
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
                this.openCheckinModal()
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
        goToday() {
            this.currentYear = this.todayYear
            this.currentMonth = this.todayMonth
        },
        formatDateStrWithYearMonth(year, month, day) {
            const y = year
            const m = String(month).padStart(2, '0')
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
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 24px 24px;
    display: flex;
    gap: 16px;
}

.main-area {
    flex: 1;
    min-width: 0;
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

// Action Section
.action-section {
    background: #fff;
    border-radius: 16px;
    padding: 32px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
    display: flex;
    gap: 48px;
    align-items: center;
}

.action-left {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}

.action-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;

    .ore-icon {
        font-family: fontawesome;
        font-size: 20px;
        color: #fa8c16;
    }
}

.action-text {
    color: #1a1a1a;
}

.action-desc {
    font-size: 13px;
    color: #999;
    margin-bottom: 4px;
}

.checkin-btn {
    width: 180px;
    height: 52px;
    background: linear-gradient(135deg, #fa8c16, #ffc069);
    color: #fff;
    border: none;
    border-radius: 26px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all 0.2s;

    &:hover {
        opacity: 0.9;
        transform: translateY(-1px);
    }

    &.checked {
        background: linear-gradient(135deg, #52c41a, #73d13d);
        cursor: default;

        &:hover {
            transform: none;
        }
    }

    .btn-ore-icon {
        font-family: fontawesome;
        font-size: 18px;
    }
}

.action-stats {
    display: flex;
    gap: 32px;
    flex: 1;
    justify-content: center;
}

.stat-item {
    text-align: center;
}

.stat-value {
    font-size: 24px;
    font-weight: 700;
    color: #fa8c16;
}

.stat-value.ore {
    color: #fa8c16;
}

.stat-label {
    font-size: 13px;
    color: #999;
    margin-top: 4px;
}

// Calendar Section
.calendar-section {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f2f5;
}

.calendar-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
}

.today-btn {
    padding: 6px 16px;
    border: 1px solid #1e80ff;
    border-radius: 16px;
    color: #1e80ff;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: #e6f4ff;
    }
}

.calendar-month-block {
    margin-bottom: 20px;

    &:last-child {
        margin-bottom: 0;
    }
}

.month-label {
    font-size: 14px;
    font-weight: 500;
    color: #666;
    margin-bottom: 12px;
}

.calendar-grid {
    width: 100%;
}

.calendar-header-row {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 8px;
    padding-bottom: 8px;
    margin-bottom: 4px;
    border-bottom: 1px solid #f0f2f5;
}

.calendar-header-cell {
    text-align: center;
    font-size: 13px;
    color: #999;
    height: 28px;
    line-height: 28px;
}

.calendar-body {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 8px;
}

.calendar-cell {
    aspect-ratio: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;
    background: #f5f7fa;

    &.is-signed {
        background: linear-gradient(135deg, #52c41a, #73d13d);
        color: #fff;

        .cell-date {
            color: #fff;
            font-weight: 600;
        }

        .cell-reward {
            color: rgba(255, 255, 255, 0.9);
        }
    }

    &.is-today {
        background: #1e80ff;
        color: #fff;
        box-shadow: 0 2px 8px rgba(30, 128, 255, 0.3);

        .cell-date {
            color: #fff;
            font-weight: 600;
        }
    }

    &.is-missed {
        background: #f5f5f5;
        color: #bbb;

        .cell-date {
            color: #bbb;
        }

        &:hover {
            background: #fff7e6;
            color: #fa8c16;

            .cell-date {
                color: #fa8c16;
            }
        }
    }

    &.is-future {
        background: #fafafa;
        color: #d9d9d9;
        cursor: not-allowed;

        .cell-date {
            color: #d9d9d9;
        }
    }

    &.is-repaired {
        background: #fff7e6;
        border: 1px dashed #fa8c16;
        color: #fa8c16;

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

    &.is-empty {
        background: transparent;
        cursor: default;
    }

    &:hover:not(.is-empty):not(.is-future):not(.is-signed):not(.is-today) {
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
        flex-direction: column;
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

    .action-section {
        flex-direction: column;
        gap: 24px;
        padding: 24px 16px;
    }

    .action-stats {
        gap: 16px;
        flex-wrap: wrap;
    }

    .stat-value {
        font-size: 20px;
    }

    .calendar-cell {
        border-radius: 6px;
    }

    .cell-date {
        font-size: 13px;
    }

    .cell-reward {
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