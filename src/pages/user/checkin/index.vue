<template>
    <div class="checkin-page">
        <div class="checkin-header">
            <div class="header-left" @click="goBack">
                <span class="back-icon">&#xf060;</span>
            </div>
            <div class="header-title">成长福利</div>
            <div class="header-right"></div>
        </div>

        <div class="checkin-content">
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

            <!-- Stats Cards -->
            <div class="stats-row">
                <div class="stat-card">
                    <div class="stat-value">{{ stats.consecutiveDays }}</div>
                    <div class="stat-label">连续签到</div>
                    <div class="stat-unit">天</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">{{ stats.totalDays }}</div>
                    <div class="stat-label">累计签到</div>
                    <div class="stat-unit">天</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">{{ stats.oreCount }}</div>
                    <div class="stat-label">当前矿石</div>
                    <div class="stat-unit"></div>
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
                                :class="{
                                    'is-empty': cell.isEmpty,
                                    'is-today': cell.isToday,
                                    'is-signed': cell.isSigned,
                                    'is-future': cell.isFuture
                                }"
                                @click="handleCellClick(cell)"
                            >
                                <template v-if="!cell.isEmpty">
                                    <span class="cell-date">{{ cell.date }}</span>
                                    <span v-if="cell.isSigned && cell.reward" class="cell-reward">
                                        +{{ cell.reward }}
                                    </span>
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
                    <div class="patch-info">
                        <span class="patch-label">补签卡</span>
                        <span class="patch-count">{{ stats.patchCardCount || 0 }}张</span>
                    </div>
                </div>
            </div>

            <!-- Task List -->
            <div class="tasks-section">
                <div class="tasks-header">
                    <span class="tasks-title">我的任务</span>
                </div>
                <div class="tasks-list">
                    <div
                        v-for="task in tasks"
                        :key="task.id"
                        class="task-item"
                    >
                        <div class="task-icon">{{ task.icon }}</div>
                        <div class="task-info">
                            <div class="task-name">{{ task.name }}</div>
                            <div class="task-desc">{{ task.description }}</div>
                        </div>
                        <div class="task-reward">
                            {{ task.completed ? '已完成' : ('奖励矿石 ' + task.reward) }}
                        </div>
                        <div
                            class="task-status"
                            :class="{ completed: task.completed }"
                        >
                            {{ task.completed ? '已完成' : '去完成' }}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { doCheckIn, getCheckInRecords, getCheckInStats, getCheckInTasks } from '@/apis/checkin'
import { toast } from '@/utils/toast'

export default {
    name: 'UserCheckin',
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
            today: new Date().getDate(),
            todayYear: new Date().getFullYear(),
            todayMonth: new Date().getMonth() + 1,
            signedDates: [],
            stats: {
                consecutiveDays: 0,
                totalDays: 0,
                oreCount: 0,
                patchCardCount: 0
            },
            todaySigned: false,
            tasks: []
        }
    },
    computed: {
        calendarCells() {
            const cells = []
            const firstDay = new Date(this.currentYear, this.currentMonth - 1, 1).getDay()
            const totalDays = new Date(this.currentYear, this.currentMonth, 0).getDate()

            const isCurrentMonth =
                this.currentYear === this.todayYear &&
                this.currentMonth === this.todayMonth

            // 填充前置空白格
            for (let i = 0; i < firstDay; i++) {
                cells.push({ isEmpty: true })
            }

            // 填充日期格
            for (let d = 1; d <= totalDays; d++) {
                const dateStr = this.formatDateStr(d)
                const signedInfo = this.signedDates.find(
                    s => s.date === dateStr || s.day === d
                )
                const isFuture = isCurrentMonth
                    ? d > this.today
                    : (this.currentYear > this.todayYear ||
                        (this.currentYear === this.todayYear && this.currentMonth > this.todayMonth))

                cells.push({
                    isEmpty: false,
                    date: d,
                    isToday: isCurrentMonth && d === this.today,
                    isSigned: !!signedInfo,
                    isFuture: isFuture,
                    reward: signedInfo ? signedInfo.reward : null
                })
            }

            return cells
        }
    },
    mounted() {
        this.loadData()
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
                toast('幸运抽奖功能开发中', 2)
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
        async loadData() {
            await this.loadStats()
            await this.loadRecords()
            await this.loadTasks()
        },
        async loadStats() {
            try {
                const res = await getCheckInStats()
                if (res && res.code === 200 && res.data) {
                    const data = res.data
                    this.stats.consecutiveDays = data.consecutiveDays || 0
                    this.stats.totalDays = data.totalDays || 0
                    this.stats.oreCount = data.oreCount || 0
                    this.stats.patchCardCount = data.patchCardCount || 0
                    this.todaySigned = data.todaySigned || false
                }
            } catch (error) {
                // Keep default values when API fails
            }
        },
        async loadRecords() {
            try {
                const res = await getCheckInRecords({
                    year: this.currentYear,
                    month: this.currentMonth
                })
                if (res && res.code === 200 && res.data) {
                    this.signedDates = res.data.records || res.data || []
                }
            } catch (error) {
                this.signedDates = []
            }
        },
        async loadTasks() {
            try {
                const res = await getCheckInTasks()
                if (res && res.code === 200 && res.data) {
                    this.tasks = res.data.list || res.data || []
                }
            } catch (error) {
                // Keep empty tasks when API fails
            }
        },
        async handleCellClick(cell) {
            if (cell.isEmpty || cell.isFuture) return
            if (cell.isSigned) {
                toast('该日期已签到', 2)
                return
            }
            if (!cell.isToday) {
                toast('只能对今天进行签到', 2)
                return
            }
            if (this.todaySigned) {
                toast('今日已签到', 2)
                return
            }
            try {
                const res = await doCheckIn()
                if (res && res.code === 200) {
                    const reward = res.data && res.data.reward ? res.data.reward : 10
                    this.todaySigned = true
                    toast('签到成功！+' + reward + ' 矿石', 2)
                    await this.loadData()
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

.checkin-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: @mian-color;
    height: @top-height;
    padding: 0 15px;
    box-sizing: border-box;
    position: sticky;
    top: 0;
    z-index: 100;
}

.header-left, .header-right {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.back-icon {
    font-family: fontawesome;
    font-size: 32px;
    color: #fff;
    cursor: pointer;
}

.header-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
}

.checkin-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px 24px;
}

.sub-tabs {
    display: flex;
    background: #fff;
    border-radius: 0 0 12px 12px;
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

.stats-row {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
}

.stat-card {
    flex: 1;
    background: #fff;
    border-radius: 12px;
    padding: 20px 24px;
    display: flex;
    align-items: center;
    gap: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #fa8c16;
}

.stat-label {
    font-size: 14px;
    color: #666;
}

.stat-unit {
    font-size: 14px;
    color: #999;
}

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
    width: 120px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
}

.encourage-text {
    background: linear-gradient(135deg, #1e80ff, #69b1ff);
    border-radius: 12px;
    padding: 20px 16px;
    text-align: center;
}

.encourage-line {
    font-size: 15px;
    color: #fff;
    font-weight: 500;
    line-height: 1.8;
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

    &:hover:not(.is-empty):not(.is-future) {
        background: #f0f5ff;
    }

    &.is-empty {
        cursor: default;
    }

    &.is-today {
        background: #1e80ff;
        border-radius: 50%;

        .cell-date {
            color: #fff;
            font-weight: 600;
        }
    }

    &.is-signed {
        background: #52c41a;

        .cell-date {
            color: #fff;
        }

        .cell-reward {
            color: #fff;
        }
    }

    &.is-signed.is-today {
        background: #52c41a;
    }

    &.is-future {
        cursor: default;

        .cell-date {
            color: #ccc;
        }
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
    width: 120px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
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

.patch-info {
    text-align: center;
    font-size: 13px;
    color: #666;
}

.patch-label {
    display: block;
    color: #999;
    font-size: 12px;
    margin-bottom: 4px;
}

.patch-count {
    color: #1e80ff;
    font-weight: 500;
}

.tasks-section {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;
}

.tasks-header {
    margin-bottom: 16px;
}

.tasks-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
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

.task-icon {
    font-size: 24px;
    width: 40px;
    text-align: center;
    flex-shrink: 0;
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

@media screen and (max-width: 768px) {
    .checkin-content {
        padding: 0 12px 12px;
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

    .stats-row {
        flex-direction: column;
        gap: 8px;
    }

    .calendar-section {
        flex-direction: column;
        padding: 16px;
    }

    .calendar-left {
        width: 100%;
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

    .task-item {
        flex-wrap: wrap;
        padding: 12px 0;
    }
}
</style>